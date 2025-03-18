package se.inera.intyg.intygstjanst.web.csintegration;

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