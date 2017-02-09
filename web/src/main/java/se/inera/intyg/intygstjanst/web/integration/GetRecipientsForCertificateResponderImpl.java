/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.RecipientType;
import se.inera.intyg.common.fk7263.schemas.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;

public class GetRecipientsForCertificateResponderImpl implements GetRecipientsForCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetRecipientsForCertificateResponderImpl.class);

    @Autowired
    private RecipientService recipientService;

    @Override
    public GetRecipientsForCertificateResponseType getRecipientsForCertificate(String logicalAddress,
            GetRecipientsForCertificateType request) {
        String certTypeStr = request.getCertificateType().trim();
        CertificateType certificateType = new CertificateType(certTypeStr);
        GetRecipientsForCertificateResponseType response = new GetRecipientsForCertificateResponseType();

        List<Recipient> recipients = recipientService.listRecipients(certificateType);

        for (Recipient r : recipients) {
            RecipientType recipientType = new RecipientType();
            recipientType.setId(r.getId());
            recipientType.setName(r.getName());
            response.getRecipient().add(recipientType);
        }

        if (response.getRecipient().isEmpty()) {
            LOGGER.error("No recipients found for type {}", certTypeStr);
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR,
                    String.format("No recipients found for certificate type: %s", certTypeStr)));
        } else {
            LOGGER.debug("{} recipient(s) found for {}", recipients.size(), certTypeStr);
            response.setResult(ResultTypeUtil.okResult());
        }

        return response;
    }

}
