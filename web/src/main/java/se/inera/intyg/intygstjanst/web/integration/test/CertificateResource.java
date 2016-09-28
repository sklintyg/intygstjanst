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

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.intygstjanst.persistence.model.dao.*;
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

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Certificate getCertificate(@PathParam("id") String id) {
        return entityManager.find(Certificate.class, id);
    }

    @DELETE
    @Path("/citizen/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCertificatesForCitizen(@PathParam("id") String id) {
        return transactionTemplate.execute(status -> {
            try {
                LOGGER.info("Deleting certificates for citizen {}", id);
                @SuppressWarnings("unchecked")
                List<String> certificates = entityManager.createQuery("SELECT c.id FROM Certificate c WHERE c.civicRegistrationNumber=:personId")
                        .setParameter("personId", id).getResultList();
                for (String certificate : certificates) {
                    deleteCertificate(certificate);
                }
                return Response.ok().build();
            } catch (Exception e) {
                status.setRollbackOnly();
                LOGGER.warn("delete certificates for citizen {} failed", id, e);
                return Response.serverError().build();
            }
        });
    }

    @DELETE
    @Path("/unit/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCertificatesForUnit(@PathParam("id") String id) {
        return transactionTemplate.execute(status -> {
            try {
                LOGGER.info("Deleting certificates for unit {}", id);
                @SuppressWarnings("unchecked")
                List<String> certificates = entityManager.createQuery("SELECT c.id FROM Certificate c WHERE c.careUnitId=:careUnitHsaId")
                        .setParameter("careUnitHsaId", id).getResultList();
                for (String certificate : certificates) {
                    deleteCertificate(certificate);
                }
                return Response.ok().build();
            } catch (Exception e) {
                status.setRollbackOnly();
                LOGGER.warn("delete certificates for unit {} failed", id, e);
                return Response.serverError().build();
            }
        });
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCertificate(@PathParam("id") final String id) {
        return transactionTemplate.execute(status -> {
            try {
                LOGGER.info("Deleting certificate {}", id);
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
            } catch (Exception e) {
                status.setRollbackOnly();
                LOGGER.warn("delete certificate with id {} failed", id, e);
                return Response.serverError().build();
            }
        });
    }

    @DELETE
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllCertificates() {
        return transactionTemplate.execute(status -> {
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
                List<SjukfallCertificate> sjukfallCertificates = entityManager
                        .createQuery("SELECT c FROM SjukfallCertificate c", SjukfallCertificate.class).getResultList();
                for (SjukfallCertificate sjukfallCert : sjukfallCertificates) {
                    entityManager.remove(sjukfallCert);
                }

                return Response.ok().build();
            } catch (Exception e) {
                status.setRollbackOnly();
                LOGGER.warn("delete all certificates failed", e);
                return Response.serverError().build();
            }
        });
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response insertCertificate(final CertificateHolder certificateHolder) {
        return transactionTemplate.execute(status -> {
            Certificate certificate = ConverterUtil.toCertificate(certificateHolder);
            try {
                LOGGER.info("insert certificate {} ({})", certificate.getId(), certificate.getType());
                OriginalCertificate originalCertificate = new OriginalCertificate(LocalDateTime.now(), getXmlBody(certificateHolder), certificate);
                entityManager.persist(certificate);
                entityManager.persist(originalCertificate);
                return Response.ok().build();
            } catch (Exception e) {
                status.setRollbackOnly();
                LOGGER.warn("insert certificate {} ({}) failed", certificate.getId(), certificate.getType(), e);
                return Response.serverError().build();
            }
        });
    }

    private String getXmlBody(CertificateHolder certificateHolder) throws IOException {
        if (StringUtils.isNotBlank(certificateHolder.getOriginalCertificate())) {
            return certificateHolder.getOriginalCertificate();
        } else {
            File file = new ClassPathResource("content/intyg-" + certificateHolder.getType() + "-content.xml").getFile();
            return FileUtils.readFileToString(file)
                    .replace("CERTIFICATE_ID", certificateHolder.getId())
                    .replace("PATIENT_CRN", certificateHolder.getCivicRegistrationNumber().getPersonnummerWithoutDash())
                    .replace("CAREUNIT_ID", certificateHolder.getCareUnitId())
                    .replace("CAREUNIT_NAME", certificateHolder.getCareUnitName())
                    .replace("CAREGIVER_ID", certificateHolder.getCareGiverId())
                    .replace("DOCTOR_NAME", certificateHolder.getSigningDoctorName())
                    .replace("SIGNED_DATE", certificateHolder.getSignedDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

}
