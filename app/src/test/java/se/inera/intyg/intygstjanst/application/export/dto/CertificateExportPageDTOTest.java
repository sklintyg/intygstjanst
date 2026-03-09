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
package se.inera.intyg.intygstjanst.application.export.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.dto.ExportCertificateInternalResponseDTO;

class CertificateExportPageDTOTest {

  @Test
  void shallUpdateTotal() {
    final var exportPageDTO =
        CertificateExportPageDTO.of("careProvider", 0, 0, 0, Collections.emptyList());
    exportPageDTO.updateTotal(10);
    assertEquals(10, exportPageDTO.getTotal());
  }

  @Test
  void shallUpdateRevoked() {
    final var exportPageDTO =
        CertificateExportPageDTO.of("careProvider", 0, 0, 0, Collections.emptyList());
    exportPageDTO.updateRevoked(10);
    assertEquals(10, exportPageDTO.getTotalRevoked());
  }

  @Test
  void shallUpdateCertificateXmls() {
    final var expectedCertificateXmlDTO =
        CertificateXmlDTO.of(
            "id", false, new String(Base64.getDecoder().decode("xml"), StandardCharsets.UTF_8));

    final var exportCertificateInternalResponseDTO =
        ExportCertificateInternalResponseDTO.builder()
            .certificateId("id")
            .revoked(false)
            .xml("xml")
            .build();

    final var exportPageDTO =
        CertificateExportPageDTO.of("careProvider", 0, 0, 0, Collections.emptyList());

    exportPageDTO.updateCertificateXmls(
        Collections.singletonList(exportCertificateInternalResponseDTO));

    assertEquals(1, exportPageDTO.getCount());
    assertEquals(1, exportPageDTO.getCertificateXmls().size());
    assertEquals(expectedCertificateXmlDTO, exportPageDTO.getCertificateXmls().getFirst());
  }

  @Test
  void shallUpdateCount() {
    final var exportPageDTO =
        CertificateExportPageDTO.of("careProvider", 0, 0, 0, Collections.emptyList());

    exportPageDTO.updateCertificateXmls(
        Collections.singletonList(
            ExportCertificateInternalResponseDTO.builder()
                .certificateId("id")
                .revoked(false)
                .xml("xml")
                .build()));

    assertEquals(1, exportPageDTO.getCount());
  }
}
