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

package se.inera.intyg.intygstjanst.web.integration.citizen;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.web.service.ListCitizenCertificatesService;
import se.inera.intyg.intygstjanst.web.service.SendCertificateService;
import se.inera.intyg.intygstjanst.web.service.dto.SendCertificateRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.ListCitizenCertificatesRequest;
import se.inera.intyg.schemas.contract.Personnummer;

@Path("/citizen")
public class CitizenCertificateController {

    private static final Logger LOG = LoggerFactory.getLogger(CitizenCertificateController.class);
    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private final ListCitizenCertificatesService listCitizenCertificatesService;
    private final SendCertificateService sendCertificateService;

    public CitizenCertificateController(ListCitizenCertificatesService listCitizenCertificatesService,
        SendCertificateService sendCertificateService) {
        this.listCitizenCertificatesService = listCitizenCertificatesService;
        this.sendCertificateService = sendCertificateService;
    }

    @PrometheusTimeMethod
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    public ListCitizenCertificatesResponseDTO getCitizenCertificates(
        @RequestBody CitizenCertificatesRequestDTO request) {

        LOG.debug("Getting list of citizen certificates");
        final var response = listCitizenCertificatesService.get(
            ListCitizenCertificatesRequest
                .builder()
                .personnummer(Personnummer.createPersonnummer(request.getPatientId()).orElseThrow())
                .certificateTypes(request.getCertificateTypes())
                .units(request.getUnits())
                .years(request.getYears())
                .statuses(request.getStatuses())
                .build()
        );

        return ListCitizenCertificatesResponseDTO
            .builder()
            .content(response)
            .build();
    }

    @PrometheusTimeMethod
    @POST
    @Path("/send")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    public void sendCitizenCertificate(
        @RequestBody CitizenCertificateSendRequestDTO request) {

        LOG.debug("Sending citizen certificate");

        final var convertedPatientId = Personnummer.createPersonnummer(request.getPatientId()).orElseThrow();

        try {
            sendCertificateService.send(
                SendCertificateRequestDTO
                    .builder()
                    .certificateId(request.getCertificateId())
                    .patientId(convertedPatientId)
                    .recipientId(request.getRecipient())
                    .build()
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
