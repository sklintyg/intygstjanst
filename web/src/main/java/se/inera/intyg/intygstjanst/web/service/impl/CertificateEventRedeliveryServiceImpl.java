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

import javax.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ScheduledMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.CertificateEventRedeliveryService;

@Service
@Slf4j
public class CertificateEventRedeliveryServiceImpl implements CertificateEventRedeliveryService {

    private static final int MAX_REDELIVERIES = 5;
    private static final long ONE_MINUTE = 60000L;
    private static final long FIVE_MINUTES = ONE_MINUTE * 5;
    private static final long THIRTY_MINUTES = ONE_MINUTE * 30;
    private static final long ONE_HOUR = ONE_MINUTE * 60;

    private static final String EVENT_TYPE = "eventType";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String MESSAGE_ID = "messageId";
    private static final String REDELIVERIES = "redeliveries";

    private final JmsTemplate jmsCertificateEventTemplate;

    public CertificateEventRedeliveryServiceImpl(@Qualifier("jmsCertificateEventTemplate") JmsTemplate jmsCertificateEventTemplate) {
        this.jmsCertificateEventTemplate = jmsCertificateEventTemplate;
    }

    @Override
    public void resend(Message message, String eventType, String certificateId, String messageId) {

        try {
            int redeliveries = message.propertyExists(REDELIVERIES) ? message.getIntProperty(REDELIVERIES) + 1 : 1;

            if (redeliveries > MAX_REDELIVERIES) {
                log.error("Certificate event handler failure after {} redeliveries for event type '{}', certificate '{}' and message '{}'.",
                    MAX_REDELIVERIES, eventType, certificateId, messageId);
                return;
            }

            final var redeliveryDelay = getRedeliveryDelay(redeliveries);
            send(message, eventType, certificateId, messageId, redeliveries, redeliveryDelay);

        } catch (Exception e) {
            log.error("Failure creating redelivery for certificate event with event type '{}', certificate '{}' and message '{}'.",
                eventType, certificateId, messageId);
        }
    }

    private void send(Message message, String eventType, String certificateId, String messageId, int redeliveries, Long redeliveryDelay) {
        jmsCertificateEventTemplate.send(session -> {
            final var textMessage = session.createTextMessage("");
            textMessage.setStringProperty(EVENT_TYPE, eventType);
            textMessage.setStringProperty(CERTIFICATE_ID, certificateId);
            if (message.propertyExists(MESSAGE_ID)) {
                textMessage.setStringProperty(MESSAGE_ID, messageId);
            }
            textMessage.setIntProperty(REDELIVERIES, redeliveries);
            textMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, redeliveryDelay);
            return textMessage;
        });
    }

    private Long getRedeliveryDelay(int redeliveries) {
        switch (redeliveries) {
            case 1:
                return ONE_MINUTE;
            case 2:
                return FIVE_MINUTES;
            case 3:
                return THIRTY_MINUTES;
            default:
                return ONE_HOUR;
        }
    }
}
