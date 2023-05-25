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

package se.inera.intyg.intygstjanst.web.integration.reko;

import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.web.service.CreateRekoStatusService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/reko")
public class RekoController {
    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private final CreateRekoStatusService createRekoStatusService;

    public RekoController(CreateRekoStatusService createRekoStatusService) {
        this.createRekoStatusService = createRekoStatusService;
    }

    @PrometheusTimeMethod
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRekoStatus(@RequestBody CreateRekoStatusRequestDTO request) {
        createRekoStatusService.create(
                request.patientId,
                request.getStatus(),
                request.getCareProviderId(),
                request.getCareUnitId(),
                request.getUnitId(),
                request.getStaffId(),
                request.getStaffName(),
                request.getSickLeaveTimestamp()
        );

        return Response.ok().build();
    }
}
