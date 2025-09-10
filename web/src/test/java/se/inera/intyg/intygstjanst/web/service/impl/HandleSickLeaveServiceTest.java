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
import se.inera.intyg.intygstjanst.web.service.converter.CertificateToSickLeaveConverter;

@ExtendWith(MockitoExtension.class)
class HandleSickLeaveServiceTest {

    @Mock
    private CertificateToSickLeaveConverter certificateToSickLeaveConverter;
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
        handleSickleaveService = new HandleSickLeaveService(sjukfallCertificateDao, csIntegrationService, certificateToSickLeaveConverter);
    }

    @Nested
    class CreatedTests {

        @Test
        void shouldNotStoreIfTypeIsNotFk7804() {
            var response = GetCertificateXmlResponse.builder()
                .certificateType(OTHER_TYPE)
                .build();
            handleSickleaveService.created(response);
            verifyNoInteractions(csIntegrationService, sjukfallCertificateDao);
        }

        @Test
        void shouldNotStoreIfQuestionIsTrue() {
            final var response = GetCertificateXmlResponse.builder()
                .certificateType(FK7804_TYPE)
                .certificateId(CERTIFICATE_ID)
                .build();

            final var cert = mock(Certificate.class);
            final var dataElement = mock(CertificateDataElement.class);
            final var value = mock(CertificateDataValueBoolean.class);

            when(value.getSelected()).thenReturn(true);
            when(dataElement.getValue()).thenReturn(value);

            final var data = new HashMap<String, CertificateDataElement>();
            data.put(QUESTION_ID, dataElement);

            when(cert.getData()).thenReturn(data);
            when(csIntegrationService.getCertificate(CERTIFICATE_ID)).thenReturn(cert);

            handleSickleaveService.created(response);

            verify(csIntegrationService).getCertificate(CERTIFICATE_ID);
            verifyNoInteractions(sjukfallCertificateDao);
        }

        @Test
        void shouldStoreIfQuestionIsFalse() {
            final var response = GetCertificateXmlResponse.builder()
                .certificateType(FK7804_TYPE)
                .certificateId(CERTIFICATE_ID)
                .build();

            final var cert = mock(Certificate.class);
            final var dataElement = mock(CertificateDataElement.class);
            final var value = mock(CertificateDataValueBoolean.class);

            when(value.getSelected()).thenReturn(false);
            when(dataElement.getValue()).thenReturn(value);

            final var sjukfallCertificate = new SjukfallCertificate(CERTIFICATE_ID);
            final var data = new HashMap<String, CertificateDataElement>();
            data.put(QUESTION_ID, dataElement);

            when(csIntegrationService.getCertificate(CERTIFICATE_ID)).thenReturn(cert);
            when(cert.getData()).thenReturn(data);
            when(certificateToSickLeaveConverter.convert(cert)).thenReturn(sjukfallCertificate);

            handleSickleaveService.created(response);

            verify(csIntegrationService).getCertificate(CERTIFICATE_ID);
            verify(sjukfallCertificateDao).store(sjukfallCertificate);
        }
    }

    @Nested
    class RevokedTests {

        @Test
        void shouldNotRevokeIfTypeIsNotFk7804() {
            var response = GetCertificateXmlResponse.builder()
                .certificateType(OTHER_TYPE)
                .build();
            handleSickleaveService.revoked(response);
            verifyNoInteractions(csIntegrationService, sjukfallCertificateDao);
        }

        @Test
        void shouldNotRevokeIfQuestionIsTrue() {
            final var response = GetCertificateXmlResponse.builder()
                .certificateType(FK7804_TYPE)
                .certificateId(CERTIFICATE_ID)
                .build();

            final var cert = mock(Certificate.class);
            final var dataElement = mock(CertificateDataElement.class);
            final var value = mock(CertificateDataValueBoolean.class);

            when(value.getSelected()).thenReturn(true);
            when(dataElement.getValue()).thenReturn(value);

            final var data = new HashMap<String, CertificateDataElement>();
            data.put(QUESTION_ID, dataElement);

            when(cert.getData()).thenReturn(data);
            when(csIntegrationService.getCertificate(CERTIFICATE_ID)).thenReturn(cert);

            handleSickleaveService.revoked(response);

            verify(csIntegrationService).getCertificate(CERTIFICATE_ID);
            verifyNoInteractions(sjukfallCertificateDao);
        }

        @Test
        void shouldRevokeIfQuestionIsFalse() {
            final var response = GetCertificateXmlResponse.builder()
                .certificateType(FK7804_TYPE)
                .certificateId(CERTIFICATE_ID)
                .build();

            final var cert = mock(Certificate.class);
            final var dataElement = mock(CertificateDataElement.class);
            final var value = mock(CertificateDataValueBoolean.class);

            when(value.getSelected()).thenReturn(false);
            when(dataElement.getValue()).thenReturn(value);

            final var data = new HashMap<String, CertificateDataElement>();
            data.put(QUESTION_ID, dataElement);

            when(csIntegrationService.getCertificate(CERTIFICATE_ID)).thenReturn(cert);
            when(cert.getData()).thenReturn(data);

            handleSickleaveService.revoked(response);

            verify(csIntegrationService).getCertificate(CERTIFICATE_ID);
            verify(sjukfallCertificateDao).revoke(CERTIFICATE_ID);
        }
    }
}