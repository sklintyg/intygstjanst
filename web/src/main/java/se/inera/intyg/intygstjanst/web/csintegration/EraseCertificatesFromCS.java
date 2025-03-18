package se.inera.intyg.intygstjanst.web.csintegration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.csintegration.dto.EraseCertificatesRequestDTO;

@Service
@RequiredArgsConstructor
public class EraseCertificatesFromCS {

    private final CSIntegrationService csIntegrationService;

    public void eraseCertificates(String careProviderId, int erasePageSize) {
        csIntegrationService.eraseCertificatesForCareProvider(
            EraseCertificatesRequestDTO.builder()
                .careProviderId(careProviderId)
                .erasePageSize(erasePageSize)
                .build()
        );
    }
}