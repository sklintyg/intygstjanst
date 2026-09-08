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
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateResponseDTO;

@Slf4j
final class BinaryCertificateSizeValidator {

  private static final int FIVE_MiB = 1024 * 1024 * 5; // 5 MiB

  private BinaryCertificateSizeValidator() {}

  public static void validateSize(
      String certificateId, BinaryCertificateResponseDTO binaryCertificateResponse) {
    if (binaryCertificateResponse.getPdfData().length > FIVE_MiB) {
      log.info(
          "Binary certificate '{}' exceeds maximum allowed size of 5 MiB (actual size: {} bytes)",
          certificateId,
          binaryCertificateResponse.getPdfData().length);
      throw new ServerException(
          "Binary certificate '" + certificateId + "' exceeds maximum allowed size of 5 MiB");
    }
  }
}
