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

package se.inera.intyg.intygstjanst.web.integration.sickleave;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.web.service.PopulateFilterService;
import se.inera.intyg.intygstjanst.web.service.SickLeavesForCareUnitService;
import se.inera.intyg.intygstjanst.web.service.dto.PopulateFiltersRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveResponseDTO;

@Path("/sickleave")
public class SickLeaveController {

    private static final String UTF_8_CHARSET = ";charset=utf-8";
    private final SickLeavesForCareUnitService sickLeavesForCareUnitService;
    private final PopulateFilterService populateFilterService;


    public SickLeaveController(SickLeavesForCareUnitService sickLeavesForCareUnitService, PopulateFilterService populateFilterService) {
        this.sickLeavesForCareUnitService = sickLeavesForCareUnitService;
        this.populateFilterService = populateFilterService;
    }

    @PrometheusTimeMethod
    @POST
    @Path("/active")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getActiveSickLeavesForCareUnit(@RequestBody SickLeaveRequestDTO sickLeaveRequestDTO) {
        final var activeSickLeavesForCareUnit = sickLeavesForCareUnitService.getActiveSickLeavesForCareUnit(sickLeaveRequestDTO);
        return Response.ok(SickLeaveResponseDTO.create(activeSickLeavesForCareUnit)).build();
    }

    @PrometheusTimeMethod
    @POST
    @Path("/filters")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response populateFilters(@RequestBody PopulateFiltersRequestDTO populateFiltersRequestDTO) {
        final var populateFiltersResponseDTO = populateFilterService.populateFilters(populateFiltersRequestDTO);
        return Response.ok(populateFiltersResponseDTO).build();
    }
}
