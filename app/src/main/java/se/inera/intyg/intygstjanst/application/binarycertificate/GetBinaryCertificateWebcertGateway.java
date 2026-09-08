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
package se.inera.intyg.intygstjanst.application.binarycertificate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import se.inera.intyg.intygstjanst.application.exception.ServerException;
import se.inera.intyg.intygstjanst.application.exception.SoapFaultFactory;
import se.inera.intyg.intygstjanst.integration.webcert.client.GetBinaryCertificateWebcertClient;
import se.inera.intyg.intygstjanst.integration.webcert.client.WebcertClientException;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateResponseDTO;

@Slf4j
@Component
@RequiredArgsConstructor
class GetBinaryCertificateWebcertGateway {

  private final GetBinaryCertificateWebcertClient webcertClient;

  public BinaryCertificateResponseDTO getBinaryCertificate(String certificateId) {
    try {
      return webcertClient.get(certificateId);
    } catch (WebcertClientException e) {
      if (e.isClientError()) {
        log.warn(
            "Call to webcert's binary certificate endpoint for certificate '{}' failed with a "
                + "client error ({}): {}",
            certificateId,
            e.getStatusCode(),
            e.getMessage());
        throw SoapFaultFactory.clientFault(
            "Failed to retrieve binary certificate with id '" + certificateId + "'");
      } else if (e.isServerError()) {
        log.error(
            "Call to webcert's binary certificate endpoint for certificate '{}' failed with a "
                + "server error ({})",
            certificateId,
            e.getStatusCode(),
            e);
      }
      throw new ServerException(
          "Failed to retrieve binary certificate with id '" + certificateId + "'");
    }
  }
}
