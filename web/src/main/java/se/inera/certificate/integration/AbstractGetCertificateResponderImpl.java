package se.inera.certificate.integration;

import static se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType.REVOKED;
import static se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType.VALIDATION_ERROR;
import static se.inera.certificate.integration.util.ResultTypeUtil.errorResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.exception.CertificateRevokedException;
import se.inera.certificate.exception.InvalidCertificateException;
import se.inera.certificate.exception.MissingConsentException;
import se.inera.certificate.exception.ServerException;
import se.inera.certificate.integration.module.ModuleApiFactory;
import se.inera.certificate.integration.module.exception.ModuleNotFoundException;
import se.inera.certificate.logging.LogMarkers;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.api.dto.ExternalModelHolder;
import se.inera.certificate.modules.support.api.dto.TransportModelResponse;
import se.inera.certificate.modules.support.api.dto.TransportModelVersion;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.certificate.service.CertificateService;

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
    protected CertificateOrResultType getCertificateForCitizen(String certificateId, String personnummer) {
        if (certificateId == null || certificateId.length() == 0) {
            LOGGER.info(LogMarkers.VALIDATION, "Tried to get certificate with non-existing ceritificateId '.");
            return new CertificateOrResultType(errorResult(VALIDATION_ERROR, "Validation error: missing  certificateId"));
        }

        if (personnummer == null || personnummer.length() == 0) {
            LOGGER.info(LogMarkers.VALIDATION, "Tried to get certificate with non-existing personnummer '.");
            return new CertificateOrResultType(errorResult(VALIDATION_ERROR, "Validation error: missing  personnummer"));
        }

        try {
            return new CertificateOrResultType(certificateService.getCertificateForCitizen(personnummer, certificateId));
        } catch (MissingConsentException ex) {
            // return ERROR if user has not given consent
            LOGGER.info(LogMarkers.MONITORING, "Tried to get certificate '" + certificateId + "' but user '" + personnummer
                    + "' has not given consent.");
            return new CertificateOrResultType(errorResult(VALIDATION_ERROR, String.format("Missing consent for patient %s", personnummer)));
        } catch (InvalidCertificateException ex) {
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
    protected CertificateOrResultType getCertificateForCare(String certificateId) {
        if (certificateId == null || certificateId.length() == 0) {
            LOGGER.info(LogMarkers.VALIDATION, "Tried to get certificate with non-existing ceritificateId '.");
            return new CertificateOrResultType(errorResult(VALIDATION_ERROR, "Validation error: missing  certificateId"));
        }
        try {
            return new CertificateOrResultType(certificateService.getCertificateForCare(certificateId));
        }  catch (InvalidCertificateException ex) {
            LOGGER.info(LogMarkers.MONITORING, "Tried to get certificate '" + certificateId + "' but no such certificate does exist.");
            return new CertificateOrResultType(errorResult(VALIDATION_ERROR, String.format("Unknown certificate ID: %s", certificateId)));
        }
    }

    protected abstract TransportModelVersion getMarshallVersion();

    protected String getCertificateDocument(Certificate certificate) {
        return marshallToTransport(certificate.getDocument(), certificate.getType());
    }

    private String marshallToTransport(String externalModel, String utlatandeTyp) {
        try {
            ModuleEntryPoint module = moduleApiFactory.getModuleEntryPoint(utlatandeTyp);
            TransportModelResponse response = module.getModuleApi().marshall(
                    new ExternalModelHolder(externalModel), getMarshallVersion());

            return response.getTransportModel();

        } catch (ModuleNotFoundException | ModuleException e) {
            throw new ServerException("Could not marshall external model to transport model", e);
        }

    }
}
