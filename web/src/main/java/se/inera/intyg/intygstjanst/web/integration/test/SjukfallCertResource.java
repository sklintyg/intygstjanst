/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;

/**
 * @author andreaskaltenbach
 */
@Path("/sjukfallcert")
public class SjukfallCertResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SjukfallCertResource.class);

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
    public SjukfallCertificate getSjukfallCertificate(@PathParam("id") String id) {
        return entityManager.find(SjukfallCertificate.class, id);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSjukfallCertificate(@PathParam("id") final String id) {
        return transactionTemplate.execute(new TransactionCallback<Response>() {
            @Override
            public Response doInTransaction(TransactionStatus status) {
                try {
                    SjukfallCertificate cert = entityManager.find(SjukfallCertificate.class, id);
                    if (cert != null) {
                        entityManager.remove(cert);
                    }
                    return Response.ok().build();
                } catch (Exception e) {
                    status.setRollbackOnly();
                    LOGGER.warn("deleted sjukfall certificate with id {} failed: {}", id, e);
                    return Response.serverError().build();
                }
            }
        });
    }

    @DELETE
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllSjukfallCertificates() {
        return transactionTemplate.execute(new TransactionCallback<Response>() {
            @Override
            public Response doInTransaction(TransactionStatus status) {
                try {
                    @SuppressWarnings("unchecked")
                    List<SjukfallCertificate> certificates = entityManager.createQuery("SELECT sc FROM SjukfallCertificate sc")
                        .getResultList();
                    for (SjukfallCertificate sjukfallCert : certificates) {
                        entityManager.remove(sjukfallCert);
                    }
                    return Response.ok().build();
                } catch (Exception e) {
                    status.setRollbackOnly();
                    LOGGER.warn("delete all sjukfall certificates failed: {}", e);
                    return Response.serverError().build();
                }
            }
        });
    }
}
