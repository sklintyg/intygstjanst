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

package se.inera.intyg.intygstjanst.web.integration.customertermination;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.xml.sax.SAXException;
import se.inera.intyg.intygstjanst.web.service.CustomerTerminationService;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateExportPageDTO;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateTextDTO;

@Path("v1")
public class CustomerTerminationController {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerTerminationController.class);

    @Autowired
    CustomerTerminationService customerTerminationService;

    @GET
    @Path("certificatetexts")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<List<CertificateTextDTO>> getCertificateTexts() {
        try {
            return ResponseEntity.ok(customerTerminationService.getCertificateTexts());
        } catch (IOException | ParserConfigurationException | TransformerException | SAXException e) {
            LOG.error("Failure fetching certificate texts.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/certificates")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<CertificateExportPageDTO> getCertificates(@QueryParam("id") String careProviderId, @QueryParam("size") int size,
        @QueryParam("page") int page) {
        final var pageable = PageRequest.of(page, size, Sort.by(Direction.ASC, "signedDate", "id"));
        final var certificateExportPage = customerTerminationService.getCertificateExportPage(careProviderId, pageable);
        return ResponseEntity.ok(certificateExportPage);
    }
}
