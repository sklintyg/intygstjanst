package se.inera.intyg.intygstjanst.web.csintegration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.web.csintegration.dto.EraseCertificatesRequestDTO;

@ExtendWith(MockitoExtension.class)
class EraseCertificatesFromCSTest {

    @Mock
    CSIntegrationService csIntegrationService;
    @InjectMocks
    EraseCertificatesFromCS eraseCertificatesFromCS;

    @Test
    void shallIncludeCareProviderId() {
        final var expectedCareProviderId = "expectedCareProviderId";
        final var argumentCaptor = ArgumentCaptor.forClass(String.class);

        eraseCertificatesFromCS.eraseCertificates(expectedCareProviderId, 10);
        verify(csIntegrationService).eraseCertificatesForCareProvider(any(EraseCertificatesRequestDTO.class), argumentCaptor.capture());
        assertEquals(expectedCareProviderId, argumentCaptor.getValue());
    }

    @Test
    void shallIncludeBatchSize() {
        final var expectedBatchSize = 10;
        final var argumentCaptor = ArgumentCaptor.forClass(EraseCertificatesRequestDTO.class);

        eraseCertificatesFromCS.eraseCertificates("careProviderId", expectedBatchSize);
        verify(csIntegrationService).eraseCertificatesForCareProvider(argumentCaptor.capture(), eq("careProviderId"));
        assertEquals(expectedBatchSize, argumentCaptor.getValue().getBatchSize());
    }
}