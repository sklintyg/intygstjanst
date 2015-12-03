package se.inera.intyg.intygstjanst.web.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.RecipientType;
import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;

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
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, String.format("No recipients found for certificate type: %s", certTypeStr)));
        } else {
            response.setResult(ResultTypeUtil.okResult());
        }

        return response;
    }

}
