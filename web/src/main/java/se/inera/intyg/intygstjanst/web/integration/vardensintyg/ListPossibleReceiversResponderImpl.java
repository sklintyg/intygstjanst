/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.clinicalprocess.healthcond.certificate.receiver.types.v1.CertificateReceiverType;
import se.inera.clinicalprocess.healthcond.certificate.receiver.types.v1.CertificateReceiverTypeType;
import se.inera.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversType;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateRecipientType;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

public class ListPossibleReceiversResponderImpl implements ListPossibleReceiversResponderInterface {

    @Autowired
    private RecipientService recipientService;

    @Override
    public ListPossibleReceiversResponseType listPossibleReceivers(String s, ListPossibleReceiversType listPossibleReceiversType) {
        TypAvIntyg intygTyp = listPossibleReceiversType.getIntygTyp();
        if (intygTyp == null || Strings.isNullOrEmpty(intygTyp.getCode())) {
            throw new IllegalArgumentException("Request to ListPossibleReceivers is missing required parameter 'intygTyp'");
        }

        CertificateType certificateType = new CertificateType(intygTyp.getCode());
        List<Recipient> recipients = recipientService.listRecipients(certificateType);

        ListPossibleReceiversResponseType response = new ListPossibleReceiversResponseType();
        for (Recipient possibleRecipient : recipients) {

            CertificateReceiverType certificateReceiverType = new CertificateReceiverType();
            certificateReceiverType.setReceiverId(possibleRecipient.getId());
            certificateReceiverType.setReceiverName(possibleRecipient.getName());
            certificateReceiverType.setReceiverType(toSchemaType(possibleRecipient.getRecipientType()));
            certificateReceiverType.setTrusted(possibleRecipient.isTrusted());
            response.getReceiverList().add(certificateReceiverType);
        }
        return response;
    }

    private CertificateReceiverTypeType toSchemaType(CertificateRecipientType certificateRecipientType) {
        return CertificateReceiverTypeType.valueOf(certificateRecipientType.name());
    }
}
