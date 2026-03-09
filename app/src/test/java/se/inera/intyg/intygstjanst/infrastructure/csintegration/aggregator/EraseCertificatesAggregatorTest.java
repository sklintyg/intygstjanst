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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.application.erase.EraseCertificatesFromIT;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.EraseCertificatesFromCS;

@ExtendWith(MockitoExtension.class)
class EraseCertificatesAggregatorTest {

  private static final String CARE_PROVIDER_ID = "careProviderId";
  @Mock EraseCertificatesFromCS eraseCertificatesFromCS;
  @Mock EraseCertificatesFromIT eraseCertificatesFromIT;
  @InjectMocks EraseCertificatesAggregator eraseCertificatesAggregator;

  @Test
  void shallThrowExceptionIfCareProviderIdIsNull() {
    assertThrows(
        IllegalArgumentException.class, () -> eraseCertificatesAggregator.eraseCertificates(null));
  }

  @Test
  void shallThrowExceptionIfCareProviderIdIsBlank() {
    assertThrows(
        IllegalArgumentException.class, () -> eraseCertificatesAggregator.eraseCertificates(""));
  }

  @Test
  void shallEraseCertificatesFromCS() {
    eraseCertificatesAggregator.eraseCertificates(CARE_PROVIDER_ID);
    verify(eraseCertificatesFromCS).eraseCertificates(CARE_PROVIDER_ID);
    verify(eraseCertificatesFromIT).eraseCertificates(CARE_PROVIDER_ID);
  }
}
