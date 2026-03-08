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

package se.inera.intyg.intygstjanst.infrastructure.csintegration.aggregator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.CSIntegrationService;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.dto.GetValidSickLeaveCertificateIdsInternalRequest;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.SjukfallCertificateDao;

@ExtendWith(MockitoExtension.class)
class ValidSickLeaveAggregatorTest {

  @Mock private SjukfallCertificateDao sjukfallCertificateDao;

  @Mock private CSIntegrationService csIntegrationService;

  @InjectMocks private ValidSickLeaveAggregator validSickLeaveAggregator;

  @Test
  void shouldReturnCertificatesWhenAllStoredInIT() {
    final var certA = new SjukfallCertificate("a");
    certA.setTestCertificate(false);
    final var certB = new SjukfallCertificate("b");
    certB.setTestCertificate(false);

    final var input = List.of(certA, certB);
    final var ids = List.of("a", "b");

    when(sjukfallCertificateDao.findSickLeavesStoredInCS(ids)).thenReturn(List.of());

    final var result = validSickLeaveAggregator.get(input);

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(c -> "a".equals(c.getId())));
    assertTrue(result.stream().anyMatch(c -> "b".equals(c.getId())));
  }

  @Test
  void shouldReturnOnlyValidCertificatesFromCSAndIT() {
    final var certA = new SjukfallCertificate("a");
    certA.setTestCertificate(false);
    final var certB = new SjukfallCertificate("b");
    certB.setTestCertificate(false);
    final var certC = new SjukfallCertificate("c");
    certC.setTestCertificate(false);

    final var input = List.of(certA, certB, certC);
    final var ids = List.of("a", "b", "c");

    when(sjukfallCertificateDao.findSickLeavesStoredInCS(ids)).thenReturn(List.of("b", "c"));
    when(csIntegrationService.getValidSickLeaveIds(
            any(GetValidSickLeaveCertificateIdsInternalRequest.class)))
        .thenReturn(List.of("c"));

    final var result = validSickLeaveAggregator.get(input);

    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(c -> "a".equals(c.getId())));
    assertTrue(result.stream().anyMatch(c -> "c".equals(c.getId())));
  }

  @Test
  void shouldExcludeTestCertificates() {
    final var certA = new SjukfallCertificate("a");
    certA.setTestCertificate(true);
    final var certB = new SjukfallCertificate("b");
    certB.setTestCertificate(false);
    final var certC = new SjukfallCertificate("c");
    certC.setTestCertificate(false);

    final var input = List.of(certA, certB, certC);
    final var ids = List.of("a", "b", "c");

    when(sjukfallCertificateDao.findSickLeavesStoredInCS(ids)).thenReturn(List.of("b", "c"));
    when(csIntegrationService.getValidSickLeaveIds(
            any(GetValidSickLeaveCertificateIdsInternalRequest.class)))
        .thenReturn(List.of("c"));

    final var result = validSickLeaveAggregator.get(input);

    assertEquals(1, result.size());
    assertTrue(result.stream().anyMatch(c -> "c".equals(c.getId())));
  }
}
