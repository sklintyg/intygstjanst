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
package se.inera.intyg.intygstjanst.web.integration.message;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.web.service.MessageService;

/**
 * Internal REST endpoint to retrieve messages on certificates
 */
@Path("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PrometheusTimeMethod
    @GET
    @Path("/{certificateId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PerformanceLogging(eventType = "retrieve-messages", eventAction = MdcLogConstants.EVENT_TYPE_ACCESSED)
    public Response findMessagesByCertificateId(@PathParam("certificateId") String certificateId) {
        if (certificateId == null || certificateId.trim().isEmpty()) {
            return Response.status(400, "Missing certificateId").build();
        }

        final var messageList = messageService.findMessagesByCertificateId(certificateId);
        return Response.ok(messageList).build();
    }
}
