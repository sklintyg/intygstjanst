package se.inera.intyg.intygstjanst.web.csintegration.aggregator;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateRepository;
import se.inera.intyg.intygstjanst.web.csintegration.ExportCertificateFromCS;
import se.inera.intyg.intygstjanst.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateExportPageDTO;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateXmlDTO;

@Service
@RequiredArgsConstructor
public class ExportCertificateAggregator {

    private final CertificateRepository certificateRepository;
    private final CertificateServiceProfile certificateServiceProfile;
    private final ExportCertificateFromCS exportCertificateFromCS;

    public CertificateExportPageDTO exportPage(String careProviderId, int collected, int batchSize) {
        final var pageable = PageRequest.of(collected / batchSize, batchSize, Sort.by(Direction.ASC, "signedDate", "id"));
        final var certificatePage = certificateRepository.findCertificatesForCareProvider(careProviderId, pageable);
        final var totalRevoked = certificateRepository.findTotalRevokedForCareProvider(careProviderId);

        final var totalCertificates = certificatePage.getTotalElements();
        final var certificateCount = certificatePage.getNumberOfElements();

        final var certificateXmls = shouldGenerateCertificateXmls(collected, totalCertificates)
            ? getCertificateXmls(certificatePage.getContent())
            : new ArrayList<CertificateXmlDTO>();

        final var certificateExportPage = CertificateExportPageDTO.of(
            careProviderId,
            certificateCount,
            totalCertificates,
            totalRevoked,
            certificateXmls
        );

        if (Boolean.FALSE.equals(certificateServiceProfile.active())) {
            return certificateExportPage;
        }

        return exportCertificateFromCS.addCertificatesFromCS(certificateExportPage, careProviderId, collected, batchSize);
    }

    private static boolean shouldGenerateCertificateXmls(int collected, long totalCertificates) {
        return collected < totalCertificates;
    }

    private List<CertificateXmlDTO> getCertificateXmls(List<Certificate> certificates) {
        return certificates.stream()
            .map(certificate -> new CertificateXmlDTO(
                certificate.getId(),
                certificate.getCertificateMetaData().isRevoked(),
                certificate.getOriginalCertificate().getDocument()))
            .toList();
    }
}