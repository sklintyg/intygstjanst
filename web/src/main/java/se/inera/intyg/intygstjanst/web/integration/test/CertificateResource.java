/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.intygstjanst.web.integration.test;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.web.integration.converter.ConverterUtil;

/**
 * @author andreaskaltenbach
 */
@Path("/certificate")
public class CertificateResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateResource.class);

    @PersistenceContext
    private EntityManager entityManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    public void setTxManager(PlatformTransactionManager txManager) {
        this.transactionTemplate = new TransactionTemplate(txManager);
    }

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Certificate getCertificate(@PathParam("id") String id) {
        return entityManager.find(Certificate.class, id);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCertificate(@PathParam("id") final String id) {
        return transactionTemplate.execute(new TransactionCallback<Response>() {
            public Response doInTransaction(TransactionStatus status) {
                try {
                    Certificate certificate = entityManager.find(Certificate.class, id);
                    if (certificate != null) {
                        entityManager.remove(certificate.getOriginalCertificate());
                        entityManager.remove(certificate);
                    }

                    // Also delete any SjukfallCertificate
                    SjukfallCertificate sjukfallCertificate = entityManager.find(SjukfallCertificate.class, id);
                    if (sjukfallCertificate != null) {
                        entityManager.remove(sjukfallCertificate);
                    }
                    return Response.ok().build();
                } catch (Throwable t) {
                    status.setRollbackOnly();
                    LOGGER.warn("delete certificate with id " + id + " failed: " + t.getMessage());
                    return Response.serverError().build();
                }
            }
        });
    }

    @DELETE
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllCertificates() {
        return transactionTemplate.execute(new TransactionCallback<Response>() {
            public Response doInTransaction(TransactionStatus status) {
                try {
                    @SuppressWarnings("unchecked")
                    List<Certificate> certificates = entityManager.createQuery("SELECT c FROM Certificate c").getResultList();
                    for (Certificate certificate : certificates) {
                        if (certificate.getOriginalCertificate() != null) {
                            entityManager.remove(certificate.getOriginalCertificate());
                        }
                        entityManager.remove(certificate);
                    }

                    // Also delete any SjukfallCertificates
                    List<SjukfallCertificate> sjukfallCertificates = entityManager.createQuery("SELECT c FROM SjukfallCertificate c", SjukfallCertificate.class).getResultList();
                    for (SjukfallCertificate sjukfallCert : sjukfallCertificates) {
                        entityManager.remove(sjukfallCert);
                    }

                    return Response.ok().build();
                } catch (Throwable t) {
                    status.setRollbackOnly();
                    LOGGER.warn("delete all certificates failed: " + t.getMessage());
                    return Response.serverError().build();
                }
            }
        });
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response insertCertificate(final CertificateHolder certificateHolder) {
        return transactionTemplate.execute(new TransactionCallback<Response>() {
            public Response doInTransaction(TransactionStatus status) {
                Certificate certificate = ConverterUtil.toCertificate(certificateHolder);
                try {
                    OriginalCertificate originalCertificate = new OriginalCertificate();
                    originalCertificate.setReceived(new LocalDateTime());

                    // Call marshall from the modules ModuleApi to create XML for originalCertificate
                    ModuleApi moduleApi = null;
                    try {
                        moduleApi = moduleRegistry.getModuleApi(certificateHolder.getType());
                    } catch (ModuleNotFoundException e) {
                        LOGGER.error("Module {} not found ", certificateHolder.getType());
                    }
                    if (moduleApi != null && moduleApi.marshall(certificate.getDocument()) != null) {
                        originalCertificate.setDocument(moduleApi.marshall(certificate.getDocument()));
                    } else {
                        LOGGER.debug("Got null while populating with original_certificate");
                        originalCertificate.setDocument(certificate.getDocument());
                    }

                    originalCertificate.setCertificate(certificate);
                    entityManager.persist(certificate);
                    entityManager.persist(originalCertificate);
                    return Response.ok().build();
                } catch (Throwable t) {
                    status.setRollbackOnly();
                    LOGGER.warn("insert certificate with id " + certificate.getId() + " failed: " + t.getMessage());
                    return Response.serverError().build();
                }
            }
        });
    }

}
