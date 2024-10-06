/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.integration.reko;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.infra.sjukfall.dto.RekoStatusDTO;
import se.inera.intyg.intygstjanst.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.web.service.CreateRekoStatusService;
import se.inera.intyg.intygstjanst.web.service.GetRekoStatusService;

@Path("/reko")
public class RekoController {

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private final CreateRekoStatusService createRekoStatusService;
    private final GetRekoStatusService getRekoStatusService;

    public RekoController(CreateRekoStatusService createRekoStatusService, GetRekoStatusService getRekoStatusService) {
        this.createRekoStatusService = createRekoStatusService;
        this.getRekoStatusService = getRekoStatusService;
    }

    @PrometheusTimeMethod
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    @PerformanceLogging(eventType = "create-reko-status", eventAction = MdcLogConstants.EVENT_TYPE_CREATION)
    public RekoStatusDTO createRekoStatus(@RequestBody CreateRekoStatusRequestDTO request) {
        return createRekoStatusService.create(
            request.patientId,
            request.getStatusId(),
            request.getCareProviderId(),
            request.getCareUnitId(),
            request.getUnitId(),
            request.getStaffId(),
            request.getStaffName(),
            request.getSickLeaveTimestamp()
        );
    }

    @PrometheusTimeMethod
    @POST
    @Path("/patient")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    @PerformanceLogging(eventType = "retrieve-reko-status", eventAction = MdcLogConstants.EVENT_TYPE_ACCESSED)
    public RekoStatusDTO getRekoStatus(@RequestBody GetRekoStatusRequestDTO request) {
        return getRekoStatusService.get(
            request.getPatientId(),
            request.getEndDate(),
            request.getStartDate(),
            request.getCareUnitId()
        );
    }
}
