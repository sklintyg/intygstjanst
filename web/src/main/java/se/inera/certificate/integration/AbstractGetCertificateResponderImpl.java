package se.inera.certificate.integration;

import static se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType.REVOKED;
import static se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType.VALIDATION_ERROR;
import static se.inera.certificate.integration.util.ResultTypeUtil.errorResult;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import se.inera.certificate.exception.CertificateRevokedException;
import se.inera.certificate.exception.InvalidCertificateException;
import se.inera.certificate.exception.InvalidCertificateIdentifierException;
import se.inera.certificate.exception.MissingConsentException;
import se.inera.certificate.integration.module.ModuleApiFactory;
import se.inera.certificate.logging.LogMarkers;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.api.dto.ExternalModelHolder;
import se.inera.certificate.modules.support.api.dto.TransportModelResponse;
import se.inera.certificate.modules.support.api.dto.TransportModelVersion;
import se.inera.certificate.service.CertificateService;

import com.google.common.base.Throwables;

/**
 * @author andreaskaltenbach
 */
public abstract class AbstractGetCertificateResponderImpl {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractGetCertificateResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private ModuleApiFactory moduleApiFactory;

    /**
     *
     * @param certificateId
     * @param personnummer
     * @return
     */
    protected CertificateOrResultType getCertificate(String certificateId, String personnummer) {
        if (certificateId == null || certificateId.length() == 0) {
            LOGGER.info(LogMarkers.VALIDATION, "Tried to get certificate with non-existing ceritificateId '.");
            return new CertificateOrResultType(errorResult(VALIDATION_ERROR, "Validation error: missing  certificateId"));
        }

        if (personnummer == null || personnummer.length() == 0) {
            LOGGER.info(LogMarkers.VALIDATION, "Tried to get certificate with non-existing personnummer '.");
            return new CertificateOrResultType(errorResult(VALIDATION_ERROR, "Validation error: missing  personnummer"));
        }

        try {
            return new CertificateOrResultType(certificateService.getCertificate(personnummer, certificateId));
        } catch (MissingConsentException ex) {
            // return ERROR if user has not given consent
            LOGGER.info(LogMarkers.MONITORING, "Tried to get certificate '" + certificateId + "' but user '" + personnummer
                    + "' has not given consent.");
            return new CertificateOrResultType(errorResult(VALIDATION_ERROR, String.format("Missing consent for patient %s", personnummer)));
        } catch (InvalidCertificateIdentifierException | InvalidCertificateException ex) {
            LOGGER.info(LogMarkers.MONITORING, "Tried to get certificate '" + certificateId + "' but no such certificate does exist for user '"
                    + personnummer + "'.");
            return new CertificateOrResultType(errorResult(VALIDATION_ERROR, String.format("Unknown certificate ID: %s", certificateId)));
        } catch (CertificateRevokedException ex) {
            // return INFO if certificate is revoked
            LOGGER.info(LogMarkers.MONITORING, "Tried to get certificate '" + certificateId + "' but certificate has been revoked'.");
            return new CertificateOrResultType((errorResult(REVOKED, "Certificate '" + certificateId + "' has been revoked")));
        }
    }

    /**
     * Returns certificate matching specified certificateId.
     * Also returns revoked certificated, it's up to implemented subclass to determine behavior in that case.
     * @param certificateId
     * @return
     */
    protected CertificateOrResultType getCertificate(String certificateId) {
        if (certificateId == null || certificateId.length() == 0) {
            LOGGER.info(LogMarkers.VALIDATION, "Tried to get certificate with non-existing ceritificateId '.");
            return new CertificateOrResultType(errorResult(VALIDATION_ERROR, "Validation error: missing  certificateId"));
        }
        try {
            return new CertificateOrResultType(certificateService.getCertificate(certificateId));
        }  catch (InvalidCertificateException ex) {
            LOGGER.info(LogMarkers.MONITORING, "Tried to get certificate '" + certificateId + "' but no such certificate does exist.");
            return new CertificateOrResultType(errorResult(VALIDATION_ERROR, String.format("Unknown certificate ID: %s", certificateId)));
        }
    }

    protected abstract TransportModelVersion getMarshallVersion();

    protected Document getCertificateDocument(Certificate certificate) {
        Utlatande utlatande = certificateService.getLakarutlatande(certificate);
        return marshall(certificate, utlatande);
    }

    private Document marshall(Certificate certificate, Utlatande utlatande) {
        try {
            ModuleEntryPoint module = moduleApiFactory.getModuleEntryPoint(utlatande.getTyp().getCode());
            TransportModelResponse response = module.getModuleApi().marshall(
                    new ExternalModelHolder(certificate.getDocument()), getMarshallVersion());

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);
            InputSource source = new InputSource();
            source.setCharacterStream(new StringReader(response.getTransportModel()));

            return factory.newDocumentBuilder().parse(source);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
