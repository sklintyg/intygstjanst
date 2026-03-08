/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.application.citizen;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.intygstjanst.application.certificate.dto.SendCertificateRequestDTO;
import se.inera.intyg.intygstjanst.application.certificate.service.SendCertificateService;
import se.inera.intyg.intygstjanst.application.citizen.dto.CitizenCertificateSendRequestDTO;
import se.inera.intyg.intygstjanst.application.citizen.dto.CitizenCertificatesRequestDTO;
import se.inera.intyg.intygstjanst.application.citizen.dto.ListCitizenCertificatesRequest;
import se.inera.intyg.intygstjanst.application.citizen.dto.ListCitizenCertificatesResponseDTO;
import se.inera.intyg.intygstjanst.application.citizen.service.ListCitizenCertificatesService;
import se.inera.intyg.intygstjanst.infrastructure.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.infrastructure.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.infrastructure.security.interceptor.ApiBasePath;
import se.inera.intyg.schemas.contract.Personnummer;

@RestController
@ApiBasePath("/internalapi")
@RequestMapping("/citizen")
@RequiredArgsConstructor
public class CitizenCertificateController {

  private static final Logger LOG = LoggerFactory.getLogger(CitizenCertificateController.class);

  private final ListCitizenCertificatesService listCitizenCertificatesService;
  private final SendCertificateService citizenSendCertificateAggregator;

  @PostMapping()
  @PerformanceLogging(
      eventAction = "list-certificates",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESSED,
      isActive = false)
  public ListCitizenCertificatesResponseDTO getCitizenCertificates(
      @RequestBody CitizenCertificatesRequestDTO request) {

    LOG.debug("Getting list of citizen certificates");
    final var response =
        listCitizenCertificatesService.get(
            ListCitizenCertificatesRequest.builder()
                .personnummer(Personnummer.createPersonnummer(request.getPatientId()).orElseThrow())
                .certificateTypes(request.getCertificateTypes())
                .units(request.getUnits())
                .years(request.getYears())
                .statuses(request.getStatuses())
                .build());

    return ListCitizenCertificatesResponseDTO.builder().content(response).build();
  }

  @PostMapping("/send")
  @PerformanceLogging(
      eventAction = "send-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_CHANGE,
      isActive = false)
  public void sendCitizenCertificate(@RequestBody CitizenCertificateSendRequestDTO request) {

    LOG.debug("Sending citizen certificate");

    final var convertedPatientId =
        Personnummer.createPersonnummer(request.getPatientId()).orElseThrow();

    try {
      citizenSendCertificateAggregator.send(
          SendCertificateRequestDTO.builder()
              .certificateId(request.getCertificateId())
              .patientId(convertedPatientId)
              .recipientId(request.getRecipient())
              .build());
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
