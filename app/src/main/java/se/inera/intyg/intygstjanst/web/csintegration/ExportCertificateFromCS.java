package se.inera.intyg.intygstjanst.web.csintegration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.csintegration.dto.ExportCertificatesRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateExportPageDTO;

@Service
@RequiredArgsConstructor
public class ExportCertificateFromCS {

    private final CSIntegrationService csIntegrationService;

    public CertificateExportPageDTO addCertificatesFromCS(CertificateExportPageDTO certificateExportPage, String careProviderId,
        int collected, int batchSize) {
        final var page = calculatePageNumber(certificateExportPage.getTotal(), collected, batchSize);
        final var totalExportForCareProvider = csIntegrationService.getInternalTotalExportForCareProvider(careProviderId);

        certificateExportPage.updateRevoked(totalExportForCareProvider.getTotalRevokedCertificates());
        certificateExportPage.updateTotal(totalExportForCareProvider.getTotalCertificates());

        if (!certificateExportPage.getCertificateXmls().isEmpty()) {
            return certificateExportPage;
        }

        final var certificatesForCareProvider = csIntegrationService.getInternalExportCertificatesForCareProvider(
            buildExportCertificatesRequest(batchSize, page),
            careProviderId
        );

        certificateExportPage.updateCertificateXmls(certificatesForCareProvider);
        return certificateExportPage;
    }

    private static ExportCertificatesRequestDTO buildExportCertificatesRequest(int batchSize, int page) {
        return ExportCertificatesRequestDTO.builder()
            .page(page)
            .size(batchSize)
            .build();
    }

    private int calculatePageNumber(long totalFromIT, int collected, int batchSize) {
        final var collectedFromCS = collected - totalFromIT;
        return (int) collectedFromCS / batchSize;
    }
}