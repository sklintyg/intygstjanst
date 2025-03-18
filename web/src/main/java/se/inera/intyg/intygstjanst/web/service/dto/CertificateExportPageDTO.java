/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.service.dto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.inera.intyg.intygstjanst.web.csintegration.dto.ExportCertificateInternalResponseDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertificateExportPageDTO {

    private String careProviderId;
    private int count;
    private long total;
    private long totalRevoked;
    private List<CertificateXmlDTO> certificateXmls;

    public static CertificateExportPageDTO of(String careProviderId, int count, long total, long totalRevoked,
        List<CertificateXmlDTO> certificateXmls) {
        return new CertificateExportPageDTO(careProviderId, count, total, totalRevoked, certificateXmls);
    }

    public void updateCertificateXmls(List<ExportCertificateInternalResponseDTO> exports) {
        if (exports == null || exports.isEmpty()) {
            return;
        }

        final var certificateXmlDTOS = exports.stream()
            .map(buildCertificateXmlDTO())
            .toList();

        this.certificateXmls = Stream.concat(certificateXmlDTOS.stream(), this.certificateXmls.stream()).toList();
        this.count = certificateXmlDTOS.size();
    }

    private Function<ExportCertificateInternalResponseDTO, CertificateXmlDTO> buildCertificateXmlDTO() {
        return export ->
            CertificateXmlDTO.of(
                export.getCertificateId(),
                export.isRevoked(),
                decodeXml(export.getXml())
            );
    }

    private String decodeXml(String xmlBase64Encoded) {
        return new String(Base64.getDecoder().decode(xmlBase64Encoded), StandardCharsets.UTF_8);
    }

    public void updateTotal(long totalCertificates) {
        this.total += totalCertificates;
    }

    public void updateRevoked(long totalRevokedCertificates) {
        this.totalRevoked += totalRevokedCertificates;
    }
}