package se.inera.certificate.integration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.recipientservice.getrecipientsforcertificate.v1.GetRecipientsForCertificateResponseType;
import se.inera.certificate.recipientservice.getrecipientsforcertificate.v1.GetRecipientsForCertificateType;
import se.inera.certificate.recipientservice.getrecipientsforcertificate.v1.RecipientType;
import se.inera.certificate.recipientservice.getrecipientsforcertificateresponder.v1.GetRecipientsForCertificateResponderInterface;
import se.inera.certificate.service.RecipientService;
import se.inera.certificate.service.recipientservice.CertificateType;
import se.inera.certificate.service.recipientservice.Recipient;

public class GetRecipientsForCertificateResponderImpl implements GetRecipientsForCertificateResponderInterface {

    @Autowired
    private RecipientService recipientService;

    @Override
    public GetRecipientsForCertificateResponseType getRecipientsForCertificate(String logicalAddress, GetRecipientsForCertificateType parameters) {
        String certTypeStr = parameters.getCertificateType().trim();
        CertificateType certificateType = new CertificateType(certTypeStr);
        List<Recipient> recipients = recipientService.listRecipients(certificateType);
        GetRecipientsForCertificateResponseType response = new GetRecipientsForCertificateResponseType();
        for (Recipient r : recipients) {
            RecipientType recipientType = new RecipientType();
            recipientType.setId(r.getId());
            recipientType.setName(r.getName());
            response.getRecipient().add(recipientType);
        }
        return response;
    }
}
