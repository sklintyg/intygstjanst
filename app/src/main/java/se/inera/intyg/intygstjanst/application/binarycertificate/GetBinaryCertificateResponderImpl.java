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

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.application.exception.ServerException;
import se.inera.intyg.intygstjanst.infrastructure.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.infrastructure.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.integration.webcert.client.GetBinaryCertificateWebcertClient;
import se.inera.intyg.intygstjanst.integration.webcert.client.WebcertClientException;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateResponseDTO;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.GetBinaryCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.GetBinaryCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.GetBinaryCertificateType;

@Service
@SchemaValidation
public class GetBinaryCertificateResponderImpl implements GetBinaryCertificateResponderInterface {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(GetBinaryCertificateResponderImpl.class);

  private final BinaryCertificateResponseConverter binaryCertificateResponseConverter;
  private final GetBinaryCertificateWebcertClient webcertClient;

  public GetBinaryCertificateResponderImpl(
      BinaryCertificateResponseConverter binaryCertificateResponseConverter,
      GetBinaryCertificateWebcertClient webcertClient) {
    this.binaryCertificateResponseConverter = binaryCertificateResponseConverter;
    this.webcertClient = webcertClient;
  }

  @Override
  @PerformanceLogging(
      eventAction = "retrieve-binary-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESSED,
      isActive = false)
  public GetBinaryCertificateResponseType getBinaryCertificate(
      String logicalAddress, GetBinaryCertificateType getBinaryCertificateRequest) {
    if (logicalAddress == null || logicalAddress.isEmpty()) {
      LOGGER.error("logicalAddress is null or empty (should not happen)");
      throw new ServerException(
          "Request to GetBinaryCertificate is missing required parameter 'logical-address'");
    }
    if (getBinaryCertificateRequest == null
        || getBinaryCertificateRequest.getIntygsId() == null
        || getBinaryCertificateRequest.getIntygsId().getExtension() == null
        || getBinaryCertificateRequest.getIntygsId().getExtension().isEmpty()
        || getBinaryCertificateRequest.getIntygsId().getRoot() == null
        || getBinaryCertificateRequest.getIntygsId().getRoot().isEmpty()) {
      LOGGER.info("intygs-id is null or empty");
      throw new ServerException(
          "Request to GetBinaryCertificate is missing required parameter 'intygs-id'");
    }

    logIncomingRequest(logicalAddress, getBinaryCertificateRequest);

    final BinaryCertificateResponseDTO binaryCertificateResponse =
        callWebcert(getBinaryCertificateRequest.getIntygsId().getRoot());

    return binaryCertificateResponseConverter.toResponse(binaryCertificateResponse);
  }

  private BinaryCertificateResponseDTO callWebcert(String certificateId) {
    try {
      return webcertClient.get(certificateId);
    } catch (WebcertClientException e) {
      if (e.isClientError()) {
        LOGGER.warn(
            "Call to webcert's binary certificate endpoint for certificate '{}' failed with a "
                + "client error ({}): {}",
            certificateId,
            e.getStatusCode(),
            e.getMessage());
      } else if (e.isServerError()) {
        LOGGER.error(
            "Call to webcert's binary certificate endpoint for certificate '{}' failed with a "
                + "server error ({})",
            certificateId,
            e.getStatusCode(),
            e);
      }
      throw new ServerException(
          "Failed to retrieve binary certificate '" + certificateId + "' from webcert");
    }
  }

  private void logIncomingRequest(String hsaId,
      GetBinaryCertificateType getBinaryCertificateRequest) {
    LOGGER.info(
        "Received request to GetBinaryCertificate with intygs-id: {} from HSA-ID {}",
        getBinaryCertificateRequest.getIntygsId().getRoot()
            + getBinaryCertificateRequest.getIntygsId().getExtension(),
        hsaId);
  }
}
