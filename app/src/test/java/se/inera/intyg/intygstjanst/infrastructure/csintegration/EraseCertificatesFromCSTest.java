package se.inera.intyg.intygstjanst.infrastructure.csintegration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.CSIntegrationService;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.EraseCertificatesFromCS;

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

        eraseCertificatesFromCS.eraseCertificates(expectedCareProviderId);
        verify(csIntegrationService).eraseCertificatesForCareProvider(argumentCaptor.capture());
        assertEquals(expectedCareProviderId, argumentCaptor.getValue());
    }
}