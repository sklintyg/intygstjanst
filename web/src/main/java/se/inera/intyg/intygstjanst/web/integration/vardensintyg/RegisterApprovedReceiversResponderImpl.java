/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.clinicalprocess.healthcond.certificate.receiver.types.v1.ApprovalStatusType;
import se.inera.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.inera.clinicalprocess.healthcond.certificate.v3.ResultType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.ReceiverApprovalStatus;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversType;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiver;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.ReceiverService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateRecipientType;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

public class RegisterApprovedReceiversResponderImpl implements RegisterApprovedReceiversResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterApprovedReceiversResponderImpl.class);

    @Autowired
    private ReceiverService receiverService;

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Override
    public RegisterApprovedReceiversResponseType registerApprovedReceivers(String s, RegisterApprovedReceiversType request) {

        LOG.debug("ENTER - registerApprovedReceivers");

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

        if (request.getTypAvIntyg() == null
            || Strings.isNullOrEmpty(request.getTypAvIntyg().getCode())) {
            resultType.setResultCode(ResultCodeType.ERROR);
            resultType.setResultText(
                "Request to registerApprovedReceiver is missing required parameter 'typAvIntyg'");
            resp.setResult(resultType);
            return resp;
        }

        // Check each receiver so it's valid before storing.
        // If there are any problems we break and return error before saving.
        for (ReceiverApprovalStatus receiverApprovalStatus : request.getApprovedReceivers()) {

            if (Strings.isNullOrEmpty(receiverApprovalStatus.getReceiverId())) {
                resultType.setResultCode(ResultCodeType.ERROR);
                resultType.setResultText(
                    "Request to registerApprovedReceiver contained receiverId that was null or empty.");
                resp.setResult(resultType);
                return resp;
            }

            // Make sure the receiverId is present in the recipients.json store.
            try {
                recipientService.getRecipient(receiverApprovalStatus.getReceiverId());
            } catch (RecipientUnknownException e) {
                resultType.setResultCode(ResultCodeType.ERROR);
                resultType.setResultText(
                    "Request to registerApprovedReceiver contained unknown receiverId '" + receiverApprovalStatus.getReceiverId()
                        + "'");
                resp.setResult(resultType);
                return resp;
            }
        }

        // If we're OK down here, remove existing ones and store the new ones
        receiverService.clearApprovedReceiversForCertificate(request.getIntygId().getExtension());

        // Iterate over ALL known receivers. If not explicitly set to YES (or huvudmottagare), set NO.
        CertificateType certType = new CertificateType(request.getTypAvIntyg().getCode());
        List<Recipient> knownRecipients = recipientService.listRecipients(certType);

        for (Recipient knownRecipient : knownRecipients) {
            boolean isIncludedInRequest = false;

            ApprovedReceiver newReceiver = new ApprovedReceiver();
            newReceiver.setCertificateId(request.getIntygId().getExtension());
            newReceiver.setReceiverId(knownRecipient.getId());

            // Find the corresponding item in the request and store YES / NO
            for (ReceiverApprovalStatus receiverApprovalStatus : request.getApprovedReceivers()) {
                if (knownRecipient.getId().equalsIgnoreCase(receiverApprovalStatus.getReceiverId())) {
                    newReceiver.setApproved(receiverApprovalStatus.getApprovalStatus() == ApprovalStatusType.YES);
                    isIncludedInRequest = true;
                    break;
                }
            }

            // If the receiver was NOT included in the request, set YES if huvudmottagare, otherwise NO.
            if (!isIncludedInRequest) {
                newReceiver.setApproved(knownRecipient.getRecipientType() == CertificateRecipientType.HUVUDMOTTAGARE);
            }
            receiverService.registerApprovedReceiver(newReceiver);
        }

        // Null-protect the logging, shouldn't be necessary though..
        if (request.getApprovedReceivers() != null) {
            String receivers = request.getApprovedReceivers().stream()
                .map(ar -> ar.getReceiverId() + ": " + ar.getApprovalStatus().name())
                .collect(Collectors.joining(", "));
            monitoringLogService.logApprovedReceiversRegistered(receivers, request.getIntygId().getExtension());
        }

        resultType.setResultCode(ResultCodeType.OK);
        resultType.setResultText("Successfully registered " + request.getApprovedReceivers().size() + " receivers for certificate '"
            + request.getIntygId().getExtension() + "'");
        resp.setResult(resultType);
        return resp;
    }
}
