/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.intygstjanst.web.integration.vardensintyg;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversType;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiver;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.ReceiverService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

public class RegisterApprovedReceiversResponderImpl implements RegisterApprovedReceiversResponderInterface {

    @Autowired
    private ReceiverService receiverService;

    @Autowired
    private RecipientService recipientService;

    @Override
    public RegisterApprovedReceiversResponseType registerApprovedReceivers(String s, RegisterApprovedReceiversType request) {

        RegisterApprovedReceiversResponseType resp = new RegisterApprovedReceiversResponseType();
        ResultType resultType = new ResultType();

        if (request.getIntygId() == null
                || Strings.isNullOrEmpty(request.getIntygId().getExtension())) {
            resultType.setResultCode(ResultCodeType.ERROR);
            resultType.setResultText(
                    "Request to registerApprovedReceiver is missing required parameter 'intygs-id'");
            resp.setResult(resultType);
            return resp;
        }

        // Check each receiver so it's valid before storing. If there are any problems we break and return error
        // before saving.
        for (String receiverId : request.getApprovedReceivers()) {
            if (Strings.isNullOrEmpty(receiverId)) {
                resultType.setResultCode(ResultCodeType.ERROR);
                resultType.setResultText(
                        "Request to registerApprovedReceiver contained receiverId with null or empty receiverId");
                resp.setResult(resultType);
                return resp;
            }

            // Make sure the receiverId is present in the recipients.json store.
            try {
                recipientService.getRecipient(receiverId);
            } catch (RecipientUnknownException e) {
                resultType.setResultCode(ResultCodeType.ERROR);
                resultType.setResultText(
                        "Request to registerApprovedReceiver contained unknown receiverId '" + receiverId + "'");
                resp.setResult(resultType);
                return resp;
            }
        }

        // If we're OK down here, remove existing entires ones and store the new ones
        receiverService.clearApprovedReceiversForCertificate(request.getIntygId().getExtension());

        for (String receiver : request.getApprovedReceivers()) {
            ApprovedReceiver approvedReceiver = new ApprovedReceiver();
            approvedReceiver.setCertificateId(request.getIntygId().getExtension());
            approvedReceiver.setReceiverId(receiver);
            receiverService.registerApprovedReceiver(approvedReceiver);
        }

        resultType.setResultCode(ResultCodeType.OK);
        resultType.setResultText("Successfully registered " + request.getApprovedReceivers().size() + " receivers for certificate '"
                + request.getIntygId().getExtension() + "'");
        resp.setResult(resultType);
        return resp;
    }
}
