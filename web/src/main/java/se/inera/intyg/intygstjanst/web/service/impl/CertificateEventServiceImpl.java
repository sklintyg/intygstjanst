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

package se.inera.intyg.intygstjanst.web.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.csintegration.CSIntegrationService;
import se.inera.intyg.intygstjanst.web.service.CertificateEventRevokeService;
import se.inera.intyg.intygstjanst.web.service.CertificateEventSendMessageService;
import se.inera.intyg.intygstjanst.web.service.CertificateEventSendService;
import se.inera.intyg.intygstjanst.web.service.CertificateEventService;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateEventServiceImpl implements CertificateEventService {

    private final StatisticsService statisticsService;
    private final CSIntegrationService csIntegrationService;
    private final CertificateEventSendService certificateEventSendService;
    private final CertificateEventRevokeService certificateEventRevokeService;
    private final CertificateEventSendMessageService certificateEventSendMessageService;

    private static final String CERTIFICATE_REVOKED = "certificate-revoked";
    private static final String CERTIFICATE_SIGNED = "certificate-signed";
    private static final String CERTIFICATE_SENT = "certificate-sent";
    private static final String MESSAGE_SENT = "message-sent";

    @Override
    public boolean processEvent(String eventType, String certificateId, String messageId) {
        final var metadata = csIntegrationService.getCertificateMetadata(certificateId);

        if (metadata.isTestCertificate()) {
            log.info(String.format(
                    "Not processing event of type '%S' for certificate with id '%s' since it is test indicated", eventType, certificateId
                )
            );
            return true;
        }

        switch (eventType) {
            case CERTIFICATE_SIGNED:
                return created(certificateId);
            case CERTIFICATE_REVOKED:
                return revoked(certificateId);
            case CERTIFICATE_SENT:
                return sent(certificateId);
            case MESSAGE_SENT:
                return messageSent(messageId);
            default:
                throw new IllegalArgumentException(
                    String.format("Invalid eventType '%s' received for certificate '%s' and message '%s'.",
                        eventType, certificateId, messageId));
        }
    }

    private boolean created(String certificateId) {
        final var response = csIntegrationService.getCertificateXmlResponse(certificateId);
        final var certificateXml = decodeXml(response.getXml());
        return statisticsService.created(certificateXml, certificateId, response.getCertificateType(), response.getUnit().getUnitId());
    }

    private boolean revoked(String certificateId) {
        final var response = csIntegrationService.getCertificateXmlResponse(certificateId);
        final var certificateXml = decodeXml(response.getXml());
        if (response.getRecipient() != null && response.getRecipient().getSent() != null) {
            certificateEventRevokeService.revoke(response);
        }
        return statisticsService.revoked(certificateXml, certificateId, response.getCertificateType(), response.getUnit().getUnitId());
    }

    private boolean sent(String certificateId) {
        final var response = csIntegrationService.getCertificateXmlResponse(certificateId);
        certificateEventSendService.send(response, decodeXml(response.getXml()));
        return statisticsService.sent(certificateId, response.getCertificateType(), response.getUnit().getUnitId(),
            response.getRecipient().getId());
    }

    private boolean messageSent(String messageId) {
        final var response = csIntegrationService.getMessageXmlResponse(messageId);
        final var messageXml = decodeXml(response.getXml());
        certificateEventSendMessageService.sendMessage(messageXml);
        return true;
    }

    private String decodeXml(String xmlBase64Encoded) {
        return new String(Base64.getDecoder().decode(xmlBase64Encoded), StandardCharsets.UTF_8);
    }
}
