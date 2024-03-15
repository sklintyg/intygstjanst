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

import javax.jms.JMSException;
import javax.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.CertificateEventListenerService;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateEventListenerServiceImpl implements CertificateEventListenerService {

    private final CertificateEventStatisticsServiceImpl certificateEventStatsisticsService;
    private final CertificateEventMessageValidatorImpl certificateEventMessageValidator;

    private static final String EVENT_TYPE = "eventType";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String MESSAGE_ID = "messageId";
    private static final String MESSAGE_SENT = "message-sent";
    private static final String ERROR_MESSAGE = "Statistics message delivery failed.";

    @JmsListener(destination = "${certificate.event.queue.name}")
    public void onMessage(Message message) throws JMSException {
        try {
            final var eventType = message.getStringProperty(EVENT_TYPE);
            final var certificateId = message.getStringProperty(CERTIFICATE_ID);
            final var messageId = message.getStringProperty(MESSAGE_ID);
            if (!certificateEventMessageValidator.validate(eventType, certificateId, messageId)) {
                log.error("Failure sending statistics. Missing required parameter");
                return;
            }

            final var success = certificateEventStatsisticsService.send(eventType, certificateId, messageId);

            if (Boolean.TRUE.equals(success)) {
                log.debug("Successfully delivered {} with eventType '{}'.", getLogString(eventType, certificateId, messageId), eventType);
            } else {
                throw new IllegalStateException(String.format("Failure delivering %s with eventType '%s'.",
                    getLogString(eventType, certificateId, messageId), eventType));
            }

        } catch (IllegalArgumentException e) {
            log.error(ERROR_MESSAGE, e);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            throw new JMSException(e.getMessage());
        }
    }

    private String getLogString(String eventType, String certificateId, String messageId) {
        if (eventType.equals(MESSAGE_SENT)) {
            return String.format("message to statistics for message id '%s' from certificate id '%s'", messageId, certificateId);
        }
        return String.format("message to statistics for certificate id '%s'", certificateId);
    }
}
