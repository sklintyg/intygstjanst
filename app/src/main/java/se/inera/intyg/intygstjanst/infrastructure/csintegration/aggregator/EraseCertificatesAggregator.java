package se.inera.intyg.intygstjanst.infrastructure.csintegration.aggregator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.EraseCertificatesFromCS;
import se.inera.intyg.intygstjanst.application.erase.EraseCertificatesFromIT;

@Service
@RequiredArgsConstructor
public class EraseCertificatesAggregator {

    private final EraseCertificatesFromCS eraseCertificatesFromCS;
    private final EraseCertificatesFromIT eraseCertificatesFromIT;

    public void eraseCertificates(String careProviderId) {
        if (careProviderId == null || careProviderId.isBlank()) {
            throw new IllegalArgumentException("careProviderId cannot be null or empty");
        }

        eraseCertificatesFromIT.eraseCertificates(careProviderId);

        eraseCertificatesFromCS.eraseCertificates(careProviderId);
    }
}