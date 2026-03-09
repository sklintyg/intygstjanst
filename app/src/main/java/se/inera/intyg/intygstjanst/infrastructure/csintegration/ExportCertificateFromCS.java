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
package se.inera.intyg.intygstjanst.infrastructure.csintegration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.application.export.dto.CertificateExportPageDTO;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.dto.ExportCertificatesRequestDTO;

@Service
@RequiredArgsConstructor
public class ExportCertificateFromCS {

  private final CSIntegrationService csIntegrationService;

  public CertificateExportPageDTO addCertificatesFromCS(
      CertificateExportPageDTO certificateExportPage,
      String careProviderId,
      int collected,
      int batchSize) {
    final var page = calculatePageNumber(certificateExportPage.getTotal(), collected, batchSize);
    final var totalExportForCareProvider =
        csIntegrationService.getInternalTotalExportForCareProvider(careProviderId);

    certificateExportPage.updateRevoked(totalExportForCareProvider.getTotalRevokedCertificates());
    certificateExportPage.updateTotal(totalExportForCareProvider.getTotalCertificates());

    if (!certificateExportPage.getCertificateXmls().isEmpty()) {
      return certificateExportPage;
    }

    final var certificatesForCareProvider =
        csIntegrationService.getInternalExportCertificatesForCareProvider(
            buildExportCertificatesRequest(batchSize, page), careProviderId);

    certificateExportPage.updateCertificateXmls(certificatesForCareProvider);
    return certificateExportPage;
  }

  private static ExportCertificatesRequestDTO buildExportCertificatesRequest(
      int batchSize, int page) {
    return ExportCertificatesRequestDTO.builder().page(page).size(batchSize).build();
  }

  private int calculatePageNumber(long totalFromIT, int collected, int batchSize) {
    final var collectedFromCS = collected - totalFromIT;
    return (int) collectedFromCS / batchSize;
  }
}
