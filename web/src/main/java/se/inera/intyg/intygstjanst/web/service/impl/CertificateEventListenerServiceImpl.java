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

import static se.inera.intyg.intygstjanst.logging.MdcLogConstants.EVENT_CERTIFICATE_ID;
import static se.inera.intyg.intygstjanst.logging.MdcLogConstants.EVENT_MESSAGE_ID;
import static se.inera.intyg.intygstjanst.logging.MdcLogConstants.SPAN_ID_KEY;
import static se.inera.intyg.intygstjanst.logging.MdcLogConstants.TRACE_ID_KEY;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.logging.MdcHelper;
import se.inera.intyg.intygstjanst.web.service.CertificateEventListenerService;
import se.inera.intyg.intygstjanst.web.service.CertificateEventService;
import se.inera.intyg.intygstjanst.web.service.CertificateEventValidator;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateEventListenerServiceImpl implements CertificateEventListenerService {

    private final CertificateEventService certificateEventService;
    private final CertificateEventValidator certificateEventMessageValidator;
    private final MdcHelper mdcHelper;

    private static final String EVENT_TYPE = "eventType";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String MESSAGE_ID = "messageId";
    private static final String ERROR_MESSAGE = "Failure processing certificate event '{}', for certificateId '{}' and messageId '{}'.";

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

            MDC.put(TRACE_ID_KEY, mdcHelper.traceId());
            MDC.put(SPAN_ID_KEY, mdcHelper.spanId());
            MDC.put(EVENT_CERTIFICATE_ID, certificateId);
            MDC.put(EVENT_MESSAGE_ID, messageId);
            MDC.put(EVENT_TYPE, eventType);

            if (!certificateEventMessageValidator.validate(eventType, certificateId, messageId)) {
                log.error(getMissingParametersMessage(eventType, certificateId, messageId));
                return;
            }

            final var success = certificateEventService.processEvent(eventType, certificateId, messageId);

            if (!success) {
                throw new IllegalStateException(getProcessEventFailedMessage(eventType, certificateId, messageId));
            }

        } catch (JMSException | IllegalArgumentException e) {
            log.error(ERROR_MESSAGE, eventType, certificateId, messageId, e);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, eventType, certificateId, messageId, e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    private String getMissingParametersMessage(String eventType, String certificateId, String messageId) {
        return String.format("Certificate event missing required parameter(s), got eventType '%s', certificateId '%s' and messageId '%s'.",
            eventType, certificateId, messageId);
    }
    private String getProcessEventFailedMessage(String eventType, String certificateId, String messageId) {
        return String.format("Failure processing event '%s' for certificateId '%s' and messageId '%s'.", eventType, certificateId,
            messageId);
    }
}
