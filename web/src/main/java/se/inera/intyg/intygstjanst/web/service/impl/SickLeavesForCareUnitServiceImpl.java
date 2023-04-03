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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.intygstjanst.web.service.IntygDataService;
import se.inera.intyg.intygstjanst.web.service.SickLeaveInformationService;
import se.inera.intyg.intygstjanst.web.service.SickLeavesForCareUnitService;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveRequestDTO;

@Service
public class SickLeavesForCareUnitServiceImpl implements SickLeavesForCareUnitService {

    private static final Logger LOG = LoggerFactory.getLogger(SickLeavesForCareUnitServiceImpl.class);
    private final SjukfallEngineService sjukfallEngine;
    private final IntygDataService intygDataService;
    private final SickLeaveInformationService sickLeaveInformationService;

    public SickLeavesForCareUnitServiceImpl(SjukfallEngineService sjukfallEngine,
        IntygDataService intygDataService, SickLeaveInformationService sickLeaveInformationService) {
        this.sjukfallEngine = sjukfallEngine;
        this.intygDataService = intygDataService;
        this.sickLeaveInformationService = sickLeaveInformationService;
    }

    @Override
    public List<SjukfallEnhet> getActiveSickLeavesForCareUnit(SickLeaveRequestDTO sickLeaveRequestDTO) {
        if (isNullOrEmpty(sickLeaveRequestDTO.getCareUnitId())) {
            throw new IllegalArgumentException("Parameter care unit id must be non-empty string");
        }
        final var intygParametrar = getIntygParametrar(sickLeaveRequestDTO);
        final var intygData = intygDataService.getIntygData(sickLeaveRequestDTO.getCareUnitId(),
            sickLeaveRequestDTO.getMaxDaysSinceSickLeaveCompleted());
        final var activeSickLeavesForUnit = sjukfallEngine.beraknaSjukfallForEnhet(intygData, intygParametrar);
        final var filteredActiveSickleavesForUnit = filterSickLeaves(sickLeaveRequestDTO.getDoctorId(), sickLeaveRequestDTO.getUnitId(),
            activeSickLeavesForUnit);
        sickLeaveInformationService.updateAndDecorateDoctorName(filteredActiveSickleavesForUnit);
        return filteredActiveSickleavesForUnit;
    }

    private boolean isNullOrEmpty(String careUnitId) {
        return careUnitId == null || careUnitId.length() == 0;
    }

    private static List<SjukfallEnhet> filterSickLeaves(String doctorId, String unitId, List<SjukfallEnhet> activeSickLeavesForUnit) {
        List<SjukfallEnhet> filteredActiveSickleavesForUnit = new ArrayList<>(activeSickLeavesForUnit);
        if (doctorId != null) {
            LOG.debug("Filtering response - a doctor shall only see patients 'sjukfall' he/she has issued certificates.");
            filteredActiveSickleavesForUnit = activeSickLeavesForUnit.stream()
                .filter(sickLeave -> sickLeave.getLakare().getId().equals(doctorId)).collect(Collectors.toList());
        }
        if (unitId != null) {
            LOG.debug("Filtering response - query for care unit, only including 'sjukfall' with active intyg on specified care unit");
            filteredActiveSickleavesForUnit = activeSickLeavesForUnit.stream()
                .filter(sickLeave -> sickLeave.getVardenhet().getId().equals(unitId)).collect(Collectors.toList());
        }
        return filteredActiveSickleavesForUnit;
    }

    private static IntygParametrar getIntygParametrar(SickLeaveRequestDTO sickLeaveRequestDTO) {
        return new IntygParametrar(sickLeaveRequestDTO.getMaxCertificateGap(),
            sickLeaveRequestDTO.getMaxDaysSinceSickLeaveCompleted(),
            LocalDate.now());
    }
}
