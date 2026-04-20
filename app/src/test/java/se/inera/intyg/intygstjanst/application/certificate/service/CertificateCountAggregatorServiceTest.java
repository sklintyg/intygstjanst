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
package se.inera.intyg.intygstjanst.application.certificate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.CSIntegrationService;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.dto.GetCertificateCountRequest;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.CertificateRepository;

@ExtendWith(MockitoExtension.class)
class CertificateCountAggregatorServiceTest {

  private static final String HSA_ID = "careProvider";
  private static final GetCertificateCountRequest CS_REQUEST =
      GetCertificateCountRequest.builder().hsaId(HSA_ID).build();
  @Mock CertificateRepository certificateRepository;
  @Mock CSIntegrationService csIntegrationService;
  @InjectMocks CertificateCountAggregatorService service;

  @Test
  void shouldReturnCountFromIt() {
    when(certificateRepository.getCertificateCountForCareProvider(HSA_ID)).thenReturn(1L);
    when(csIntegrationService.getCertificateCount(CS_REQUEST)).thenReturn(0L);

    assertEquals(1L, service.count(HSA_ID));
  }

  @Test
  void shouldReturnCountFromCs() {
    when(certificateRepository.getCertificateCountForCareProvider(HSA_ID)).thenReturn(0L);
    when(csIntegrationService.getCertificateCount(CS_REQUEST)).thenReturn(1L);

    assertEquals(1L, service.count(HSA_ID));
  }

  @Test
  void shouldReturnCountFromCSAndIt() {
    when(certificateRepository.getCertificateCountForCareProvider(HSA_ID)).thenReturn(1L);
    when(csIntegrationService.getCertificateCount(CS_REQUEST)).thenReturn(1L);

    assertEquals(2L, service.count(HSA_ID));
  }
}
