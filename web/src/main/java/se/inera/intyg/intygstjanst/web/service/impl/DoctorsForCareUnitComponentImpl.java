/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.web.service.DoctorsForCareUnitComponent;
import se.inera.intyg.intygstjanst.web.service.SickLeaveInformationService;

@Component
public class DoctorsForCareUnitComponentImpl implements DoctorsForCareUnitComponent {

    private final SickLeaveInformationService sickLeaveInformationService;

    public DoctorsForCareUnitComponentImpl(SickLeaveInformationService sickLeaveInformationService) {
        this.sickLeaveInformationService = sickLeaveInformationService;
    }

    @Override
    public List<Lakare> getDoctorsForCareUnit(List<SjukfallCertificate> sickLeaveCertificates) {
        final var doctorsWithActiveSickLeave = extractDoctorsFromCertificateAndSortedByName(sickLeaveCertificates);
        decorateWithHsaId(doctorsWithActiveSickLeave);
        return doctorsWithActiveSickLeave;
    }

    private List<Lakare> extractDoctorsFromCertificateAndSortedByName(List<SjukfallCertificate> doctorIds) {
        return doctorIds.stream()
            .map(SjukfallCertificate::getSigningDoctorId)
            .filter(Objects::nonNull)
            .map(sickLeaveInformationService::getEmployee)
            .distinct()
            .sorted(Comparator.comparing(Lakare::getNamn))
            .collect(Collectors.toList());
    }

    private void decorateWithHsaId(List<Lakare> doctorList) {
        doctorList.stream()
            .collect(Collectors.groupingBy(Lakare::getNamn))
            .forEach((name, doctorNames) -> {
                if (doctorNames.size() > 1) {
                    doctorNames.forEach(lakare -> lakare.setNamn(lakare.getNamn() + " (" + lakare.getId() + ")"));
                }
            });
    }
}
