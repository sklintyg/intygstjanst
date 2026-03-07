package se.inera.intyg.intygstjanst.infrastructure.csintegration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.dto.ExportCertificatesRequestDTO;
import se.inera.intyg.intygstjanst.application.export.dto.CertificateExportPageDTO;

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