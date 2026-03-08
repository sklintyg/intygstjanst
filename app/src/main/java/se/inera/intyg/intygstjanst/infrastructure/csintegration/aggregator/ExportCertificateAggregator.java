/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.intygstjanst.infrastructure.csintegration.aggregator;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.application.export.dto.CertificateExportPageDTO;
import se.inera.intyg.intygstjanst.application.export.dto.CertificateXmlDTO;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.ExportCertificateFromCS;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.CertificateRepository;

@Service
@RequiredArgsConstructor
public class ExportCertificateAggregator {

  private final CertificateRepository certificateRepository;
  private final ExportCertificateFromCS exportCertificateFromCS;

  public CertificateExportPageDTO exportPage(String careProviderId, int collected, int batchSize) {
    final var pageable =
        PageRequest.of(
            collected / batchSize, batchSize, Sort.by(Direction.ASC, "signedDate", "id"));
    final var certificatePage =
        certificateRepository.findCertificatesForCareProvider(careProviderId, pageable);
    final var totalRevoked = certificateRepository.findTotalRevokedForCareProvider(careProviderId);

    final var totalCertificates = certificatePage.getTotalElements();
    final var certificateCount = certificatePage.getNumberOfElements();

    final var certificateXmls =
        shouldGenerateCertificateXmls(collected, totalCertificates)
            ? getCertificateXmls(certificatePage.getContent())
            : new ArrayList<CertificateXmlDTO>();

    final var certificateExportPage =
        CertificateExportPageDTO.of(
            careProviderId, certificateCount, totalCertificates, totalRevoked, certificateXmls);

    return exportCertificateFromCS.addCertificatesFromCS(
        certificateExportPage, careProviderId, collected, batchSize);
  }

  private static boolean shouldGenerateCertificateXmls(int collected, long totalCertificates) {
    return collected < totalCertificates;
  }

  private List<CertificateXmlDTO> getCertificateXmls(List<Certificate> certificates) {
    return certificates.stream()
        .map(
            certificate ->
                new CertificateXmlDTO(
                    certificate.getId(),
                    certificate.getCertificateMetaData().isRevoked(),
                    certificate.getOriginalCertificate().getDocument()))
        .toList();
  }
}
