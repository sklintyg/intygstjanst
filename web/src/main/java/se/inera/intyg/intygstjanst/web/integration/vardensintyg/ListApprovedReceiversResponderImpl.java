/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.clinicalprocess.healthcond.certificate.receiver.types.v1.ApprovalStatusType;
import se.inera.clinicalprocess.healthcond.certificate.receiver.types.v1.CertificateReceiverRegistrationType;
import se.inera.clinicalprocess.healthcond.certificate.receiver.types.v1.CertificateReceiverTypeType;
import se.inera.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listapprovedreceivers.v1.ListApprovedReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listapprovedreceivers.v1.ListApprovedReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listapprovedreceivers.v1.ListApprovedReceiversType;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiver;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiverDao;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateRecipientType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

public class ListApprovedReceiversResponderImpl implements ListApprovedReceiversResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(ListApprovedReceiversResponderImpl.class);

    @Autowired
    private ApprovedReceiverDao approvedReceiverDao;

    @Autowired
    private RecipientService recipientService;

    @Override
    public ListApprovedReceiversResponseType listApprovedReceivers(String s, ListApprovedReceiversType listApprovedReceiversType) {
        if (listApprovedReceiversType.getIntygsId() == null) {
            throw new IllegalArgumentException("Request to ListApprovedReceivers is missing required parameter 'IntygId'");
        }
        IntygId intygsId = listApprovedReceiversType.getIntygsId();
        if (intygsId == null || Strings.isNullOrEmpty(intygsId.getExtension())) {
            throw new IllegalArgumentException("Request to ListApprovedReceivers is missing required parameter 'IntygId.extension'");
        }

        ListApprovedReceiversResponseType response = new ListApprovedReceiversResponseType();

        // Get list of registered possible receivers.
        List<ApprovedReceiver> receivers = approvedReceiverDao.getApprovedReceiverIdsForCertificate(intygsId.getExtension());
        for (ApprovedReceiver receiver : receivers) {
            try {
                Recipient serviceRecipient = recipientService.getRecipient(receiver.getReceiverId());
                CertificateReceiverRegistrationType registrationType = new CertificateReceiverRegistrationType();
                registrationType.setReceiverId(receiver.getReceiverId());
                registrationType.setReceiverName(serviceRecipient.getName());
                registrationType.setReceiverType(toSchemaType(serviceRecipient.getRecipientType()));
                registrationType.setTrusted(serviceRecipient.isTrusted());
                registrationType.setApprovalStatus(receiver.isApproved() ? ApprovalStatusType.YES : ApprovalStatusType.NO);
                response.getReceiverList().add(registrationType);

            } catch (RecipientUnknownException e) {
                LOG.error("RecipientUnknownException when building list of approved receivers for intyg-id '{}'",
                    intygsId.getExtension());
                throw new ServerException(e.getMessage());
            }
        }
        return response;
    }

    private CertificateReceiverTypeType toSchemaType(CertificateRecipientType certificateRecipientType) {
        return CertificateReceiverTypeType.valueOf(certificateRecipientType.name());
    }
}
