package se.inera.intyg.intygstjanst.web.csintegration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SendCitizenCertificateRequestDTO;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.TestCertificateException;
import se.inera.intyg.intygstjanst.web.service.CertificateService.SendStatus;
import se.inera.intyg.intygstjanst.web.service.InternalNotificationService;
import se.inera.intyg.intygstjanst.web.service.dto.PersonIdDTO;
import se.inera.intyg.intygstjanst.web.service.dto.PersonIdTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.SendCertificateRequestDTO;
import se.inera.intyg.schemas.contract.Personnummer;

@ExtendWith(MockitoExtension.class)
class CitizenSendCertificateFromCSTest {

    private static final String CERTIFICATE_ID = "certificateId";
    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    InternalNotificationService internalNotificationService;
    @InjectMocks
    CitizenSendCertificateFromCS citizenSendCertificateFromCS;
    private static final Personnummer PERSONAL_IDENTITY_NUMBER = Personnummer.createPersonnummer("191212121212").orElseThrow();

    @Test
    void shallReturnNullIfCertificateDontExistInCertificateService()
        throws TestCertificateException, CertificateRevokedException, RecipientUnknownException, InvalidCertificateException {
        final var request = SendCertificateRequestDTO.builder()
            .certificateId(CERTIFICATE_ID)
            .build();
        doReturn(false).when(csIntegrationService).certificateExists(CERTIFICATE_ID);

        final var result = citizenSendCertificateFromCS.send(request);
        assertNull(result);
    }

    @Test
    void shallBuildValidSendCertificateRequestDTO()
        throws TestCertificateException, CertificateRevokedException, RecipientUnknownException, InvalidCertificateException {
        final var expectedRequest = SendCitizenCertificateRequestDTO.builder()
            .personId(
                PersonIdDTO.builder()
                    .id(PERSONAL_IDENTITY_NUMBER.getOriginalPnr())
                    .type(PersonIdTypeDTO.PERSONAL_IDENTITY_NUMBER)
                    .build()
            )
            .build();
        final var argumentCaptor = ArgumentCaptor.forClass(SendCitizenCertificateRequestDTO.class);
        final var request = SendCertificateRequestDTO.builder()
            .certificateId(CERTIFICATE_ID)
            .patientId(PERSONAL_IDENTITY_NUMBER)
            .build();

        final var certificate = new Certificate();

        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doReturn(certificate).when(csIntegrationService).sendCitizenCertificates(argumentCaptor.capture(), eq(CERTIFICATE_ID));

        citizenSendCertificateFromCS.send(request);
        assertEquals(argumentCaptor.getValue(), expectedRequest);
    }

    @Test
    void shallCallNotifyCareIfSentByCitizen()
        throws TestCertificateException, CertificateRevokedException, RecipientUnknownException, InvalidCertificateException {
        final var argumentCaptor = ArgumentCaptor.forClass(SendCitizenCertificateRequestDTO.class);
        final var request = SendCertificateRequestDTO.builder()
            .certificateId(CERTIFICATE_ID)
            .patientId(PERSONAL_IDENTITY_NUMBER)
            .build();

        final var certificate = new Certificate();

        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doReturn(certificate).when(csIntegrationService).sendCitizenCertificates(argumentCaptor.capture(), eq(CERTIFICATE_ID));

        citizenSendCertificateFromCS.send(request);
        verify(internalNotificationService).notifyCareIfSentByCitizen(certificate, PERSONAL_IDENTITY_NUMBER.getOriginalPnr(), null);
    }

    @Test
    void shallReturnSendStatusOk()
        throws TestCertificateException, CertificateRevokedException, RecipientUnknownException, InvalidCertificateException {
        final var requestToCS = SendCitizenCertificateRequestDTO.builder()
            .personId(
                PersonIdDTO.builder()
                    .id(PERSONAL_IDENTITY_NUMBER.getOriginalPnr())
                    .type(PersonIdTypeDTO.PERSONAL_IDENTITY_NUMBER)
                    .build()
            )
            .build();

        final var request = SendCertificateRequestDTO.builder()
            .certificateId(CERTIFICATE_ID)
            .patientId(PERSONAL_IDENTITY_NUMBER)
            .build();

        final var certificate = new Certificate();

        doReturn(true).when(csIntegrationService).certificateExists(CERTIFICATE_ID);
        doReturn(certificate).when(csIntegrationService).sendCitizenCertificates(requestToCS, CERTIFICATE_ID);

        final var result = citizenSendCertificateFromCS.send(request);
        assertEquals(SendStatus.OK, result);
    }
}