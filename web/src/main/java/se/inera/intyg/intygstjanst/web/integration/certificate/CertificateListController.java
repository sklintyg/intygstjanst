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
package se.inera.intyg.intygstjanst.web.integration.certificate;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.infra.certificate.dto.CertificateListRequest;
import se.inera.intyg.infra.certificate.dto.CertificateListResponse;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.web.service.CertificateListService;

/**
 * Internal REST endpoint to retrieve list of certificates.
 */
@Controller
@Path("/certificatelist")
public class CertificateListController {

    final CertificateListService certificateListService;

    @Autowired
    public CertificateListController(CertificateListService certificateListService) {
        this.certificateListService = certificateListService;
    }

    /**
     * Internal REST endpoint to retrieve list of signed certificates for a doctor on the logged in unit.
     *
     * @param parameters Parameters of filter query including filters that user has chosen or default filters.
     * @return Response including a list of all signed certificates and the total amount of certificates.
     */
    @PrometheusTimeMethod
    @POST
    @Path("/certificates/doctor")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PerformanceLogging(eventAction = "list-certificates", eventType = MdcLogConstants.EVENT_TYPE_ACCESSED)
    public CertificateListResponse listCertificatesForDoctor(@RequestBody CertificateListRequest parameters) {
        return certificateListService.listCertificatesForDoctor(parameters);
    }
}
