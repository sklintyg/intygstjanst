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

import static se.inera.intyg.intygstjanst.web.integration.sickleave.SickLeaveLogMessageFactory.INTYG_DATA_SERVICE;
import static se.inera.intyg.intygstjanst.web.integration.sickleave.SickLeaveLogMessageFactory.SICK_LEAVE_INFORMATION;

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
import se.inera.intyg.intygstjanst.web.integration.sickleave.SickLeaveLogMessageFactory;
import se.inera.intyg.intygstjanst.web.service.DiagnosisChapterService;
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
    private final DiagnosisChapterService diagnosisChapterService;

    public SickLeavesForCareUnitServiceImpl(SjukfallEngineService sjukfallEngine,
        IntygDataService intygDataService, SickLeaveInformationService sickLeaveInformationService,
        DiagnosisChapterService diagnosisChapterService) {
        this.sjukfallEngine = sjukfallEngine;
        this.intygDataService = intygDataService;
        this.sickLeaveInformationService = sickLeaveInformationService;
        this.diagnosisChapterService = diagnosisChapterService;
    }

    @Override
    public List<SjukfallEnhet> getActiveSickLeavesForCareUnit(SickLeaveRequestDTO sickLeaveRequestDTO) {
        if (isNullOrEmpty(sickLeaveRequestDTO.getCareUnitId())) {
            throw new IllegalArgumentException("Parameter care unit id must be non-empty string");
        }
        final var intygParametrar = getIntygParametrar(sickLeaveRequestDTO);

        final var sickLeaveLogFactory = new SickLeaveLogMessageFactory(System.currentTimeMillis());
        final var intygData = intygDataService.getIntygData(sickLeaveRequestDTO.getCareUnitId(),
            sickLeaveRequestDTO.getMaxDaysSinceSickLeaveCompleted());
        LOG.debug(sickLeaveLogFactory.message(INTYG_DATA_SERVICE, intygData.size()));

        final var activeSickLeavesForUnit = sjukfallEngine.beraknaSjukfallForEnhet(intygData, intygParametrar);
        final var filteredActiveSickleavesForUnit = filterSickLeaves(sickLeaveRequestDTO,
            activeSickLeavesForUnit);

        sickLeaveLogFactory.setStartTimer(System.currentTimeMillis());
        sickLeaveInformationService.updateAndDecorateDoctorName(filteredActiveSickleavesForUnit);
        LOG.debug(sickLeaveLogFactory.message(SICK_LEAVE_INFORMATION, filteredActiveSickleavesForUnit.size()));

        return filteredActiveSickleavesForUnit;
    }

    private boolean isNullOrEmpty(String careUnitId) {
        return careUnitId == null || careUnitId.length() == 0;
    }

    private List<SjukfallEnhet> filterSickLeaves(SickLeaveRequestDTO sickLeaveRequestDTO,
        List<SjukfallEnhet> activeSickLeavesForUnit) {
        List<SjukfallEnhet> filteredActiveSickleavesForUnit = new ArrayList<>(activeSickLeavesForUnit);
        if (sickLeaveRequestDTO.getDoctorIds() != null && !sickLeaveRequestDTO.getDoctorIds().isEmpty()) {
            LOG.debug("Filtering response - a doctor shall only see patients 'sjukfall' he/she has issued certificates. DoctorId: {}",
                sickLeaveRequestDTO.getDoctorIds());
            filteredActiveSickleavesForUnit = filterOnDoctorId(sickLeaveRequestDTO, filteredActiveSickleavesForUnit);
        }
        if (sickLeaveRequestDTO.getUnitId() != null) {
            LOG.debug("Filtering response - query for unit, only including 'sjukfall' with active intyg on unit: {}",
                sickLeaveRequestDTO.getUnitId());
            filteredActiveSickleavesForUnit = filterOnUnitId(sickLeaveRequestDTO, filteredActiveSickleavesForUnit);
        }
        if (sickLeaveRequestDTO.getFromSickLeaveLength() != null && sickLeaveRequestDTO.getToSickLeaveLength() != null) {
            LOG.debug("Filtering response - only including 'sjukfall' with 'dagar' greater than: {} and smaller then: {}",
                sickLeaveRequestDTO.getFromSickLeaveLength(), sickLeaveRequestDTO.getToSickLeaveLength());
            filteredActiveSickleavesForUnit = filterOnSickLeaveLength(sickLeaveRequestDTO, filteredActiveSickleavesForUnit);
        }
        if (sickLeaveRequestDTO.getDiagnosisChapters() != null && !sickLeaveRequestDTO.getDiagnosisChapters().isEmpty()) {
            LOG.debug("Filtering response - only including 'sjukfall' with diagnosis code: {}", sickLeaveRequestDTO.getDiagnosisChapters());
            filteredActiveSickleavesForUnit = filterOnDiagnosisChapter(sickLeaveRequestDTO, filteredActiveSickleavesForUnit);
        }
        return filteredActiveSickleavesForUnit;
    }

    private List<SjukfallEnhet> filterOnDoctorId(SickLeaveRequestDTO sickLeaveRequestDTO,
        List<SjukfallEnhet> filteredActiveSickleavesForUnit) {
        return filteredActiveSickleavesForUnit.stream()
            .filter(sickLeave -> sickLeaveRequestDTO.getDoctorIds().contains(sickLeave.getLakare().getId()))
            .collect(Collectors.toList());
    }

    private List<SjukfallEnhet> filterOnDiagnosisChapter(SickLeaveRequestDTO sickLeaveRequestDTO,
        List<SjukfallEnhet> filteredActiveSickleavesForUnit) {
        return filteredActiveSickleavesForUnit.stream()
            .filter(sickLeave -> sickLeaveRequestDTO.getDiagnosisChapters()
                .contains(diagnosisChapterService.getDiagnosisChaptersFromSickLeave(sickLeave)))
            .collect(Collectors.toList());
    }

    private List<SjukfallEnhet> filterOnSickLeaveLength(SickLeaveRequestDTO sickLeaveRequestDTO,
        List<SjukfallEnhet> filteredActiveSickleavesForUnit) {
        return filteredActiveSickleavesForUnit.stream()
            .filter(sickLeave -> sickLeave.getDagar() >= sickLeaveRequestDTO.getFromSickLeaveLength()
                && sickLeave.getDagar() <= sickLeaveRequestDTO.getToSickLeaveLength())
            .collect(Collectors.toList());
    }

    private List<SjukfallEnhet> filterOnUnitId(SickLeaveRequestDTO sickLeaveRequestDTO,
        List<SjukfallEnhet> filteredActiveSickleavesForUnit) {
        return filteredActiveSickleavesForUnit.stream()
            .filter(sickLeave -> sickLeave.getVardenhet().getId().equals(sickLeaveRequestDTO.getUnitId())).collect(Collectors.toList());
    }

    private IntygParametrar getIntygParametrar(SickLeaveRequestDTO sickLeaveRequestDTO) {
        return new IntygParametrar(sickLeaveRequestDTO.getMaxCertificateGap(),
            sickLeaveRequestDTO.getMaxDaysSinceSickLeaveCompleted(),
            LocalDate.now());
    }
}
