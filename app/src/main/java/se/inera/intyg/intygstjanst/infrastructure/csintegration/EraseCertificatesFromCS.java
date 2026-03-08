package se.inera.intyg.intygstjanst.infrastructure.csintegration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EraseCertificatesFromCS {

    private final CSIntegrationService csIntegrationService;

    public void eraseCertificates(String careProviderId) {
        csIntegrationService.eraseCertificatesForCareProvider(
            careProviderId
        );
    }
}