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
package se.inera.intyg.intygstjanst.web.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v11.GetRecipientsForCertificateResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v11.GetRecipientsForCertificateResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v11.GetRecipientsForCertificateType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v11.RecipientType;
import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.v1.utils.ResultTypeUtil;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;

import java.util.List;
import java.util.stream.Collectors;

public class GetRecipientsForCertificateResponderImpl implements GetRecipientsForCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetRecipientsForCertificateResponderImpl.class);

    @Autowired
    private RecipientService recipientService;

    @Override
    @PrometheusTimeMethod
    public GetRecipientsForCertificateResponseType getRecipientsForCertificate(String logicalAddress,
            GetRecipientsForCertificateType request) {

        String intygsId = request.getCertificateId().trim();

        List<Recipient> recipientList = recipientService.listRecipients(intygsId);

        List<RecipientType> filteredList = recipientList.stream()
                .filter(Recipient::isActive)
                .map(r -> {
                    return createRecipientType(r);
                })
                .collect(Collectors.toList());

        GetRecipientsForCertificateResponseType response = new GetRecipientsForCertificateResponseType();
        response.getRecipient().addAll(filteredList);

        if (response.getRecipient().isEmpty()) {
            LOGGER.error("No recipients found for certificate {}", intygsId);
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR,
                    String.format("No recipients found for certificate id: %s", intygsId)));
        } else {
            LOGGER.debug("{} recipient(s) found for {}", response.getRecipient().size(), intygsId);
            response.setResult(ResultTypeUtil.okResult());
        }

        return response;
    }

    private RecipientType createRecipientType(Recipient recipient) {
        RecipientType recipientType = new RecipientType();
        recipientType.setId(recipient.getId());
        recipientType.setName(recipient.getName());
        recipientType.setTrusted(recipient.isTrusted());
        recipientType.setType(recipient.getRecipientType().name());

        return recipientType;
    }

}
