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
package se.inera.intyg.intygstjanst.application.citizen.service;

import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.application.citizen.dto.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.application.citizen.dto.CitizenCertificateIssuerDTO;
import se.inera.intyg.intygstjanst.application.citizen.dto.CitizenCertificateSummaryDTO;
import se.inera.intyg.intygstjanst.application.citizen.dto.CitizenCertificateTypeDTO;
import se.inera.intyg.intygstjanst.application.citizen.dto.CitizenCertificateUnitDTO;
import se.inera.intyg.intygstjanst.application.citizen.repository.model.CitizenCertificate;

@Service
public class CitizenCertificateDTOConverter {

  private final CitizenCertificateRecipientConverter citizenCertificateRecipientConverter;

  public CitizenCertificateDTOConverter(
      CitizenCertificateRecipientConverter citizenCertificateRecipientConverter) {
    this.citizenCertificateRecipientConverter = citizenCertificateRecipientConverter;
  }

  public CitizenCertificateDTO convert(
      CitizenCertificate certificate, String typeName, String summaryLabel) {
    return CitizenCertificateDTO.builder()
        .id(certificate.getId())
        .type(getType(typeName, certificate.getType(), certificate.getTypeVersion()))
        .summary(getSummary(certificate.getAdditionalInfo(), summaryLabel))
        .issuer(getIssuer(certificate.getIssuerName()))
        .unit(getUnit(certificate.getUnitId(), certificate.getUnitName()))
        .recipient(
            citizenCertificateRecipientConverter
                .convert(certificate.getType(), certificate.getSentDate())
                .orElse(null))
        .issued(certificate.getIssued())
        .relations(certificate.getRelations())
        .build();
  }

  private CitizenCertificateIssuerDTO getIssuer(String name) {
    return CitizenCertificateIssuerDTO.builder().name(name).build();
  }

  private CitizenCertificateTypeDTO getType(String name, String id, String version) {
    return CitizenCertificateTypeDTO.builder().id(id).name(name).version(version).build();
  }

  private CitizenCertificateSummaryDTO getSummary(String value, String label) {
    return CitizenCertificateSummaryDTO.builder().value(value).label(label).build();
  }

  private CitizenCertificateUnitDTO getUnit(String id, String name) {
    return CitizenCertificateUnitDTO.builder().id(id).name(name).build();
  }
}
