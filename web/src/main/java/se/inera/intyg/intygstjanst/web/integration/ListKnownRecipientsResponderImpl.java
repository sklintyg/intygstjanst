package se.inera.intyg.intygstjanst.web.integration;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listknownrecipients.v1.ListKnownRecipientsResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listknownrecipients.v1.ListKnownRecipientsResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listknownrecipients.v1.ListKnownRecipientsType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listknownrecipients.v1.RecipientType;
import se.inera.intyg.common.fk7263.schemas.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;

import java.util.ArrayList;
import java.util.List;

public class ListKnownRecipientsResponderImpl implements ListKnownRecipientsResponderInterface {

    @Autowired
    private RecipientService recipientService;

    @Override
    public ListKnownRecipientsResponseType listKnownRecipients(String logicalAddress, ListKnownRecipientsType request) {
        ListKnownRecipientsResponseType response = new ListKnownRecipientsResponseType();
        List<RecipientType> recipientTypeList = new ArrayList<>();

        recipientService.listRecipients().forEach(r -> {
            RecipientType recipientType = new RecipientType();
            recipientType.setId(r.getId());
            recipientType.setName(r.getName());
            recipientTypeList.add(recipientType);
        });

        if (recipientTypeList.isEmpty()) {
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, "No recipients found!"));
        } else {
            response.getRecipient().addAll(recipientTypeList);
            response.setResult(ResultTypeUtil.okResult());
        }

        return response;
    }
}
