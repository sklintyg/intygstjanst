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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.intygstjanst.application.exception.ServerException;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateResponseDTO;

class BinaryCertificateSizeValidatorTest {

  private static final String CERTIFICATE_ID = "cert-123";
  private static final int FIVE_MiB = 1024 * 1024 * 5;

  private static BinaryCertificateResponseDTO dtoWithPdfSize(int size) {
    return BinaryCertificateResponseDTO.builder().pdfData(new byte[size]).build();
  }

  @Nested
  class WhenSizeIsWithinLimit {

    @Test
    void shallNotThrowWhenPdfDataIsEmpty() {
      final var dto = dtoWithPdfSize(0);
      assertDoesNotThrow(() -> BinaryCertificateSizeValidator.validateSize(CERTIFICATE_ID, dto));
    }

    @Test
    void shallNotThrowWhenPdfDataIsBelowLimit() {
      final var dto = dtoWithPdfSize(FIVE_MiB - 1);
      assertDoesNotThrow(() -> BinaryCertificateSizeValidator.validateSize(CERTIFICATE_ID, dto));
    }

    @Test
    void shallNotThrowWhenPdfDataIsExactlyAtLimit() {
      final var dto = dtoWithPdfSize(FIVE_MiB);
      assertDoesNotThrow(() -> BinaryCertificateSizeValidator.validateSize(CERTIFICATE_ID, dto));
    }
  }

  @Nested
  class WhenSizeExceedsLimit {

    @Test
    void shallThrowServerExceptionWhenPdfDataExceedsLimit() {
      final var dto = dtoWithPdfSize(FIVE_MiB + 1);
      assertThrows(
          ServerException.class,
          () -> BinaryCertificateSizeValidator.validateSize(CERTIFICATE_ID, dto));
    }

    @Test
    void shallIncludeCertificateIdInExceptionMessage() {
      final var dto = dtoWithPdfSize(FIVE_MiB + 1);
      final var exception =
          assertThrows(
              ServerException.class,
              () -> BinaryCertificateSizeValidator.validateSize(CERTIFICATE_ID, dto));
      assertTrue(exception.getMessage().contains(CERTIFICATE_ID));
    }

    @Test
    void shallIncludeSizeLimitInExceptionMessage() {
      final var dto = dtoWithPdfSize(FIVE_MiB + 1);
      final var exception =
          assertThrows(
              ServerException.class,
              () -> BinaryCertificateSizeValidator.validateSize(CERTIFICATE_ID, dto));
      assertTrue(exception.getMessage().contains("5 MiB"));
    }
  }
}
