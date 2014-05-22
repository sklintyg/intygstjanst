package se.inera.certificate.integration;

import static se.inera.certificate.integration.util.ResultOfCallUtil.failResult;
import static se.inera.certificate.integration.util.ResultOfCallUtil.infoResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.exception.CertificateRevokedException;
import se.inera.certificate.exception.InvalidCertificateException;
import se.inera.certificate.exception.InvalidCertificateIdentifierException;
import se.inera.certificate.integration.util.ResultOfCallUtil;
import se.inera.certificate.integration.validator.SendCertificateRequestValidator;
import se.inera.certificate.integration.validator.ValidationException;
import se.inera.certificate.logging.LogMarkers;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.CertificateService.SendStatus;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.v1.rivtabp20.SendMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType;

public class SendMedicalCertificateResponderImpl implements SendMedicalCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMedicalCertificateResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Override
    public SendMedicalCertificateResponseType sendMedicalCertificate(AttributedURIType logicalAddress,
            SendMedicalCertificateRequestType request) {
        SendMedicalCertificateResponseType response = new SendMedicalCertificateResponseType();

        try {
            new SendCertificateRequestValidator(request.getSend()).validateAndCorrect();

            String certificateId = request.getSend().getLakarutlatande().getLakarutlatandeId();
            String civicRegistrationNumber = request.getSend().getLakarutlatande().getPatient().getPersonId()
                    .getExtension();

            SendStatus status = certificateService.sendCertificate(civicRegistrationNumber, certificateId, logicalAddress.getValue());

            if (status == SendStatus.ALREADY_SENT) {
                response.setResult(ResultOfCallUtil.infoResult("Certificate '" + certificateId + "' is already sent."));
                LOGGER.info(LogMarkers.MONITORING, certificateId + " already sent to FK");
            } else {
                response.setResult(ResultOfCallUtil.okResult());
                LOGGER.info(LogMarkers.MONITORING, certificateId + " sent to FK");
            }
            return response;
        } catch (InvalidCertificateIdentifierException | InvalidCertificateException e) {
            // return with ERROR response if certificate was not found
            LOGGER.info(LogMarkers.MONITORING, "Tried to send certificate '" + safeGetCertificateId(request) + "' for patient '"
                    + safeGetCivicRegistrationNumber(request) + "' but certificate does not exist");
            response.setResult(failResult("No certificate '" + safeGetCertificateId(request)
                    + "' found to send for patient '" + safeGetCivicRegistrationNumber(request) + "'."));
            return response;
        } catch (CertificateRevokedException e) {
            // return with INFO response if certificate was revoked before
            LOGGER.info(LogMarkers.MONITORING, "Tried to send certificate '" + safeGetCertificateId(request) + "' for patient '"
                    + safeGetCivicRegistrationNumber(request) + "' which is revoked");
            response.setResult(infoResult("Certificate '" + safeGetCertificateId(request) + "' has been revoked."));
            return response;
        } catch (ValidationException e) {
            LOGGER.error(LogMarkers.VALIDATION, "Validation error found for send certificate '" + safeGetCertificateId(request)
                    + "' issued by '" + safeGetIssuedBy(request) + "' for patient '" + safeGetCivicRegistrationNumber(request) + ": " + e.getMessage());
            // return with ERROR response if certificate had validation errors
            response.setResult(failResult(e.getMessage()));
            return response;
        }

    }

    private String safeGetCertificateId(SendMedicalCertificateRequestType request) {
        // Initialize log context info if available
        if (request.getSend() != null && request.getSend().getLakarutlatande() != null
                && request.getSend().getLakarutlatande().getLakarutlatandeId() != null) {
            return request.getSend().getLakarutlatande().getLakarutlatandeId();
        }
        return null;
    }

    private String safeGetCivicRegistrationNumber(SendMedicalCertificateRequestType request) {
        // Initialize log context info if available
        if (request.getSend().getLakarutlatande().getPatient() != null
                && request.getSend().getLakarutlatande().getPatient().getPersonId() != null) {
            return request.getSend().getLakarutlatande().getPatient().getPersonId().getExtension();
        }
        return null;
    }

    private String safeGetIssuedBy(SendMedicalCertificateRequestType request) {
        // Initialize log context info if available
        if (request.getSend().getAdressVard() != null
                && request.getSend().getAdressVard().getHosPersonal() != null
                && request.getSend().getAdressVard().getHosPersonal().getEnhet() != null
                && request.getSend().getAdressVard().getHosPersonal().getEnhet().getEnhetsId() != null) {
            return request.getSend().getAdressVard().getHosPersonal().getEnhet().getEnhetsId().getExtension();
        }
        return null;
    }
}
