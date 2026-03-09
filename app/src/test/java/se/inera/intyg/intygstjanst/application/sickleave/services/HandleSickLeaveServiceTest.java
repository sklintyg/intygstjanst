/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.application.sickleave.services;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.application.sickleave.converter.SickLeaveCertificateToSjukfallCertificateConverter;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.CSIntegrationService;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.dto.GetCertificateXmlResponse;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.dto.SickLeaveCertificateDTO;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.dto.SickLeaveResponseDTO;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.SjukfallCertificateDao;

@ExtendWith(MockitoExtension.class)
class HandleSickLeaveServiceTest {

  @Mock private SickLeaveCertificateToSjukfallCertificateConverter converter;
  @Mock private SjukfallCertificateDao sjukfallCertificateDao;
  @Mock private CSIntegrationService csIntegrationService;
  @InjectMocks private HandleSickLeaveService handleSickleaveService;

  private static final String FK7804_TYPE = "fk7804";
  private static final String CERTIFICATE_ID = "certId";
  private static final SickLeaveCertificateDTO SICK_LEAVE_CERTIFICATE_DTO =
      SickLeaveCertificateDTO.builder()
          .id(CERTIFICATE_ID)
          .type(FK7804_TYPE)
          .signingDoctorId("signingDoctorId")
          .signingDoctorName("signingDoctorName")
          .signingDateTime(null)
          .careUnitId("careUnitId")
          .careUnitName("careUnitName")
          .careGiverId("careGiverId")
          .civicRegistrationNumber("civicRegistrationNumber")
          .patientName("patientName")
          .diagnoseCode("diagnoseCode")
          .biDiagnoseCode1("biDiagnoseCode1")
          .biDiagnoseCode2("biDiagnoseCode2")
          .employment("employment")
          .deleted(false)
          .testCertificate(false)
          .build();

  @BeforeEach
  void setUp() {
    handleSickleaveService =
        new HandleSickLeaveService(sjukfallCertificateDao, csIntegrationService, converter);
  }

  @Nested
  class CreatedTests {

    @Test
    void shouldNotStoreIfNotAvailable() {
      final var response =
          GetCertificateXmlResponse.builder()
              .certificateType(FK7804_TYPE)
              .certificateId(CERTIFICATE_ID)
              .build();

      final var cert = mock(SickLeaveResponseDTO.class);

      final var data =
          SickLeaveResponseDTO.builder()
              .sickLeaveCertificate(SICK_LEAVE_CERTIFICATE_DTO)
              .available(false)
              .build();

      when(cert.isAvailable()).thenReturn(data.isAvailable());
      when(csIntegrationService.getSickLeaveCertificate(CERTIFICATE_ID)).thenReturn(cert);

      handleSickleaveService.created(response);

      verify(csIntegrationService).getSickLeaveCertificate(CERTIFICATE_ID);
      verifyNoInteractions(sjukfallCertificateDao);
    }

    @Test
    void shouldStoreIfAvailable() {
      final var response =
          GetCertificateXmlResponse.builder()
              .certificateType(FK7804_TYPE)
              .certificateId(CERTIFICATE_ID)
              .build();

      final var cert = mock(SickLeaveResponseDTO.class);

      final var data =
          SickLeaveResponseDTO.builder()
              .sickLeaveCertificate(SICK_LEAVE_CERTIFICATE_DTO)
              .available(true)
              .build();

      when(cert.getSickLeaveCertificate()).thenReturn(data.getSickLeaveCertificate());
      when(cert.isAvailable()).thenReturn(data.isAvailable());
      when(csIntegrationService.getSickLeaveCertificate(CERTIFICATE_ID)).thenReturn(cert);

      handleSickleaveService.created(response);

      verify(csIntegrationService).getSickLeaveCertificate(CERTIFICATE_ID);
      verify(sjukfallCertificateDao).store(converter.convert(data.getSickLeaveCertificate()));
    }
  }

  @Nested
  class RevokedTests {

    @Test
    void shouldNotRevokeIfNotAvailable() {
      final var response =
          GetCertificateXmlResponse.builder()
              .certificateType(FK7804_TYPE)
              .certificateId(CERTIFICATE_ID)
              .build();

      final var cert = mock(SickLeaveResponseDTO.class);

      final var data =
          SickLeaveResponseDTO.builder()
              .sickLeaveCertificate(SICK_LEAVE_CERTIFICATE_DTO)
              .available(false)
              .build();

      when(cert.isAvailable()).thenReturn(data.isAvailable());
      when(csIntegrationService.getSickLeaveCertificate(CERTIFICATE_ID)).thenReturn(cert);

      handleSickleaveService.revoked(response);

      verify(csIntegrationService).getSickLeaveCertificate(CERTIFICATE_ID);
      verifyNoInteractions(sjukfallCertificateDao);
    }

    @Test
    void shouldRevokeIfAvailable() {
      final var response =
          GetCertificateXmlResponse.builder()
              .certificateType(FK7804_TYPE)
              .certificateId(CERTIFICATE_ID)
              .build();

      final var cert = mock(SickLeaveResponseDTO.class);

      final var data =
          SickLeaveResponseDTO.builder()
              .sickLeaveCertificate(SICK_LEAVE_CERTIFICATE_DTO)
              .available(true)
              .build();

      when(cert.getSickLeaveCertificate()).thenReturn(data.getSickLeaveCertificate());
      when(cert.isAvailable()).thenReturn(data.isAvailable());
      when(csIntegrationService.getSickLeaveCertificate(CERTIFICATE_ID)).thenReturn(cert);

      handleSickleaveService.revoked(response);

      verify(csIntegrationService).getSickLeaveCertificate(CERTIFICATE_ID);
      verify(sjukfallCertificateDao).revoke(CERTIFICATE_ID);
    }
  }
}
