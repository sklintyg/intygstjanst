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

package se.inera.intyg.intygstjanst.application.sickleave;

import static se.inera.intyg.intygstjanst.application.sickleave.services.SickLeaveLogMessageFactory.GET_SICK_LEAVE_ACTIVE;
import static se.inera.intyg.intygstjanst.application.sickleave.services.SickLeaveLogMessageFactory.GET_SICK_LEAVE_FILTER;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.intygstjanst.infrastructure.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.infrastructure.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.application.sickleave.services.SickLeaveLogMessageFactory;
import se.inera.intyg.intygstjanst.application.sickleave.services.GetSickLeaveFilterService;
import se.inera.intyg.intygstjanst.application.sickleave.services.GetSickLeavesService;
import se.inera.intyg.intygstjanst.application.sickleave.dto.GetSickLeaveFilterServiceRequest;
import se.inera.intyg.intygstjanst.application.sickleave.dto.GetSickLeaveServiceRequest;
import se.inera.intyg.intygstjanst.application.sickleave.dto.PopulateFiltersRequestDTO;
import se.inera.intyg.intygstjanst.application.sickleave.dto.PopulateFiltersResponseDTO;
import se.inera.intyg.intygstjanst.infrastructure.security.interceptor.ApiBasePath;
import se.inera.intyg.intygstjanst.application.sickleave.dto.SickLeaveRequestDTO;
import se.inera.intyg.intygstjanst.application.sickleave.dto.SickLeaveResponseDTO;

@RestController
@ApiBasePath("/internalapi")
@RequestMapping("/sickleave")
@RequiredArgsConstructor
@Slf4j
public class SickLeaveController {

    private final GetSickLeavesService getSickLeavesService;
    private final GetSickLeaveFilterService getSickLeaveFilterService;

    @PostMapping("/active")
    @PerformanceLogging(eventAction = "list-sick-leaves", eventType = MdcLogConstants.EVENT_TYPE_ACCESSED)
    public SickLeaveResponseDTO getActiveSickLeavesForCareUnit(@RequestBody SickLeaveRequestDTO sickLeaveRequestDTO) {
        final var sickLeaveLogMessageFactory = new SickLeaveLogMessageFactory(System.currentTimeMillis());
        final var sjukfallEnhetList = getSickLeavesService.get(
            GetSickLeaveServiceRequest.builder()
                .careUnitId(sickLeaveRequestDTO.getCareUnitId())
                .unitId(sickLeaveRequestDTO.getUnitId())
                .maxDaysSinceSickLeaveCompleted(sickLeaveRequestDTO.getMaxDaysSinceSickLeaveCompleted())
                .doctorIds(sickLeaveRequestDTO.getDoctorIds())
                .maxCertificateGap(sickLeaveRequestDTO.getMaxCertificateGap())
                .sickLeaveLengthIntervals(sickLeaveRequestDTO.getSickLeaveLengthIntervals())
                .diagnosisChapters(sickLeaveRequestDTO.getDiagnosisChapters())
                .fromPatientAge(sickLeaveRequestDTO.getFromPatientAge())
                .toPatientAge(sickLeaveRequestDTO.getToPatientAge())
                .protectedPersonFilterId(sickLeaveRequestDTO.getProtectedPersonFilterId())
                .fromSickLeaveEndDate(sickLeaveRequestDTO.getFromSickLeaveEndDate())
                .toSickLeaveEndDate(sickLeaveRequestDTO.getToSickLeaveEndDate())
                .rekoStatusTypeIds(sickLeaveRequestDTO.getRekoStatusTypeIds())
                .occupationTypeIds(sickLeaveRequestDTO.getOccupationTypeIds())
                .textSearch(sickLeaveRequestDTO.getTextSearch())
                .build()
        );

        if (log.isInfoEnabled()) {
            log.info(sickLeaveLogMessageFactory.message(GET_SICK_LEAVE_ACTIVE, sjukfallEnhetList.size()));
        }

        return new SickLeaveResponseDTO(sjukfallEnhetList);
    }

    @PostMapping("/filters")
    @PerformanceLogging(eventAction = "list-sick-leaves-filter", eventType = MdcLogConstants.EVENT_TYPE_ACCESSED)
    public PopulateFiltersResponseDTO populateFilters(@RequestBody PopulateFiltersRequestDTO populateFiltersRequestDTO) {
        final var sickLeaveLogMessageFactory = new SickLeaveLogMessageFactory(System.currentTimeMillis());
        final var getSickLeaveFilterServiceResponse = getSickLeaveFilterService.get(
            GetSickLeaveFilterServiceRequest.builder()
                .careUnitId(populateFiltersRequestDTO.getCareUnitId())
                .unitId(populateFiltersRequestDTO.getUnitId())
                .doctorId(populateFiltersRequestDTO.getDoctorId())
                .maxDaysSinceSickLeaveCompleted(populateFiltersRequestDTO.getMaxDaysSinceSickLeaveCompleted())
                .protectedPersonFilterId(populateFiltersRequestDTO.getProtectedPersonFilterId())
                .build()
        );

        if (log.isInfoEnabled()) {
            log.info(sickLeaveLogMessageFactory.message(GET_SICK_LEAVE_FILTER));
        }

        return new PopulateFiltersResponseDTO(
            getSickLeaveFilterServiceResponse.getActiveDoctors(),
            getSickLeaveFilterServiceResponse.getDiagnosisChapters(),
            getSickLeaveFilterServiceResponse.getNbrOfSickLeaves(),
            getSickLeaveFilterServiceResponse.isHasOngoingSickLeaves(),
            getSickLeaveFilterServiceResponse.getRekoStatusTypes(),
            getSickLeaveFilterServiceResponse.getOccupationTypes()
        );
    }
}
