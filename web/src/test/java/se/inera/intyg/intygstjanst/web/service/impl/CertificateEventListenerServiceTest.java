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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import javax.jms.JMSException;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CertificateEventListenerServiceTest {

    @Mock
    private CertificateEventStatisticsService certificateEventStatisticsService;
    @Mock
    private CertificateEventMessageValidator certificateEventMessageValidator;

    @InjectMocks
    private CertificateEventListenerService certificateEventListenerService;

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String MESSAGE_ID = "messageId";
    private static final String EVENT_TYPE = "eventType";
    private static final String EVENT_SIGNED = "certificate-signed";
    private static final String EVENT_MESSAGE_SENT = "message-sent";

    @Test
    void shouldExitIfInputPropertiesDoNotValidate() throws JMSException {
        final var message = new ActiveMQTextMessage();
        certificateEventListenerService.onMessage(message);
        verifyNoInteractions(certificateEventStatisticsService);
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenFalseResponseFromService() throws JMSException {
        final var message = new ActiveMQTextMessage();
        message.setStringProperty(EVENT_TYPE, EVENT_SIGNED);
        message.setStringProperty(CERTIFICATE_ID, CERTIFICATE_ID);

        when(certificateEventMessageValidator.validate(anyString(), anyString(), nullable(String.class))).thenReturn(true);
        when(certificateEventStatisticsService.send(anyString(), anyString(), nullable(String.class))).thenReturn(false);

        assertThrows(JMSException.class, () -> certificateEventListenerService.onMessage(message));
    }

    @Test
    void shouldThrowRestClientExceptionWhenFalseResponseFromService() throws JMSException {
        final var message = new ActiveMQTextMessage();
        message.setStringProperty(EVENT_TYPE, EVENT_SIGNED);
        message.setStringProperty(CERTIFICATE_ID, CERTIFICATE_ID);

        when(certificateEventMessageValidator.validate(anyString(), anyString(), nullable(String.class))).thenReturn(true);
        when(certificateEventStatisticsService.send(anyString(), anyString(), nullable(String.class))).thenReturn(false);

        assertThrows(JMSException.class, () -> certificateEventListenerService.onMessage(message));
    }

    @Test
    void shouldNotThrowExceptionIfIlllegalArgumentExceptionFromService() throws JMSException {
        final var message = new ActiveMQTextMessage();
        message.setStringProperty(EVENT_TYPE, EVENT_SIGNED);
        message.setStringProperty(CERTIFICATE_ID, CERTIFICATE_ID);
        message.setStringProperty(MESSAGE_ID, MESSAGE_ID);

        when(certificateEventMessageValidator.validate(anyString(), anyString(), nullable(String.class))).thenReturn(true);
        when(certificateEventStatisticsService.send(anyString(), anyString(), nullable(String.class)))
            .thenThrow(IllegalArgumentException.class);

        assertDoesNotThrow(() -> certificateEventListenerService.onMessage(message));
    }

    @Test
    void shouldNotThrowExceptionIfSuccessfulMessageDelivery() throws JMSException {
        final var message = new ActiveMQTextMessage();
        message.setStringProperty(EVENT_TYPE, EVENT_MESSAGE_SENT);
        message.setStringProperty(CERTIFICATE_ID, CERTIFICATE_ID);

        when(certificateEventMessageValidator.validate(anyString(), anyString(), nullable(String.class))).thenReturn(true);
        when(certificateEventStatisticsService.send(anyString(), anyString(), nullable(String.class))).thenReturn(true);

        assertDoesNotThrow(() -> certificateEventListenerService.onMessage(message));
    }
}
