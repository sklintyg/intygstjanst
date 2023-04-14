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

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.inera.intyg.intygstjanst.web.service.SickLeaveInformationService;

@Service
public class SickLeaveInformationServiceImpl implements SickLeaveInformationService {

    private final HsaService hsaService;

    public SickLeaveInformationServiceImpl(HsaService hsaService) {
        this.hsaService = hsaService;
    }

    @Override
    public void updateAndDecorateDoctorName(List<SjukfallEnhet> sickLeaves) {
        sickLeaves.forEach(this::updateEmployeeName);
        decorateWithHsaId(sickLeaves);
    }

    @Override
    public Lakare getEmployee(String doctorId) {
        return Lakare.create(doctorId, hsaService.getHsaEmployeeName(doctorId));
    }

    private void updateEmployeeName(SjukfallEnhet sickLeave) {
        if (sickLeave.getLakare() == null) {
            return;
        }
        final var employeeName = hsaService.getHsaEmployeeName(sickLeave.getLakare().getId());
        setEmployeeNameIfFound(sickLeave, employeeName);
    }

    private static void setEmployeeNameIfFound(SjukfallEnhet sickLeave, String employeeHsaName) {
        if (employeeHsaName != null) {
            sickLeave.getLakare().setNamn(employeeHsaName);
        } else {
            sickLeave.getLakare().setNamn(sickLeave.getLakare().getId());
        }
    }

    public void decorateWithHsaId(List<SjukfallEnhet> sickLeaves) {
        long numberOfHsaIds = sickLeaves.stream().map(sickLeave -> sickLeave.getLakare().getId()).distinct().count();
        long numberOfLakareNames = sickLeaves.stream().map(sickLeave -> sickLeave.getLakare().getNamn()).distinct().count();
        if (numberOfHsaIds == numberOfLakareNames) {
            return;
        }
        sickLeaves.stream()
            .map(SjukfallEnhet::getLakare)
            .collect(Collectors.groupingBy(Lakare::getNamn))
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().stream()
                .map(Lakare::getId)
                .distinct()
                .count() > 1)
            .forEach(entry -> entry.getValue().forEach(lakare ->
                lakare.setNamn(lakare.getNamn() + " (" + lakare.getId() + ")")
            ));
    }
}
