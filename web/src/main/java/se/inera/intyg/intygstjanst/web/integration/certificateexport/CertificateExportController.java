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
package se.inera.intyg.intygstjanst.web.integration.certificateexport;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import se.inera.intyg.intygstjanst.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.web.service.CertificateExportService;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateExportPageDTO;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateTextDTO;

@Path("v1")
public class CertificateExportController {

    @Autowired
    CertificateExportService certificateExportService;

    @Value("${erase.certificates.page.size:1000}")
    private int eraseCertificatesPageSize;

    @GET
    @Path("certificatetexts")
    @Produces(MediaType.APPLICATION_JSON)
    @PerformanceLogging(eventType = "retrieve-certificate-texts", eventAction = MdcLogConstants.EVENT_TYPE_ACCESSED)
    public List<CertificateTextDTO> getCertificateTexts() {
        return certificateExportService.getCertificateTexts();
    }

    @GET
    @Path("/certificates/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @PerformanceLogging(eventType = "list-certificates", eventAction = MdcLogConstants.EVENT_TYPE_ACCESSED)
    public CertificateExportPageDTO getCertificates(@PathParam("id") String careProviderId, @QueryParam("size") int size,
        @QueryParam("page") int page) {
        return certificateExportService.getCertificateExportPage(careProviderId, page, size);
    }

    @DELETE
    @Path("/certificates/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @PerformanceLogging(eventType = "erase-certificates", eventAction = MdcLogConstants.EVENT_TYPE_DELETION)
    public void eraseDataForCareProvider(@PathParam("id") String careProviderId) {
        certificateExportService.eraseCertificates(careProviderId, eraseCertificatesPageSize);
    }
}
