package se.inera.intyg.intygstjanst.infrastructure.csintegration.aggregator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.EraseCertificatesFromCS;
import se.inera.intyg.intygstjanst.application.erase.EraseCertificatesFromIT;

@ExtendWith(MockitoExtension.class)
class EraseCertificatesAggregatorTest {

    private static final String CARE_PROVIDER_ID = "careProviderId";
    @Mock
    EraseCertificatesFromCS eraseCertificatesFromCS;
    @Mock
    EraseCertificatesFromIT eraseCertificatesFromIT;
    @InjectMocks
    EraseCertificatesAggregator eraseCertificatesAggregator;

    @Test
    void shallThrowExceptionIfCareProviderIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
            eraseCertificatesAggregator.eraseCertificates(null));
    }

    @Test
    void shallThrowExceptionIfCareProviderIdIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
            eraseCertificatesAggregator.eraseCertificates(""));
    }

    @Test
    void shallEraseCertificatesFromCS() {
        eraseCertificatesAggregator.eraseCertificates(CARE_PROVIDER_ID);
        verify(eraseCertificatesFromCS).eraseCertificates(CARE_PROVIDER_ID);
        verify(eraseCertificatesFromIT).eraseCertificates(CARE_PROVIDER_ID);
    }
}