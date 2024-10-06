/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integration;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listknownrecipients.v1.ListKnownRecipientsResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listknownrecipients.v1.ListKnownRecipientsResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listknownrecipients.v1.ListKnownRecipientsType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listknownrecipients.v1.RecipientType;
import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.v1.utils.ResultTypeUtil;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.web.service.RecipientService;

public class ListKnownRecipientsResponderImpl implements ListKnownRecipientsResponderInterface {

    @Autowired
    private RecipientService recipientService;

    @Override
    @PrometheusTimeMethod
    @PerformanceLogging(eventType = "list-recipients", eventAction = MdcLogConstants.EVENT_TYPE_ACCESSED)
    public ListKnownRecipientsResponseType listKnownRecipients(String logicalAddress, ListKnownRecipientsType request) {
        ListKnownRecipientsResponseType response = new ListKnownRecipientsResponseType();
        List<RecipientType> recipientTypeList = recipientService.listRecipients().stream()
            .map(r -> {
                RecipientType recipientType = new RecipientType();
                recipientType.setId(r.getId());
                recipientType.setName(r.getName());
                recipientType.setTrusted(r.isTrusted());
                return recipientType;
            })
            .collect(Collectors.toList());

        if (recipientTypeList.isEmpty()) {
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, "No recipients found!"));
        } else {
            response.getRecipient().addAll(recipientTypeList);
            response.setResult(ResultTypeUtil.okResult());
        }

        return response;
    }
}
