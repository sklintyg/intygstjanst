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
package se.inera.intyg.intygstjanst.web.integration.intyginfo;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.intyginfo.dto.ItIntygInfo;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.web.service.IntygInfoService;

/**
 * Internal REST endpoint for intyg oriented data.
 */
@Path("/intygInfo")
public class IntygInfoController {

    @Autowired
    private IntygInfoService intygInfoService;

    @PrometheusTimeMethod
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getIntygInfo(@PathParam("id") String id) {

        Optional<ItIntygInfo> intygInfo = intygInfoService.getIntygInfo(id);

        if (intygInfo.isEmpty()) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(intygInfo.get()).build();
    }

    @PrometheusTimeMethod
    @GET
    @Path("/{hsaId}/count")
    @Produces(MediaType.APPLICATION_JSON)
    public Long getCertificateCountForCareProvider(@PathParam("hsaId") String hsaId) {
        return intygInfoService.getCertificateCount(hsaId);
    }

}
