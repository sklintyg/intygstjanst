package se.inera.intyg.intygstjanst.web.csintegration.aggregator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.web.csintegration.EraseCertificatesFromCS;
import se.inera.intyg.intygstjanst.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.intygstjanst.web.service.impl.EraseCertificatesFromIT;

@ExtendWith(MockitoExtension.class)
class EraseCertificatesAggregatorTest {

    private static final String CARE_PROVIDER_ID = "careProviderId";
    private static final int ERASE_PAGE_SIZE = 5;
    @Mock
    EraseCertificatesFromCS eraseCertificatesFromCS;
    @Mock
    EraseCertificatesFromIT eraseCertificatesFromIT;
    @Mock
    CertificateServiceProfile certificateServiceProfile;
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
    void shallNotEraseCertificatesFromCSIfProfileInactive() {
        doReturn(false).when(certificateServiceProfile).active();
        eraseCertificatesAggregator.eraseCertificates(CARE_PROVIDER_ID);
        verifyNoInteractions(eraseCertificatesFromCS);
        verify(eraseCertificatesFromIT).eraseCertificates(CARE_PROVIDER_ID);
    }


    @Test
    void shallEraseCertificatesFromCSIfProfileActive() {
        doReturn(true).when(certificateServiceProfile).active();
        eraseCertificatesAggregator.eraseCertificates(CARE_PROVIDER_ID);
        verify(eraseCertificatesFromCS).eraseCertificates(CARE_PROVIDER_ID);
        verify(eraseCertificatesFromIT).eraseCertificates(CARE_PROVIDER_ID);
    }
}