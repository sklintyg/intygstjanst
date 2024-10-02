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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v11.GetRecipientsForCertificateResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v11.GetRecipientsForCertificateResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v11.GetRecipientsForCertificateType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v11.RecipientType;
import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.v1.utils.ResultTypeUtil;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateRecipientType;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateTypeInfo;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

public class GetRecipientsForCertificateResponderImpl implements GetRecipientsForCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetRecipientsForCertificateResponderImpl.class);

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private CertificateService certificateService;

    @Override
    @PrometheusTimeMethod
    public GetRecipientsForCertificateResponseType getRecipientsForCertificate(String logicalAddress,
        GetRecipientsForCertificateType request) {

        GetRecipientsForCertificateResponseType response = new GetRecipientsForCertificateResponseType();

        String intygsId = request.getCertificateId().trim();
        List<Recipient> recipientList = getRecipientList(logicalAddress, intygsId);

        if (recipientList.isEmpty()) {
            LOGGER.error("No recipients found for certificate {}", intygsId);
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR,
                String.format("No recipients found for certificate id: %s", intygsId)));
        } else {
            response.getRecipient().addAll(getRecipientTypeList(recipientList));

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

    private List<RecipientType> getRecipientTypeList(List<Recipient> recipientList) {
        return recipientList.stream()
            .map(r -> {
                return createRecipientType(r);
            })
            .collect(Collectors.toList());
    }

    private List<Recipient> getRecipientList(String logicalAddress, String intygsId) {
        List<Recipient> recipientList = new ArrayList<>();

        try {
            // Get approved recipients
            recipientList = recipientService.listRecipients(intygsId).stream()
                .filter(Recipient::isActive).collect(Collectors.toList());

            // There might be zero recipients...
            // Then get the main receiver for this certificate's type
            if (recipientList.size() == 0) {
                String intygsTyp = getIntygsTyp(intygsId);
                if (StringUtils.isNotBlank(intygsTyp)) {
                    CertificateType certificateType = new CertificateType(intygsTyp);
                    recipientList = recipientService.listRecipients(certificateType).stream()
                        .filter(r -> r.getRecipientType().equals(CertificateRecipientType.HUVUDMOTTAGARE))
                        .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return recipientList;
    }

    private String getIntygsTyp(String intygsId) {
        final CertificateTypeInfo certificateTypeInfo = certificateService.getCertificateTypeInfo(intygsId);
        if (certificateTypeInfo != null && certificateTypeInfo.getTypAvIntyg() != null) {
            return certificateTypeInfo.getTypAvIntyg().getCode();
        }

        LOGGER.error("Failed to get certificate's type. Certificate with id {} is invalid or does not exist", intygsId);
        return null;
    }

}
