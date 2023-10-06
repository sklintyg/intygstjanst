/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.CertificateService.SendStatus;
import se.inera.intyg.intygstjanst.web.service.InternalNotificationService;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;
import se.inera.intyg.intygstjanst.web.service.dto.SendCertificateRequestDTO;
import se.inera.intyg.schemas.contract.Personnummer;

@ExtendWith(MockitoExtension.class)
class SendCertificateServiceImplTest {

  private static final SendCertificateRequestDTO REQUEST = SendCertificateRequestDTO
      .builder()
      .certificateId("id")
      .hsaId("hsaId")
      .patientId(Personnummer.createPersonnummer("191212121212").get())
      .recipientId("recipient")
      .build();

  private static Certificate certificate;

  @Mock
  CertificateService certificateService;
  @Mock
  StatisticsService statisticsService;
  @Mock
  InternalNotificationService internalNotificationService;
  @InjectMocks
  SendCertificateServiceImpl sendCertificateService;

  @BeforeEach
  void setup() throws InvalidCertificateException {
    certificate = getCertificate();
    when(certificateService.getCertificateForCare(anyString()))
        .thenReturn(certificate);
  }

  private Certificate getCertificate() {
    final var certificate = new Certificate("id");
    certificate.setType("Lisjp");
    certificate.setCareUnitId("unitId");
    return certificate;
  }

  @Nested
  class StatisticsServiceTest {
    @SneakyThrows
    @Test
    void shouldSendCertificateId() {
      sendCertificateService.send(REQUEST);

      final var captor = ArgumentCaptor.forClass(String.class);

      verify(statisticsService).sent(captor.capture(), anyString(), anyString(), anyString());
      assertEquals(REQUEST.getCertificateId(), captor.getValue());
    }

    @SneakyThrows
    @Test
    void shouldSendCertificateType() {
      sendCertificateService.send(REQUEST);

      final var captor = ArgumentCaptor.forClass(String.class);

      verify(statisticsService).sent(anyString(), captor.capture(), anyString(), anyString());
      assertEquals(certificate.getType(), captor.getValue());
    }

    @SneakyThrows
    @Test
    void shouldSendCareUnitId() {
      sendCertificateService.send(REQUEST);

      final var captor = ArgumentCaptor.forClass(String.class);

      verify(statisticsService).sent(anyString(), anyString(), captor.capture(), anyString());
      assertEquals(certificate.getCareUnitId(), captor.getValue());
    }

    @SneakyThrows
    @Test
    void shouldSendRecipientId() {
      sendCertificateService.send(REQUEST);

      final var captor = ArgumentCaptor.forClass(String.class);

      verify(statisticsService).sent(anyString(), anyString(), anyString(), captor.capture());
      assertEquals(REQUEST.getRecipientId(), captor.getValue());
    }
  }

  @Nested
  class InternalNotificationServiceTest {

    @SneakyThrows
    @Test
    void shouldSendCertificate() {
      sendCertificateService.send(REQUEST);

      final var captor = ArgumentCaptor.forClass(Certificate.class);

      verify(internalNotificationService).notifyCareIfSentByCitizen(captor.capture(), anyString(), anyString());
      assertEquals(certificate, captor.getValue());
    }

    @SneakyThrows
    @Test
    void shouldSendPatientId() {
      sendCertificateService.send(REQUEST);

      final var captor = ArgumentCaptor.forClass(String.class);

      verify(internalNotificationService).notifyCareIfSentByCitizen(any(), captor.capture(), anyString());
      assertEquals("191212121212", captor.getValue());
    }

    @SneakyThrows
    @Test
    void shouldSendHosId() {
      sendCertificateService.send(REQUEST);

      final var captor = ArgumentCaptor.forClass(String.class);

      verify(internalNotificationService).notifyCareIfSentByCitizen(any(), anyString(), captor.capture());
      assertEquals(REQUEST.getHsaId(), captor.getValue());
    }

    @Nested
    class AlreadySent {
      @SneakyThrows
      @BeforeEach
      void setup() {
        when(certificateService.sendCertificate(any(), anyString(), anyString())).thenReturn(
            SendStatus.ALREADY_SENT);
      }

      @SneakyThrows
      @Test
      void shouldNotCallInternalNotification() {
        sendCertificateService.send(REQUEST);

        verify(internalNotificationService, times(0)).notifyCareIfSentByCitizen(any(), any(), any());
      }

      @SneakyThrows
      @Test
      void shouldNotCallStatisticsService() {
        sendCertificateService.send(REQUEST);

        verify(statisticsService, times(0)).sent(any(), any(), any(), any());
      }

      @SneakyThrows
      @Test
      void shouldReturnSentStatus() {
        final var response = sendCertificateService.send(REQUEST);

        assertEquals(SendStatus.ALREADY_SENT, response);
      }
    }
  }

  @Nested
  class SendCertificate {
    @SneakyThrows
    @Test
    void shouldSendPatientId() {
      sendCertificateService.send(REQUEST);

      final var captor = ArgumentCaptor.forClass(Personnummer.class);

      verify(certificateService).sendCertificate(captor.capture(), anyString(), anyString());
      assertEquals(REQUEST.getPatientId(), captor.getValue());
    }

    @SneakyThrows
    @Test
    void shouldSendCertificateId() {
      sendCertificateService.send(REQUEST);

      final var captor = ArgumentCaptor.forClass(String.class);

      verify(certificateService).sendCertificate(any(), captor.capture(), anyString());
      assertEquals(REQUEST.getCertificateId(), captor.getValue());
    }

    @SneakyThrows
    @Test
    void shouldSendRecipient() {
      sendCertificateService.send(REQUEST);

      final var captor = ArgumentCaptor.forClass(String.class);

      verify(certificateService).sendCertificate(any(), anyString(), captor.capture());
      assertEquals(REQUEST.getRecipientId(), captor.getValue());
    }
  }
}