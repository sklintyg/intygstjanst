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

import lombok.extern.slf4j.Slf4j;
import se.inera.intyg.intygstjanst.application.exception.ServerException;
import se.inera.intyg.intygstjanst.application.exception.SoapFaultFactory;
import se.riv.clinicalprocess.healthcond.certificate.getBinaryCertificate.v1.GetBinaryCertificateType;

@Slf4j
final class GetBinaryCertificateRequestValidator {

  private GetBinaryCertificateRequestValidator() {}

  public static void validateLogicalAddress(String logicalAddress) {
    if (logicalAddress == null || logicalAddress.isEmpty()) {
      log.error("logicalAddress is null or empty (should not happen)");
      throw new ServerException(
          "Request to GetBinaryCertificate is missing required parameter 'logical-address'");
    }
  }

  public static void validateRequest(GetBinaryCertificateType request) {
    if (request == null
        || request.getIntygsId() == null
        || request.getIntygsId().getExtension() == null
        || request.getIntygsId().getExtension().isEmpty()
        || request.getIntygsId().getRoot() == null
        || request.getIntygsId().getRoot().isEmpty()) {
      log.info("intygs-id is null or empty");
      throw SoapFaultFactory.clientFault(
          "Request to GetBinaryCertificate is missing required parameter 'intygs-id'");
    }
  }
}
