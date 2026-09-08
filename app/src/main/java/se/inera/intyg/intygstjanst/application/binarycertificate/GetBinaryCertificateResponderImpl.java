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

import static se.inera.intyg.intygstjanst.application.binarycertificate.BinaryCertificateSizeValidator.validateSize;
import static se.inera.intyg.intygstjanst.application.binarycertificate.GetBinaryCertificateRequestValidator.validateLogicalAddress;
import static se.inera.intyg.intygstjanst.application.binarycertificate.GetBinaryCertificateRequestValidator.validateRequest;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.annotations.SchemaValidation;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.infrastructure.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.infrastructure.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateResponseDTO;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.GetBinaryCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.GetBinaryCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.GetBinaryCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IIType;

@Slf4j
@Service
@SchemaValidation
@RequiredArgsConstructor
public class GetBinaryCertificateResponderImpl implements GetBinaryCertificateResponderInterface {

  private final BinaryCertificateResponseConverter binaryCertificateResponseConverter;
  private final GetBinaryCertificateWebcertGateway webcertClient;

  @Override
  @PerformanceLogging(
      eventAction = "retrieve-binary-certificate",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESSED,
      isActive = false)
  public GetBinaryCertificateResponseType getBinaryCertificate(
      String logicalAddress, GetBinaryCertificateType getBinaryCertificateRequest) {

    logIncomingRequest(logicalAddress, getBinaryCertificateRequest);

    validateLogicalAddress(logicalAddress);
    validateRequest(getBinaryCertificateRequest);

    final String certificateId = getBinaryCertificateRequest.getIntygsId().getExtension();

    final BinaryCertificateResponseDTO binaryCertificateResponse =
        webcertClient.getBinaryCertificate(certificateId);

    validateSize(certificateId, binaryCertificateResponse);

    return binaryCertificateResponseConverter.toResponse(binaryCertificateResponse);
  }

  private void logIncomingRequest(String hsaId, GetBinaryCertificateType request) {
    final var certificateId =
        Optional.ofNullable(request)
            .map(GetBinaryCertificateType::getIntygsId)
            .map(IIType::getExtension)
            .orElse("<missing>");
    log.info(
        "Received GetBinaryCertificate request from HSA-ID '{}' for intygs-id: '{}'",
        hsaId,
        certificateId);
  }
}
