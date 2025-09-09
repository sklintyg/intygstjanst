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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.intygstjanst.web.csintegration.CSIntegrationService;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCertificateXmlResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetMessageXmlResponse;
import se.inera.intyg.intygstjanst.web.service.CertificateEventRevokeService;
import se.inera.intyg.intygstjanst.web.service.CertificateEventSendMessageService;
import se.inera.intyg.intygstjanst.web.service.CertificateEventSendService;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;
import se.inera.intyg.intygstjanst.web.service.dto.RecipientDTO;
import se.inera.intyg.intygstjanst.web.service.dto.UnitDTO;

@ExtendWith(MockitoExtension.class)
class CertificateEventServiceImplTest {

    @Mock
    private StatisticsService statisticsService;
    @Mock
    private CSIntegrationService csIntegrationService;
    @Mock
    private CertificateEventSendService certificateEventSendService;
    @Mock
    private CertificateEventRevokeService certificateEventRevokeService;
    @Mock
    private CertificateEventSendMessageService certificateEventSendMessageService;
    @Mock
    private HandleSickleaveService handleSickleaveService;
    @InjectMocks
    private CertificateEventServiceImpl certificateEventService;

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "fk7211";
    private static final String EVENT_SIGNED = "certificate-signed";
    private static final String EVENT_REVOKED = "certificate-revoked";
    private static final String EVENT_SENT = "certificate-sent";
    private static final String EVENT_MESSAGE_SENT = "message-sent";
    private static final String EVENT_UNKNOWN = "unknown";
    private static final String UNIT_ID = "unitId";
    private static final String MESSAGE_ID = "messageId";
    private static final String RECIPIENT_ID = "recipientId";
    private static final String DECODED_XML = "xmlFromCertificateService";
    private static final String ENCODED_XML = Base64.getEncoder().encodeToString("xmlFromCertificateService"
        .getBytes(StandardCharsets.UTF_8));

    @Nested
    class TestInidicated {

