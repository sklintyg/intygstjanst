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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;
import se.inera.intyg.intygstjanst.web.service.dto.GetCertificateXmlResponse;
import se.inera.intyg.intygstjanst.web.service.dto.GetMessageXmlResponse;

@ExtendWith(MockitoExtension.class)
class CertificateEventStatisticsServiceImplTest {

    @Mock
    private StatisticsService statisticsService;
    @Mock
    private GetCertificateXmlServiceImpl getCertificateXmlService;
    @Mock
    private GetMessageXmlServiceImpl getMessageXmlService;

    @InjectMocks
    private CertificateEventStatisticsServiceImpl certificateEventStatisticsServiceImpl;

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "fk7211";
    private static final String EVENT_SIGNED = "certificate-signed";
    private static final String EVENT_REVOKED = "certificate-revoked";
    private static final String EVENT_SENT = "certificate-sent";
    private static final String EVENT_MESSAGE_SENT = "message-sent";
    private static final String EVENT_UNKNOWN = "unknown";
    private static final String UNIT_ID = "unitId";
    private static final String MESSAGE_ID = "messageId";
    private static final String RECIPIENT = "recipient";
    private static final String TOPIC = "topic";
    private static final String DECODED_XML = "xmlFromCertificateService";
    private static final String ENCODED_XML = Base64.getEncoder().encodeToString("xmlFromCertificateService"
        .getBytes(StandardCharsets.UTF_8));

    @Test
    void shouldCallStatisticsServiceCreated() {
        final var resp = GetCertificateXmlResponse.builder()
            .certificateId(CERTIFICATE_ID)
            .certificateType(CERTIFICATE_TYPE)
            .unitId(UNIT_ID)
            .recipient(null)
            .xml(ENCODED_XML)
            .build();
        when(getCertificateXmlService.get(CERTIFICATE_ID)).thenReturn(resp);
        when(statisticsService.created(DECODED_XML, CERTIFICATE_ID, CERTIFICATE_TYPE, UNIT_ID)).thenReturn(true);

        final var result = certificateEventStatisticsServiceImpl.send(EVENT_SIGNED, CERTIFICATE_ID, MESSAGE_ID);

        assertTrue(result);
        verify(statisticsService).created(DECODED_XML, resp.getCertificateId(), resp.getCertificateType(), resp.getUnitId());
    }

    @Test
    void shouldCallStatisticsServiceRevoked() {
        final var resp = GetCertificateXmlResponse.builder()
            .certificateId(CERTIFICATE_ID)
            .certificateType(CERTIFICATE_TYPE)
            .unitId(UNIT_ID)
            .recipient(null)
            .xml(ENCODED_XML)
            .build();
        when(getCertificateXmlService.get(CERTIFICATE_ID)).thenReturn(resp);
        when(statisticsService.revoked(DECODED_XML, CERTIFICATE_ID, CERTIFICATE_TYPE, UNIT_ID)).thenReturn(true);

        final var result = certificateEventStatisticsServiceImpl.send(EVENT_REVOKED, CERTIFICATE_ID, MESSAGE_ID);

        assertTrue(result);
        verify(statisticsService).revoked(DECODED_XML, resp.getCertificateId(), resp.getCertificateType(), resp.getUnitId());
    }

    @Test
    void shouldCallStatisticsServiceSent() {
        final var resp = GetCertificateXmlResponse.builder()
            .certificateId(CERTIFICATE_ID)
            .certificateType(CERTIFICATE_TYPE)
            .unitId(UNIT_ID)
            .recipient(RECIPIENT)
            .build();
        when(getCertificateXmlService.get(CERTIFICATE_ID)).thenReturn(resp);
        when(statisticsService.sent(CERTIFICATE_ID, CERTIFICATE_TYPE, UNIT_ID, RECIPIENT)).thenReturn(true);

        final var result = certificateEventStatisticsServiceImpl.send(EVENT_SENT, CERTIFICATE_ID, MESSAGE_ID);

        assertTrue(result);
        verify(statisticsService).sent(resp.getCertificateId(), resp.getCertificateType(), resp.getUnitId(), resp.getRecipient());
    }

    @Test
    void shouldCallStatisticsServiceMessageSent() {
        final var resp = GetMessageXmlResponse.builder()
            .messageId(MESSAGE_ID)
            .topic(TOPIC)
            .xml(ENCODED_XML)
            .build();
        when(getMessageXmlService.get(MESSAGE_ID)).thenReturn(resp);
        when(statisticsService.messageSent(DECODED_XML, MESSAGE_ID, TOPIC)).thenReturn(true);

        final var result = certificateEventStatisticsServiceImpl.send(EVENT_MESSAGE_SENT, CERTIFICATE_ID, MESSAGE_ID);

        assertTrue(result);
        verify(statisticsService).messageSent(DECODED_XML, resp.getMessageId(), resp.getTopic());
    }

    @Test
    void shouldReturnFalseIfFalseReturnedFromStatisticsServie() {
        final var resp = GetCertificateXmlResponse.builder()
            .certificateId(CERTIFICATE_ID)
            .certificateType(CERTIFICATE_TYPE)
            .unitId(UNIT_ID)
            .recipient(null)
            .xml(ENCODED_XML)
            .build();
        when(getCertificateXmlService.get(CERTIFICATE_ID)).thenReturn(resp);
        when(statisticsService.created(DECODED_XML, CERTIFICATE_ID, CERTIFICATE_TYPE, UNIT_ID)).thenReturn(false);

        final var result = certificateEventStatisticsServiceImpl.send(EVENT_SIGNED, CERTIFICATE_ID, MESSAGE_ID);

        assertFalse(result);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUnknownEventType() {
        assertThrows(IllegalArgumentException.class, () -> certificateEventStatisticsServiceImpl.send(EVENT_UNKNOWN, CERTIFICATE_ID,
            MESSAGE_ID));
        verifyNoInteractions(statisticsService);
        verifyNoInteractions(getCertificateXmlService);
        verifyNoInteractions(getMessageXmlService);
    }

    @Test
    void shouldThrowRestClientExceptionWhenFailedCertificateXmlCall() {
        when(getCertificateXmlService.get(CERTIFICATE_ID)).thenThrow(RestClientException.class);
        assertThrows(RestClientException.class, () -> certificateEventStatisticsServiceImpl.send(EVENT_SIGNED, CERTIFICATE_ID, MESSAGE_ID));
    }

    @Test
    void shouldThrowRestClientExceptionWhenFailedMessageXmlCall() {
        when(getMessageXmlService.get(MESSAGE_ID)).thenThrow(RestClientException.class);
        assertThrows(RestClientException.class, () -> certificateEventStatisticsServiceImpl.send(EVENT_MESSAGE_SENT, CERTIFICATE_ID,
            MESSAGE_ID));
    }
}
