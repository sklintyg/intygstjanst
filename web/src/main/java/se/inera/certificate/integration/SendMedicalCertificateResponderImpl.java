package se.inera.certificate.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.certificate.exception.RecipientUnknownException;
import se.inera.certificate.integration.module.exception.CertificateRevokedException;
import se.inera.certificate.integration.module.exception.InvalidCertificateException;
import se.inera.certificate.integration.validator.SendCertificateRequestValidator;
import se.inera.certificate.logging.LogMarkers;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.CertificateService.SendStatus;
import se.inera.certificate.service.RecipientService;
import se.inera.certificate.service.recipientservice.CertificateType;
import se.inera.certificate.service.recipientservice.Recipient;
import se.inera.certificate.validate.CertificateValidationException;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.v1.rivtabp20.SendMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.utils.ResultOfCallUtil;

import java.util.List;

public class SendMedicalCertificateResponderImpl implements SendMedicalCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMedicalCertificateResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private RecipientService recipientService;

    @Override
    public SendMedicalCertificateResponseType sendMedicalCertificate(AttributedURIType logicalAddress,
            SendMedicalCertificateRequestType request) {

        SendMedicalCertificateResponseType response = new SendMedicalCertificateResponseType();

        try {
            new SendCertificateRequestValidator(request.getSend()).validateAndCorrect();

            String certificateId = request.getSend().getLakarutlatande().getLakarutlatandeId();
            String civicRegistrationNumber = request.getSend().getLakarutlatande().getPatient().getPersonId().getExtension();

            // Comment 2015-02-13 by Magnus Ekstrand:
            //   Lookup recipient based on the certificate type. This works if and only if a certificate
            //   type only have one recipient. If in a future a certificate type can be sent to multiple
            //   recipients, other mechanisms must be implemented to determine correct recipient.
            Certificate certificate = certificateService.getCertificateForCare(certificateId);
            Recipient recipient = lookupRecipient(certificate);

            // Send certificate to recipient
            SendStatus status = certificateService.sendCertificate(civicRegistrationNumber, certificateId, recipient.getId());

            if (status == SendStatus.ALREADY_SENT) {
                response.setResult(ResultOfCallUtil.infoResult("Certificate '" + certificateId + "' is already sent."));
                LOGGER.info(LogMarkers.MONITORING, certificateId + " already sent to" + logicalAddress.getValue());
            } else {
                response.setResult(ResultOfCallUtil.okResult());
                LOGGER.info(LogMarkers.MONITORING, certificateId + " sent to " + logicalAddress.getValue());
            }

            return response;

        } catch (InvalidCertificateException e) {
            // return with ERROR response if certificate was not found
            LOGGER.info(LogMarkers.MONITORING, "Tried to send certificate '" + safeGetCertificateId(request) + "' for patient '"
                    + safeGetCivicRegistrationNumber(request) + "' but certificate does not exist");
            response.setResult(ResultOfCallUtil.failResult("No certificate '" + safeGetCertificateId(request)
                    + "' found to send for patient '" + safeGetCivicRegistrationNumber(request) + "'."));
            return response;

        } catch (CertificateRevokedException e) {
            // return with INFO response if certificate was revoked before
            LOGGER.info(LogMarkers.MONITORING, "Tried to send certificate '" + safeGetCertificateId(request) + "' for patient '"
                    + safeGetCivicRegistrationNumber(request) + "' which is revoked");
            response.setResult(ResultOfCallUtil.infoResult("Certificate '" + safeGetCertificateId(request) + "' has been revoked."));
            return response;

        } catch (CertificateValidationException e) {
            LOGGER.error(LogMarkers.VALIDATION, "Validation error found for send certificate '" + safeGetCertificateId(request)
                    + "' issued by '" + safeGetIssuedBy(request) + "' for patient '" + safeGetCivicRegistrationNumber(request) + ": " + e.getMessage());
            // return with ERROR response if certificate had validation errors
            response.setResult(ResultOfCallUtil.failResult(e.getMessage()));
            return response;
        } catch (RecipientUnknownException e) {
            LOGGER.error("Unknown recipient");
            response.setResult(ResultOfCallUtil.failResult(e.getMessage()));
            return response;

        }

    }

    private Recipient lookupRecipient(Certificate certificate) throws RecipientUnknownException {
        return lookupRecipient(new CertificateType(certificate.getType()));
    }

    private Recipient lookupRecipient(CertificateType certificateType) throws RecipientUnknownException {

        List<Recipient> recipients = recipientService.listRecipients(certificateType);

        if (recipients.size() == 1) {
            return recipients.get(0);
        }

        String errorMsg = "";

        if (recipients.size() == 0) {
            errorMsg = String.format("No recipient was found for certificate of type %s. Maybe this is a missed configuration.", certificateType.getId());
        }

        if (recipients.size() > 1) {
            errorMsg = String.format("Multiple recipients were found for certificate of type %s. Unable to decide recipient. Maybe this is a missed configuration.", certificateType.getId());
        }

        LOGGER.error(LogMarkers.VALIDATION, errorMsg);
        throw new RecipientUnknownException(errorMsg);
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
