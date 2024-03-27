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
import se.inera.intyg.intygstjanst.web.service.CertificateEventService;
import se.inera.intyg.intygstjanst.web.service.CertificateEventValidator;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateEventListenerServiceImpl implements CertificateEventListenerService {

    private final CertificateEventService certificateEventStatsisticsService;
    private final CertificateEventValidator certificateEventMessageValidator;

    private static final String EVENT_TYPE = "eventType";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String MESSAGE_ID = "messageId";
    private static final String MESSAGE_SENT = "message-sent";
    private static final String ERROR_MESSAGE = "Failure handling certificate event with eventType '{}', certificateId '{}' and "
        + "messageId '{}'.";

    @Override
    @JmsListener(destination = "${certificate.event.queue.name}")
    public void processMessage(Message message) {
        String eventType = null;
        String certificateId = null;
        String messageId = null;

        try {
            eventType = message.getStringProperty(EVENT_TYPE);
            certificateId = message.getStringProperty(CERTIFICATE_ID);
            messageId = message.getStringProperty(MESSAGE_ID);

            if (!certificateEventMessageValidator.validate(eventType, certificateId, messageId)) {
                log.error(getMissingParametersMessage(eventType, certificateId, messageId));
                return;
            }

            final var success = certificateEventStatsisticsService.send(eventType, certificateId, messageId);

            if (!success) {
                throw new IllegalStateException(getSendStatisticsFailedMessage(eventType, certificateId, messageId));
            }

        } catch (JMSException | IllegalArgumentException e) {
            log.error(ERROR_MESSAGE, eventType, certificateId, messageId, e);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, eventType, certificateId, messageId, e);
            throw e;
        }
    }

    private String getMissingParametersMessage(String eventType, String certificateId, String messageId) {
        return String.format("Received certificate event missing required parameter(s), eventType: '%s', certificateId: '%s', "
            + "messageId: '%s'.", eventType, certificateId, messageId);
    }
    private String getSendStatisticsFailedMessage(String eventType, String certificateId, String messageId) {
        return String.format("Failure sending statistics for event type '%s' for certificate '%s' with message '%s'.",
            eventType, certificateId, messageId);
    }
}
