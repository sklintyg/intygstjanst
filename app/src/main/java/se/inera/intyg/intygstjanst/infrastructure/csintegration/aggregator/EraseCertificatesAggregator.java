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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.application.erase.EraseCertificatesFromIT;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.EraseCertificatesFromCS;

@Service
@RequiredArgsConstructor
public class EraseCertificatesAggregator {

  private final EraseCertificatesFromCS eraseCertificatesFromCS;
  private final EraseCertificatesFromIT eraseCertificatesFromIT;

  public void eraseCertificates(String careProviderId) {
    if (careProviderId == null || careProviderId.isBlank()) {
      throw new IllegalArgumentException("careProviderId cannot be null or empty");
    }

    eraseCertificatesFromIT.eraseCertificates(careProviderId);

    eraseCertificatesFromCS.eraseCertificates(careProviderId);
  }
}
