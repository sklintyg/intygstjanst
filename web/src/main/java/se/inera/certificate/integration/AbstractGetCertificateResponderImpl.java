package se.inera.certificate.integration;

import static se.inera.certificate.integration.util.ResultOfCallUtil.failResult;
import static se.inera.certificate.integration.util.ResultOfCallUtil.infoResult;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import se.inera.certificate.exception.CertificateRevokedException;
import se.inera.certificate.exception.InvalidCertificateException;
import se.inera.certificate.exception.MissingConsentException;
import se.inera.certificate.integration.rest.ModuleRestApi;
import se.inera.certificate.integration.rest.ModuleRestApiFactory;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.service.CertificateService;

import com.google.common.base.Throwables;

/**
 * @author andreaskaltenbach
 */
public abstract class AbstractGetCertificateResponderImpl {

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractGetCertificateResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private ModuleRestApiFactory moduleRestApiFactory;

    protected CertificateOrResultOfCall getCertificate(String certificateId, String personnummer) {
        if (certificateId == null || certificateId.length() == 0) {
            LOG.info("Tried to get certificate with non-existing ceritificateId '.");
            return new CertificateOrResultOfCall(failResult("Validation error: missing  certificateId"));
        }
        try {
            return new CertificateOrResultOfCall(certificateService.getCertificate(personnummer, certificateId));
        } catch (MissingConsentException ex) {
            // return ERROR if user has not given consent
            LOG.info("Tried to get certificate '" + certificateId + "' but user '" + personnummer
                    + "' has not given consent.");
            return new CertificateOrResultOfCall(failResult(String.format("Missing consent for patient %s",
                    personnummer)));
        } catch (InvalidCertificateException ex) {
            LOG.info("Tried to get certificate '" + certificateId + "' but no such certificate does exist for user '"
                    + personnummer + "'.");
            return new CertificateOrResultOfCall(failResult(String.format("Unknown certificate ID: %s", certificateId)));
        } catch (CertificateRevokedException ex) {
            // return INFO if certificate is revoked
            LOG.info("Tried to get certificate '" + certificateId + "' but certificate has been revoked'.");
            return new CertificateOrResultOfCall((infoResult("Certificate '" + certificateId + "' has been revoked")));
        }
    }

    protected abstract String getMarshallVersion();

    protected Document getCertificateDocument(Certificate certificate) {
        Utlatande utlatande = certificateService.getLakarutlatande(certificate);
        return marshall(certificate, utlatande);
    }

    private Document marshall(Certificate certificate, Utlatande utlatande) {
        ModuleRestApi restApi = moduleRestApiFactory.getModuleRestService(utlatande.getTyp().getCode());
        Response response = restApi.marshall(getMarshallVersion(), certificate.getDocument());

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document document = factory.newDocumentBuilder().parse(new InputSource((InputStream) response.getEntity()));
            response.close();
            return document;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
