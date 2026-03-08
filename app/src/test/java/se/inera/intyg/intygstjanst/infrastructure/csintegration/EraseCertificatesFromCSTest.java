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

package se.inera.intyg.intygstjanst.infrastructure.csintegration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EraseCertificatesFromCSTest {

  @Mock CSIntegrationService csIntegrationService;
  @InjectMocks EraseCertificatesFromCS eraseCertificatesFromCS;

  @Test
  void shallIncludeCareProviderId() {
    final var expectedCareProviderId = "expectedCareProviderId";
    final var argumentCaptor = ArgumentCaptor.forClass(String.class);

    eraseCertificatesFromCS.eraseCertificates(expectedCareProviderId);
    verify(csIntegrationService).eraseCertificatesForCareProvider(argumentCaptor.capture());
    assertEquals(expectedCareProviderId, argumentCaptor.getValue());
  }
}
