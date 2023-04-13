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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.web.service.GetDoctorsFromSickLeaves;
import se.inera.intyg.intygstjanst.web.service.SickLeaveInformationService;

@Component
public class GetDoctorsFromSickLeavesImpl implements GetDoctorsFromSickLeaves {

    private static final Logger LOG = LoggerFactory.getLogger(GetDoctorsFromSickLeavesImpl.class);
    private final SickLeaveInformationService sickLeaveInformationService;

    public GetDoctorsFromSickLeavesImpl(SickLeaveInformationService sickLeaveInformationService) {
        this.sickLeaveInformationService = sickLeaveInformationService;
    }

    @Override
    public List<Lakare> getDoctors(List<SjukfallCertificate> sickLeaveCertificates, String doctorId) {
        if (doctorId != null) {
            LOG.debug("Returning empty list of doctors since doctorId: {} was provided", doctorId);
            return Collections.emptyList();
        }
        final var doctors = extractDoctorsFromCertificateAndSortedByName(sickLeaveCertificates);
        decorateDuplicateNamesWithHsaId(doctors);
        return doctors;
    }

    private List<Lakare> extractDoctorsFromCertificateAndSortedByName(List<SjukfallCertificate> sickLeaveCertificate) {
        return sickLeaveCertificate.stream()
            .map(SjukfallCertificate::getSigningDoctorId)
            .filter(Objects::nonNull)
            .distinct()
            .map(sickLeaveInformationService::getEmployee)
            .sorted(Comparator.comparing(Lakare::getNamn))
            .collect(Collectors.toList());
    }

    private void decorateDuplicateNamesWithHsaId(List<Lakare> doctorList) {
        doctorList.stream()
            .collect(Collectors.groupingBy(Lakare::getNamn))
            .forEach((name, doctorNames) -> {
                if (doctorNames.size() > 1) {
                    doctorNames.forEach(lakare -> lakare.setNamn(lakare.getNamn() + " (" + lakare.getId() + ")"));
                }
            });
    }
}
