package se.inera.certificate.integration.test;

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

import se.inera.certificate.integration.rest.ModuleRestApi;
import se.inera.certificate.integration.rest.ModuleRestApiFactory;
import se.inera.certificate.integration.util.RestUtils;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.model.dao.OriginalCertificate;

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
    private ModuleRestApiFactory moduleRestApiFactory;

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
                    entityManager.remove(certificate.getOriginalCertificate());
                    entityManager.remove(certificate);
                    return Response.ok().build();
                } catch (Throwable t) {
                    status.setRollbackOnly();
                    LOGGER.warn("delete certificate with id " + id + " failed: " + t.getMessage());
                    return Response.serverError().build();
                }
            }
        });
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response insertCertificate(final Certificate certificate) {
        return transactionTemplate.execute(new TransactionCallback<Response>() {
            public Response doInTransaction(TransactionStatus status) {
                try {
                    OriginalCertificate originalCertificate = new OriginalCertificate();
                    originalCertificate.setReceived(new LocalDateTime());
                    originalCertificate.setDocument(marshall(certificate));
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
    
    protected String marshall(Certificate certificate) {
        ModuleRestApi moduleRestApi = moduleRestApiFactory.getModuleRestService("fk7263");
        Response response = moduleRestApi.marshall("1.0", certificate.getDocument());

        switch (response.getStatus()) {
        case 200:
            return RestUtils.entityAsString(response);
        default:
            String errorMessage = "Failed to unmarshal certificate for certificate type '" + certificate.getType()
                    + "'. HTTP status code is " + response.getStatus();
            LOGGER.error(errorMessage);
            return "";
        }

    }
}
