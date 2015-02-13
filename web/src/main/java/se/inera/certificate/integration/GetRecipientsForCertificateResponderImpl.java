package se.inera.certificate.integration;

import static se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType.APPLICATION_ERROR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.RecipientType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.inera.certificate.exception.RecipientUnknownException;
import se.inera.certificate.service.RecipientService;
import se.inera.certificate.service.recipientservice.CertificateType;
import se.inera.certificate.service.recipientservice.Recipient;

import java.util.ArrayList;
import java.util.List;

public class GetRecipientsForCertificateResponderImpl implements GetRecipientsForCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetRecipientsForCertificateResponderImpl.class);

    @Autowired
    private RecipientService recipientService;

    @Override
    public GetRecipientsForCertificateResponseType getRecipientsForCertificate(String logicalAddress, GetRecipientsForCertificateType request) {
        String certTypeStr = request.getCertificateType().trim();
        CertificateType certificateType = new CertificateType(certTypeStr);
        GetRecipientsForCertificateResponseType response = new GetRecipientsForCertificateResponseType();
        List<Recipient> recipients = new ArrayList<Recipient>();

        try {
            recipients = recipientService.listRecipients(certificateType);
        } catch (RecipientUnknownException rue) {
            LOGGER.error(rue.getMessage());
        }

        for (Recipient r : recipients) {
            RecipientType recipientType = new RecipientType();
            recipientType.setId(r.getId());
            recipientType.setName(r.getName());
            response.getRecipient().add(recipientType);
        }

        if (response.getRecipient().isEmpty()) {
            response.setResult(ResultTypeUtil.errorResult(APPLICATION_ERROR, String.format("No recipients found for certificate type: %s", certTypeStr)));
        } else {
            response.setResult(ResultTypeUtil.okResult());
        }

        return response;
    }

}