        @BeforeEach
        void setup() {
            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID))
                .thenReturn(
                    CertificateMetadata.builder()
                        .testCertificate(true)
                        .build()
                );
        }

        @Test
        void shouldNotCallStatisticsService() {
            final var result = certificateEventService.processEvent(EVENT_SIGNED, CERTIFICATE_ID, MESSAGE_ID);

            assertTrue(result);
            verifyNoInteractions(statisticsService);
        }
    }

    @Nested
    class NotTestInidicated {

        @BeforeEach
        void setup() {
            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID))
                .thenReturn(
                    CertificateMetadata.builder()
                        .testCertificate(false)
                        .build()
                );
        }

        @Nested
        class EventSigned {

            @Test
            void shouldCallStatisticsServiceCreated() {
                final var resp = GetCertificateXmlResponse.builder()
                    .certificateId(CERTIFICATE_ID)
                    .certificateType(CERTIFICATE_TYPE)
                    .unit(
                        UnitDTO.builder()
                            .unitId(UNIT_ID)
                            .build()
                    )
                    .recipient(null)
                    .xml(ENCODED_XML)
                    .build();
                when(csIntegrationService.getCertificateXmlResponse(CERTIFICATE_ID)).thenReturn(resp);
                when(statisticsService.created(DECODED_XML, CERTIFICATE_ID, CERTIFICATE_TYPE, UNIT_ID)).thenReturn(true);

                final var result = certificateEventService.processEvent(EVENT_SIGNED, CERTIFICATE_ID, MESSAGE_ID);

                assertTrue(result);
                verify(statisticsService).created(DECODED_XML, resp.getCertificateId(), resp.getCertificateType(),
                    resp.getUnit().getUnitId());
            }

            @Test
            void shouldCallHandleSickleaveService() {
                final var resp = GetCertificateXmlResponse.builder()
                    .certificateId(CERTIFICATE_ID)
                    .certificateType(CERTIFICATE_TYPE)
                    .unit(
                        UnitDTO.builder()
                            .unitId(UNIT_ID)
                            .build()
                    )
                    .recipient(null)
                    .xml(ENCODED_XML)
                    .build();
                when(csIntegrationService.getCertificateXmlResponse(CERTIFICATE_ID)).thenReturn(resp);

                certificateEventService.processEvent(EVENT_SIGNED, CERTIFICATE_ID, MESSAGE_ID);

                verify(handleSickleaveService).created(resp, DECODED_XML);
            }

            @Test
            void shouldReturnFalseIfFalseReturnedFromStatisticsService() {
                final var resp = GetCertificateXmlResponse.builder()
                    .certificateId(CERTIFICATE_ID)
                    .certificateType(CERTIFICATE_TYPE)
                    .unit(
                        UnitDTO.builder()
                            .unitId(UNIT_ID)
                            .build()
                    )
                    .recipient(null)
                    .xml(ENCODED_XML)
                    .build();
                when(csIntegrationService.getCertificateXmlResponse(CERTIFICATE_ID)).thenReturn(resp);
                when(statisticsService.created(DECODED_XML, CERTIFICATE_ID, CERTIFICATE_TYPE, UNIT_ID)).thenReturn(false);

                final var result = certificateEventService.processEvent(EVENT_SIGNED, CERTIFICATE_ID, MESSAGE_ID);

                assertFalse(result);
            }

            @Test
            void shouldThrowRestClientExceptionWhenFailedCertificateXmlCall() {
                when(csIntegrationService.getCertificateXmlResponse(CERTIFICATE_ID)).thenThrow(RestClientException.class);
                assertThrows(RestClientException.class,
                    () -> certificateEventService.processEvent(EVENT_SIGNED, CERTIFICATE_ID, MESSAGE_ID));
            }
        }

        @Nested
        class EventSent {

            @Test
            void shouldCallSendServiceForCertificateSentEvent() {
                final var resp = GetCertificateXmlResponse.builder()
                    .certificateId(CERTIFICATE_ID)
                    .certificateType(CERTIFICATE_TYPE)
                    .unit(
                        UnitDTO.builder()
                            .unitId(UNIT_ID)
                            .build()
                    )
                    .xml(ENCODED_XML)
                    .recipient(RecipientDTO.builder()
                        .id(RECIPIENT_ID)
                        .build())
                    .build();
                when(csIntegrationService.getCertificateXmlResponse(CERTIFICATE_ID)).thenReturn(resp);
                when(statisticsService.sent(CERTIFICATE_ID, CERTIFICATE_TYPE, UNIT_ID, RECIPIENT_ID)).thenReturn(true);

                final var result = certificateEventService.processEvent(EVENT_SENT, CERTIFICATE_ID, MESSAGE_ID);

                assertTrue(result);
                verify(certificateEventSendService).send(resp, DECODED_XML);
            }

            @Test
            void shouldCallStatisticsServiceSent() {
                final var resp = GetCertificateXmlResponse.builder()
                    .certificateId(CERTIFICATE_ID)
                    .certificateType(CERTIFICATE_TYPE)
                    .unit(
                        UnitDTO.builder()
                            .unitId(UNIT_ID)
                            .build()
                    )
                    .xml(ENCODED_XML)
                    .recipient(RecipientDTO.builder()
                        .id(RECIPIENT_ID)
                        .build())
                    .build();
                when(csIntegrationService.getCertificateXmlResponse(CERTIFICATE_ID)).thenReturn(resp);
                when(statisticsService.sent(CERTIFICATE_ID, CERTIFICATE_TYPE, UNIT_ID, RECIPIENT_ID)).thenReturn(true);

                final var result = certificateEventService.processEvent(EVENT_SENT, CERTIFICATE_ID, MESSAGE_ID);

                assertTrue(result);
                verify(statisticsService).sent(resp.getCertificateId(), resp.getCertificateType(), resp.getUnit().getUnitId(),
                    resp.getRecipient().getId());
            }

            @Test
            void shouldThrowExceptionIfWhenExceptionFromCertificateEventSendService() {
                final var resp = GetCertificateXmlResponse.builder()
                    .certificateId(CERTIFICATE_ID)
                    .certificateType(CERTIFICATE_TYPE)
                    .unit(
                        UnitDTO.builder()
                            .unitId(UNIT_ID)
                            .build()
                    )
                    .xml(ENCODED_XML)
                    .recipient(RecipientDTO.builder()
                        .id(RECIPIENT_ID)
                        .build())
                    .build();
                when(csIntegrationService.getCertificateXmlResponse(CERTIFICATE_ID)).thenReturn(resp);
                doThrow(IllegalStateException.class).when(certificateEventSendService).send(resp, DECODED_XML);

                assertThrows(IllegalStateException.class,
                    () -> certificateEventService.processEvent(EVENT_SENT, CERTIFICATE_ID, MESSAGE_ID));
            }
        }

        @Nested
        class EventRevoked {

            @Test
            void shouldCallStatisticsServiceRevokedIfSent() {
                final var resp = GetCertificateXmlResponse.builder()
                    .certificateId(CERTIFICATE_ID)
                    .certificateType(CERTIFICATE_TYPE)
                    .unit(
                        UnitDTO.builder()
                            .unitId(UNIT_ID)
                            .build()
                    )
                    .recipient(RecipientDTO.builder()
                        .sent(LocalDateTime.now())
                        .build())
                    .xml(ENCODED_XML)
                    .build();
                when(csIntegrationService.getCertificateXmlResponse(CERTIFICATE_ID)).thenReturn(resp);
                when(statisticsService.revoked(DECODED_XML, CERTIFICATE_ID, CERTIFICATE_TYPE, UNIT_ID)).thenReturn(true);

                final var result = certificateEventService.processEvent(EVENT_REVOKED, CERTIFICATE_ID, MESSAGE_ID);

                assertTrue(result);
                verify(statisticsService).revoked(DECODED_XML, resp.getCertificateId(), resp.getCertificateType(),
                    resp.getUnit().getUnitId());
            }

            @Test
            void shouldCallStatisticsServiceRevokedIfNotSent() {
                final var resp = GetCertificateXmlResponse.builder()
                    .certificateId(CERTIFICATE_ID)
                    .certificateType(CERTIFICATE_TYPE)
                    .unit(
                        UnitDTO.builder()
                            .unitId(UNIT_ID)
                            .build()
                    )
                    .recipient(RecipientDTO.builder()
                        .sent(null)
                        .build())
                    .xml(ENCODED_XML)
                    .build();
                when(csIntegrationService.getCertificateXmlResponse(CERTIFICATE_ID)).thenReturn(resp);
                when(statisticsService.revoked(DECODED_XML, CERTIFICATE_ID, CERTIFICATE_TYPE, UNIT_ID)).thenReturn(true);

                final var result = certificateEventService.processEvent(EVENT_REVOKED, CERTIFICATE_ID, MESSAGE_ID);

                assertTrue(result);
                verify(statisticsService).revoked(DECODED_XML, resp.getCertificateId(), resp.getCertificateType(),
                    resp.getUnit().getUnitId());
            }

            @Test
            void shouldCallRevokeServiceForCertificateRevokedEventIfSent() {
                final var resp = GetCertificateXmlResponse.builder()
                    .certificateId(CERTIFICATE_ID)
                    .certificateType(CERTIFICATE_TYPE)
                    .unit(
                        UnitDTO.builder()
                            .unitId(UNIT_ID)
                            .build()
                    )
                    .xml(ENCODED_XML)
                    .recipient(RecipientDTO.builder()
                        .id(RECIPIENT_ID)
                        .sent(LocalDateTime.now())
                        .build())
                    .build();
                when(csIntegrationService.getCertificateXmlResponse(CERTIFICATE_ID)).thenReturn(resp);
                when(statisticsService.revoked(DECODED_XML, CERTIFICATE_ID, CERTIFICATE_TYPE, UNIT_ID)).thenReturn(true);

                final var result = certificateEventService.processEvent(EVENT_REVOKED, CERTIFICATE_ID, MESSAGE_ID);

                assertTrue(result);
                verify(certificateEventRevokeService, times(1)).revoke(resp);
            }

            @Test
            void shouldNotCallRevokeServiceForCertificateRevokedEventIfNotSent() {
                final var resp = GetCertificateXmlResponse.builder()
                    .certificateId(CERTIFICATE_ID)
                    .certificateType(CERTIFICATE_TYPE)
                    .unit(
                        UnitDTO.builder()
                            .unitId(UNIT_ID)
                            .build()
                    )
                    .xml(ENCODED_XML)
                    .recipient(RecipientDTO.builder()
                        .id(RECIPIENT_ID)
                        .build())
                    .build();
                when(csIntegrationService.getCertificateXmlResponse(CERTIFICATE_ID)).thenReturn(resp);
                when(statisticsService.revoked(DECODED_XML, CERTIFICATE_ID, CERTIFICATE_TYPE, UNIT_ID)).thenReturn(true);

                certificateEventService.processEvent(EVENT_REVOKED, CERTIFICATE_ID, MESSAGE_ID);

                verify(certificateEventRevokeService, times(0)).revoke(resp);
            }
        }

        @Nested
        class EventMessageSent {

            @Test
            void shouldReturnTrueIfNoExceptionFromSendMessageService() {
                final var resp = GetMessageXmlResponse.builder()
                    .xml(ENCODED_XML)
                    .build();
                when(csIntegrationService.getMessageXmlResponse(MESSAGE_ID)).thenReturn(resp);

                final var result = certificateEventService.processEvent(EVENT_MESSAGE_SENT, CERTIFICATE_ID, MESSAGE_ID);
                assertTrue(result);
            }

            @Test
            void shouldThrowIfExceptionFromSendMessageService() {
                final var resp = GetMessageXmlResponse.builder()
                    .xml(ENCODED_XML)
                    .build();
                when(csIntegrationService.getMessageXmlResponse(MESSAGE_ID)).thenReturn(resp);
                doThrow(IllegalStateException.class).when(certificateEventSendMessageService).sendMessage(DECODED_XML);

                assertThrows(IllegalStateException.class,
                    () -> certificateEventService.processEvent(EVENT_MESSAGE_SENT, CERTIFICATE_ID, MESSAGE_ID));
            }

            @Test
            void shouldNotCallStatisticsServiceMessageSent() {
                final var resp = GetMessageXmlResponse.builder()
                    .xml(ENCODED_XML)
                    .build();
                when(csIntegrationService.getMessageXmlResponse(MESSAGE_ID)).thenReturn(resp);

                certificateEventService.processEvent(EVENT_MESSAGE_SENT, CERTIFICATE_ID, MESSAGE_ID);
                verifyNoInteractions(statisticsService);
            }

            @Test
            void shouldThrowRestClientExceptionWhenFailedMessageXmlCall() {
                when(csIntegrationService.getMessageXmlResponse(MESSAGE_ID)).thenThrow(RestClientException.class);
                assertThrows(RestClientException.class, () -> certificateEventService.processEvent(EVENT_MESSAGE_SENT, CERTIFICATE_ID,
                    MESSAGE_ID));
            }
        }

        @Nested
        class EventUnknown {

            @Test
            void shouldThrowIllegalArgumentExceptionWhenUnknownEventType() {
                assertThrows(IllegalArgumentException.class, () -> certificateEventService.processEvent(EVENT_UNKNOWN, CERTIFICATE_ID,
                    MESSAGE_ID));
                verifyNoInteractions(statisticsService);
            }
        }
    }

}
