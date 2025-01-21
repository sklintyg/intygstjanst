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

package se.inera.intyg.intygstjanst.web.integration.sickleave;

import static se.inera.intyg.intygstjanst.web.integration.sickleave.SickLeaveLogMessageFactory.GET_SICK_LEAVE_ACTIVE;
import static se.inera.intyg.intygstjanst.web.integration.sickleave.SickLeaveLogMessageFactory.GET_SICK_LEAVE_FILTER;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.web.service.GetSickLeaveFilterService;
import se.inera.intyg.intygstjanst.web.service.GetSickLeavesService;
import se.inera.intyg.intygstjanst.web.service.dto.GetSickLeaveFilterServiceRequest;
import se.inera.intyg.intygstjanst.web.service.dto.GetSickLeaveServiceRequest;
import se.inera.intyg.intygstjanst.web.service.dto.PopulateFiltersRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.PopulateFiltersResponseDTO;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveResponseDTO;

@Path("/sickleave")
public class SickLeaveController {

    private static final Logger LOG = LoggerFactory.getLogger(SickLeaveController.class);
    private static final String UTF_8_CHARSET = ";charset=utf-8";
    private final GetSickLeavesService getSickLeavesService;
    private final GetSickLeaveFilterService getSickLeaveFilterService;

    public SickLeaveController(GetSickLeavesService getSickLeavesService, GetSickLeaveFilterService getSickLeaveFilterService) {
        this.getSickLeavesService = getSickLeavesService;
        this.getSickLeaveFilterService = getSickLeaveFilterService;
    }

    @PrometheusTimeMethod
    @POST
    @Path("/active")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    @PerformanceLogging(eventAction = "list-sick-leaves", eventType = MdcLogConstants.EVENT_TYPE_ACCESSED)
    public Response getActiveSickLeavesForCareUnit(@RequestBody SickLeaveRequestDTO sickLeaveRequestDTO) {
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
        LOG.info(sickLeaveLogMessageFactory.message(GET_SICK_LEAVE_ACTIVE, sjukfallEnhetList.size()));

        return Response.ok(new SickLeaveResponseDTO(sjukfallEnhetList)).build();
    }

    @PrometheusTimeMethod
    @POST
    @Path("/filters")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    @PerformanceLogging(eventAction = "list-sick-leaves-filter", eventType = MdcLogConstants.EVENT_TYPE_ACCESSED)
    public Response populateFilters(@RequestBody PopulateFiltersRequestDTO populateFiltersRequestDTO) {
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
        LOG.info(sickLeaveLogMessageFactory.message(GET_SICK_LEAVE_FILTER));

        return Response.ok(
            new PopulateFiltersResponseDTO(
                getSickLeaveFilterServiceResponse.getActiveDoctors(),
                getSickLeaveFilterServiceResponse.getDiagnosisChapters(),
                getSickLeaveFilterServiceResponse.getNbrOfSickLeaves(),
                getSickLeaveFilterServiceResponse.isHasOngoingSickLeaves(),
                getSickLeaveFilterServiceResponse.getRekoStatusTypes(),
                getSickLeaveFilterServiceResponse.getOccupationTypes()
            )
        ).build();
    }
}
