/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.intygstjanst.web.service.HealthCheckService;
import se.inera.intyg.intygstjanst.web.service.impl.HealthCheckServiceImpl.Status;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * RESTinterface for checking the general health status of the application.
 *
 * @author erik
 */
public class HealthCheckController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckController.class);

    @Autowired
    private HealthCheckService healthCheck;

    @GET
    @Path("/ping")
    @Produces(MediaType.APPLICATION_XML)
    public Response getPing() {
        String xmlResponse = buildXMLResponse(true, 0);
        LOGGER.debug("Pinged Intygstjänsten, got: " + xmlResponse);
        return Response.ok(xmlResponse).build();
    }

    @GET
    @Path("/checkDB")
    @Produces(MediaType.APPLICATION_XML)
    public Response checkDB() {
        Status status = healthCheck.getDbStatus();
        String xmlResponse = buildXMLResponse(status);
        return Response.ok(xmlResponse).build();
    }

    @GET
    @Path("/checkJMS")
    @Produces(MediaType.APPLICATION_XML)
    public Response checkJMS() {
        Status status = healthCheck.getJMSStatus();
        String xmlResponse = buildXMLResponse(status);
        return Response.ok(xmlResponse).build();
    }

    @GET
    @Path("/uptime")
    @Produces(MediaType.APPLICATION_XML)
    public Response checkUptime() {
        Status status = healthCheck.getUptime();
        String xmlResponse = buildXMLResponse(status);
        return Response.ok(xmlResponse).build();
    }

    private String buildXMLResponse(Status status) {
        return buildXMLResponse(status.isOk(), status.getMeasurement());
    }

    private String buildXMLResponse(boolean ok, long time) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pingdom_http_custom_check>");
        sb.append("<status>" + (ok ? "OK" : "FAIL") + "</status>");
        sb.append("<response_time>" + time + "</response_time>");
        sb.append("</pingdom_http_custom_check>");
        return sb.toString();
    }
}
