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
package se.inera.intyg.intygstjanst.application.sickleave.converter;

import org.springframework.stereotype.Component;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.dto.SickLeaveCertificateDTO;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.builder.SjukfallCertificateBuilder;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.SjukfallCertificateWorkCapacity;

@Component
public class SickLeaveCertificateToSjukfallCertificateConverter {

  public SjukfallCertificate convert(SickLeaveCertificateDTO sickLeaveCertificate) {
    if (sickLeaveCertificate == null) {
      throw new IllegalStateException("Sick leave certificate cannot be null");
    }

    final var sjukfallCertificate =
        new SjukfallCertificateBuilder(sickLeaveCertificate.getId())
            .certificateType(sickLeaveCertificate.getType())
            .signingDoctorId(sickLeaveCertificate.getSigningDoctorId())
            .signingDoctorName(sickLeaveCertificate.getSigningDoctorName())
            .signingDateTime(sickLeaveCertificate.getSigningDateTime())
            .careUnitId(sickLeaveCertificate.getCareUnitId())
            .careUnitName(sickLeaveCertificate.getCareUnitName())
            .careGiverId(sickLeaveCertificate.getCareGiverId())
            .civicRegistrationNumber(sickLeaveCertificate.getCivicRegistrationNumber())
            .patientName(sickLeaveCertificate.getPatientName())
            .diagnoseCode(sickLeaveCertificate.getDiagnoseCode())
            .employment(sickLeaveCertificate.getEmployment())
            .deleted(sickLeaveCertificate.getDeleted())
            .testCertificate(sickLeaveCertificate.isTestCertificate())
            .workCapacities(
                sickLeaveCertificate.getSjukfallCertificateWorkCapacity().stream()
                    .map(
                        workCapacity -> {
                          final var workCapacityEntity = new SjukfallCertificateWorkCapacity();
                          workCapacityEntity.setFromDate(workCapacity.getFromDate());
                          workCapacityEntity.setToDate(workCapacity.getToDate());
                          workCapacityEntity.setCapacityPercentage(
                              workCapacity.getCapacityPercentage());
                          return workCapacityEntity;
                        })
                    .toList())
            .build();

    sjukfallCertificate.setBiDiagnoseCode1(sickLeaveCertificate.getBiDiagnoseCode1());
    sjukfallCertificate.setBiDiagnoseCode2(sickLeaveCertificate.getBiDiagnoseCode2());

    return sjukfallCertificate;
  }
}
