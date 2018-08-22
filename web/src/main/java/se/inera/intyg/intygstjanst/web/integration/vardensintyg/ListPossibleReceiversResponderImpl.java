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
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversType;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.riv.clinicalprocess.healthcond.certificate.receiver.types.v1.CertificateReceiverType;
import se.riv.clinicalprocess.healthcond.certificate.receiver.types.v1.CertificateReceiverTypeType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;

import java.util.List;

// @SchemaValidation
public class ListPossibleReceiversResponderImpl implements ListPossibleReceiversResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(ListPossibleReceiversResponderImpl.class);

    @Autowired
    private IntygModuleRegistry intygModuleRegistry;

    @Autowired
    private RecipientService recipientService;

    @Override
    public ListPossibleReceiversResponseType listPossibleReceivers(String s, ListPossibleReceiversType listPossibleReceiversType) {
        TypAvIntyg intygTyp = listPossibleReceiversType.getIntygTyp();
        if (intygTyp == null || Strings.isNullOrEmpty(intygTyp.getCode())) {
            throw new IllegalArgumentException("Request to ListPossibleReceivers is missing required parameter 'intygTyp'");
        }

        String huvudmottagareCode = findHuvudmottagare(intygTyp);

        CertificateType certificateType = new CertificateType(intygTyp.getCode());
        List<Recipient> recipients = recipientService.listRecipients(certificateType);

        ListPossibleReceiversResponseType response = new ListPossibleReceiversResponseType();
        for (Recipient possibleRecipient : recipients) {

            CertificateReceiverType certificateReceiverType = new CertificateReceiverType();
            certificateReceiverType.setReceiverId(possibleRecipient.getId());
            certificateReceiverType.setReceiverName(possibleRecipient.getName());
            certificateReceiverType.setReceiverType(resolveMottagarTyp(huvudmottagareCode, possibleRecipient.getId()));
            certificateReceiverType.setTrusted(possibleRecipient.isTrusted());
            response.getRecipient().add(certificateReceiverType);
        }
        return response;
    }

    private CertificateReceiverTypeType resolveMottagarTyp(String huvudmottagareCode, String mottagarId) {
        return huvudmottagareCode.equalsIgnoreCase(mottagarId) ? CertificateReceiverTypeType.HUVUDMOTTAGARE
                : CertificateReceiverTypeType.ANNAN_MOTTAGARE;
    }

    private String findHuvudmottagare(TypAvIntyg intygTyp) {
        try {
            ModuleEntryPoint moduleEntryPoint = intygModuleRegistry.getModuleEntryPoint(intygTyp.getCode());
            return moduleEntryPoint.getDefaultRecipient();
        } catch (ModuleNotFoundException e) {
            throw new IllegalArgumentException("No default receiver (huvudmottagare) found for intygtyp '" + intygTyp.getCode() + "'");
        }
    }
}
