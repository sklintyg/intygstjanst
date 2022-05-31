/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.intygstjanst.web.service.CertificateExportService;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateExportPageDTO;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateTextDTO;

@Path("v1")
public class CertificateExportController {

    @Autowired
    CertificateExportService certificateExportService;

    private static final int ERASE_CERTIFICATES_PAGE_SIZE = 1000;

    @GET
    @Path("certificatetexts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CertificateTextDTO> getCertificateTexts() {
        return certificateExportService.getCertificateTexts();
    }

    @GET
    @Path("/certificates/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public CertificateExportPageDTO getCertificates(@PathParam("id") String careProviderId, @QueryParam("size") int size,
        @QueryParam("page") int page) {
        return certificateExportService.getCertificateExportPage(careProviderId, page, size);
    }

    @DELETE
    @Path("/certificates/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void eraseDataForCareProvider(@PathParam("id") String careProviderId) {
        certificateExportService.eraseCertificates(careProviderId, ERASE_CERTIFICATES_PAGE_SIZE);
    }
}
