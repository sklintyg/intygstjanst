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

package se.inera.intyg.intygstjanst.web.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;

@Service
@RequiredArgsConstructor
public class CertificateEventStatisticsService {

    private final StatisticsService statisticsService;
    private final GetCertificateXmlService getCertificateXmlService;
    private final GetMessageXmlService getMessageXmlService;

    private static final String CERTIFICATE_SIGNED = "certificate-signed";
    private static final String CERTIFICATE_REVOKED = "certificate-revoked";
    private static final String CERTIFICATE_SENT = "certificate-sent";
    private static final String MESSAGE_SENT = "message-sent";

    public boolean send(String eventType, String certificateId, String messageId) {
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
                throw new IllegalArgumentException(String.format("Failure sending statistics for id '%s'. Received unknown event type "
                    + "'%s'.", certificateId, eventType));
        }
    }

    private boolean created(String certificateId) {
        final var response = getCertificateXmlService.get(certificateId);
        final var certificateXml = decodeXml(response.getCertificateXmlBase64());
        return statisticsService.created(certificateXml, certificateId, response.getCertificateType(), response.getUnitId());
    }

    private boolean revoked(String certificateId) {
        final var response = getCertificateXmlService.get(certificateId);
        final var certificateXml = decodeXml(response.getCertificateXmlBase64());
        return statisticsService.revoked(certificateXml, certificateId, response.getCertificateType(), response.getUnitId());
    }

    private boolean sent(String certificateId) {
        final var response = getCertificateXmlService.get(certificateId);
        return statisticsService.sent(certificateId, response.getCertificateType(), response.getUnitId(), response.getRecipient());
    }

    private boolean messageSent(String messageId) {
        final var response = getMessageXmlService.get(messageId);
        final var messageXml = decodeXml(response.getMessageXmlBase64());
        return statisticsService.messageSent(messageXml, messageId, response.getTopic());
    }

    private String decodeXml(String xmlBase64Encoded) {
        return new String(Base64.getDecoder().decode(xmlBase64Encoded), StandardCharsets.UTF_8);
    }
}
