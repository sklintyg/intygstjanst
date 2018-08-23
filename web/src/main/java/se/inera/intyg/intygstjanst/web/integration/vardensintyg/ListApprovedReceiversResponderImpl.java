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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listapprovedreceivers.v1.ListApprovedReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listapprovedreceivers.v1.ListApprovedReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listapprovedreceivers.v1.ListApprovedReceiversType;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiverDao;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateRecipientType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.riv.clinicalprocess.healthcond.certificate.receiver.types.v1.CertificateReceiverType;
import se.riv.clinicalprocess.healthcond.certificate.receiver.types.v1.CertificateReceiverTypeType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;

import java.util.List;

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

        List<String> approvedReceiverIds = approvedReceiverDao.getApprovedReceiverIdsForCertificate(intygsId.getExtension());

        ListApprovedReceiversResponseType response = new ListApprovedReceiversResponseType();
        for (String allowedRecipientId : approvedReceiverIds) {

            try {
                Recipient serviceRecipient = recipientService.getRecipient(allowedRecipientId);
                CertificateReceiverType recipient = new CertificateReceiverType();
                recipient.setReceiverId(allowedRecipientId);
                recipient.setReceiverName(serviceRecipient.getName());
                recipient.setReceiverType(toSchemaType(serviceRecipient.getRecipientType()));
                recipient.setTrusted(serviceRecipient.isTrusted());
                response.getReceiverList().add(recipient);
            } catch (RecipientUnknownException e) {
                String errMsg = "Certificate with id '" + intygsId.getExtension() + "' specified approved recipient of type '"
                        + allowedRecipientId + "' which is not known.";
                LOG.error(errMsg);
                throw new ServerException(errMsg);
            }
        }
        return response;
    }

    private CertificateReceiverTypeType toSchemaType(CertificateRecipientType certificateRecipientType) {
        return CertificateReceiverTypeType.valueOf(certificateRecipientType.name());
    }
}
