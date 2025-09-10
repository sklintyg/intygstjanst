package se.inera.intyg.intygstjanst.web.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueBoolean;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.csintegration.CSIntegrationService;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCertificateXmlResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SickLeaveResponseDTO;

@ExtendWith(MockitoExtension.class)
class HandleSickLeaveServiceTest {

    @Mock
    private SjukfallCertificateDao sjukfallCertificateDao;
    @Mock
    private CSIntegrationService csIntegrationService;
    @InjectMocks
    private HandleSickLeaveService handleSickleaveService;

    private static final String FK7804_TYPE = "fk7804";
    private static final String OTHER_TYPE = "otherType";
    private static final String CERTIFICATE_ID = "certId";
    private static final String QUESTION_ID = "27";

    @BeforeEach
    void setUp() {
        handleSickleaveService = new HandleSickLeaveService(sjukfallCertificateDao, csIntegrationService);
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
              .sickLeaveCertificate(new SjukfallCertificate(CERTIFICATE_ID))
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
              .sickLeaveCertificate(new SjukfallCertificate(CERTIFICATE_ID))
              .available(true)
              .build();


          when(cert.getSickLeaveCertificate()).thenReturn(data.getSickLeaveCertificate());
          when(cert.isAvailable()).thenReturn(data.isAvailable());
          when(csIntegrationService.getSickLeaveCertificate(CERTIFICATE_ID)).thenReturn(cert);

          handleSickleaveService.created(response);

          verify(csIntegrationService).getSickLeaveCertificate(CERTIFICATE_ID);
          verify(sjukfallCertificateDao).store(data.getSickLeaveCertificate());
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
                .sickLeaveCertificate(new SjukfallCertificate(CERTIFICATE_ID))
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
                .sickLeaveCertificate(new SjukfallCertificate(CERTIFICATE_ID))
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