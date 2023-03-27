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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.intygstjanst.web.service.ListActiveSickLeaveService;
import se.inera.intyg.intygstjanst.web.service.SickLeavesForCareUnitService;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveRequestDTO;

@Component
public class SickLeavesForCareUnitServiceImpl implements SickLeavesForCareUnitService {

    private final SjukfallEngineService sjukfallEngine;
    private final ListActiveSickLeaveService listActiveSickLeaveService;

    public SickLeavesForCareUnitServiceImpl(SjukfallEngineService sjukfallEngine,
        ListActiveSickLeaveService listActiveSickLeaveService) {
        this.sjukfallEngine = sjukfallEngine;
        this.listActiveSickLeaveService = listActiveSickLeaveService;
    }

    @Override
    public List<SjukfallEnhet> getActiveSickLeavesForCareUnit(SickLeaveRequestDTO sickLeaveRequestDTO) {
        final var intygParametrar = getIntygParametrar(sickLeaveRequestDTO);
        final var intygData = listActiveSickLeaveService.get(sickLeaveRequestDTO.getUnitId(),
            sickLeaveRequestDTO.getMaxDaysSinceSickLeaveCompleted());
        final var activeSickLeavesForUnit = sjukfallEngine.beraknaSjukfallForEnhet(intygData, intygParametrar);
        return filterSickLeaves(sickLeaveRequestDTO.getDoctorId(), sickLeaveRequestDTO.getCareUnitId(), activeSickLeavesForUnit);
    }

    private static List<SjukfallEnhet> filterSickLeaves(String doctorId, String careUnitId, List<SjukfallEnhet> activeSickLeavesForUnit) {
        List<SjukfallEnhet> filteredActiveSickleavesForUnit;
        if (careUnitId == null) {
            filteredActiveSickleavesForUnit = activeSickLeavesForUnit.stream()
                .filter(sickLeave -> sickLeave.getLakare().getId().equals(doctorId)).collect(Collectors.toList());
        } else {
            filteredActiveSickleavesForUnit = activeSickLeavesForUnit.stream()
                .filter(sickLeave -> sickLeave.getVardenhet().getId().equals(careUnitId)).collect(Collectors.toList());
        }
        return filteredActiveSickleavesForUnit;
    }

    private static IntygParametrar getIntygParametrar(SickLeaveRequestDTO sickLeaveRequestDTO) {
        return new IntygParametrar(sickLeaveRequestDTO.getMaxCertificateGap(),
            sickLeaveRequestDTO.getMaxDaysSinceSickLeaveCompleted(),
            LocalDate.now());
    }
}
