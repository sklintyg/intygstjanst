package se.inera.intyg.intygstjanst.web.service.impl;

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
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.csintegration.CSIntegrationService;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCertificateXmlResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SickLeaveCertificateDTO;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SickLeaveResponseDTO;
import se.inera.intyg.intygstjanst.web.service.converter.SickLeaveCertificateToSjukfallCertificateConverter;

@ExtendWith(MockitoExtension.class)
class HandleSickLeaveServiceTest {

    @Mock
    private SickLeaveCertificateToSjukfallCertificateConverter converter;
    @Mock
    private SjukfallCertificateDao sjukfallCertificateDao;
    @Mock
    private CSIntegrationService csIntegrationService;
    @InjectMocks
    private HandleSickLeaveService handleSickleaveService;

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
        handleSickleaveService = new HandleSickLeaveService(sjukfallCertificateDao, csIntegrationService, converter);
    }

    @Nested
    class CreatedTests {

        @Test
        void shouldNotStoreIfNotAvailable() {
            final var response = GetCertificateXmlResponse.builder()
                .certificateType(FK7804_TYPE)
                .certificateId(CERTIFICATE_ID)
                .build();

            final var cert = mock(SickLeaveResponseDTO.class);

          final var data = SickLeaveResponseDTO.builder()
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
          final var response = GetCertificateXmlResponse.builder()
              .certificateType(FK7804_TYPE)
              .certificateId(CERTIFICATE_ID)
              .build();

          final var cert = mock(SickLeaveResponseDTO.class);

          final var data = SickLeaveResponseDTO.builder()
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
            final var response = GetCertificateXmlResponse.builder()
                .certificateType(FK7804_TYPE)
                .certificateId(CERTIFICATE_ID)
                .build();

            final var cert = mock(SickLeaveResponseDTO.class);

            final var data = SickLeaveResponseDTO.builder()
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
            final var response = GetCertificateXmlResponse.builder()
                .certificateType(FK7804_TYPE)
                .certificateId(CERTIFICATE_ID)
                .build();

            final var cert = mock(SickLeaveResponseDTO.class);

            final var data = SickLeaveResponseDTO.builder()
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