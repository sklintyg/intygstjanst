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
package se.inera.intyg.intygstjanst.application.testcertificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.application.testcertificate.dto.TestCertificateEraseRequest;
import se.inera.intyg.intygstjanst.application.testcertificate.dto.TestCertificateEraseResult;
import se.inera.intyg.intygstjanst.application.testcertificate.service.TestCertificateService;

@ExtendWith(MockitoExtension.class)
class TestCertificateControllerTest {

  @Mock private TestCertificateService testCertificateService;

  @InjectMocks private TestCertificateController testCertificateController;

  @Test
  void testEraseTestCertificateSuccessful() {
    final var testCertificateEraseRequest = new TestCertificateEraseRequest();
    testCertificateEraseRequest.setFrom(null);
    testCertificateEraseRequest.setTo(LocalDateTime.now());

    final var expected = TestCertificateEraseResult.create(0, 0);

    doReturn(expected).when(testCertificateService).eraseTestCertificates(any(), any());

    final var actual = testCertificateController.eraseTestCertificates(testCertificateEraseRequest);

    assertEquals(expected, actual);
  }

  @Test
  void testEraseTestCertificateMissingToDate() {
    final var testCertificateEraseRequest = new TestCertificateEraseRequest();
    testCertificateEraseRequest.setFrom(null);
    testCertificateEraseRequest.setTo(null);

    final var ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> testCertificateController.eraseTestCertificates(testCertificateEraseRequest));

    assertEquals("Missing date to", ex.getMessage());
  }

  @Test
  void testEraseTestCertificateIncorrectDateRange() {
    final var testCertificateEraseRequest = new TestCertificateEraseRequest();
    testCertificateEraseRequest.setFrom(LocalDateTime.now());
    testCertificateEraseRequest.setTo(testCertificateEraseRequest.getFrom().minusDays(1));

    final var ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> testCertificateController.eraseTestCertificates(testCertificateEraseRequest));

    assertEquals("From date is after to date", ex.getMessage());
  }
}
