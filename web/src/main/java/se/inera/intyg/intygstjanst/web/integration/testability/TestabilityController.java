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
package se.inera.intyg.intygstjanst.web.integration.testability;

import static se.inera.intyg.intygstjanst.web.service.impl.TestabilityServiceImpl.ALFA_MEDICINCENTRUM;
import static se.inera.intyg.intygstjanst.web.service.impl.TestabilityServiceImpl.ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.web.service.TestabilityService;

@Path("/testability")
public class TestabilityController {

    private final TestabilityService testabilityService;

    private static final String VERIFICATION_MESSAGE = String.format("Test data sucsessfully created for units: %s & %s",
        ALFA_MEDICINCENTRUM, ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN);

    public TestabilityController(TestabilityService testabilityService) {
        this.testabilityService = testabilityService;
    }

    @PrometheusTimeMethod
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/createDefault")
    public Response createDefaultTestData() {
        testabilityService.createDefaultTestData();
        return Response.ok(VERIFICATION_MESSAGE).build();
    }
}