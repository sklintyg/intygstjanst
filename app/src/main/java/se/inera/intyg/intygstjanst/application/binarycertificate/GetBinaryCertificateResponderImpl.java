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

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.ws.WebServiceContext;
import org.apache.cxf.annotations.SchemaValidation;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.application.exception.ServerException;
import se.inera.intyg.intygstjanst.infrastructure.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.infrastructure.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.integration.webcert.client.GetBinaryCertificateWebcertClient;
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

  @Resource private WebServiceContext wsContext;

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
      throw new ServerException(
          "Request to GetBinaryCertificate is missing required parameter 'logical-address' (should never happen)");
    }
    if (getBinaryCertificateRequest == null
        || getBinaryCertificateRequest.getIntygsId() == null
        || getBinaryCertificateRequest.getIntygsId().getExtension() == null
        || getBinaryCertificateRequest.getIntygsId().getExtension().isEmpty()) {
      throw new ServerException(
          "Request to GetBinaryCertificate is missing required parameter 'intygs-id'");
    }

    logIncomingRequest(getBinaryCertificateRequest);

    BinaryCertificateResponseDTO binaryCertificateResponse = webcertClient.get(
        getBinaryCertificateRequest.getIntygsId().getRoot());

    return binaryCertificateResponseConverter.toResponse(binaryCertificateResponse);
  }

  private void logIncomingRequest(GetBinaryCertificateType getBinaryCertificateRequest) {
    HttpServletRequest httpRequest = getCurrentHttpRequest();
    String callingSystemHsaId =
        httpRequest.getHeader(
            "x-rivta-original-serviceconsumer-hsaid"); // TODO: check correct HTTP header name
    LOGGER.info(
        "Received request to GetBinaryCertificate with intygs-id: {} from HSA-ID {}",
        getBinaryCertificateRequest.getIntygsId().getRoot()
            + getBinaryCertificateRequest.getIntygsId().getExtension(),
        callingSystemHsaId);
  }

  private HttpServletRequest getCurrentHttpRequest() {
    return (HttpServletRequest)
        wsContext.getMessageContext().get(AbstractHTTPDestination.HTTP_REQUEST);
  }
}
