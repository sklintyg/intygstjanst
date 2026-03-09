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

import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.CSIntegrationService;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.dto.GetValidSickLeaveCertificateIdsInternalRequest;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.SjukfallCertificateDao;

@Service
@RequiredArgsConstructor
public class ValidSickLeaveAggregator {

  private final SjukfallCertificateDao sjukfallCertificateDao;
  private final CSIntegrationService csIntegrationService;

  public List<SjukfallCertificate> get(List<SjukfallCertificate> sjukfallCertificate) {
    final var sickLeaveIds = sjukfallCertificate.stream().map(SjukfallCertificate::getId).toList();

    final var sickLeavesStoredInCS = sjukfallCertificateDao.findSickLeavesStoredInCS(sickLeaveIds);

    if (sickLeavesStoredInCS.isEmpty()) {
      return sjukfallCertificate.stream()
          .filter(sickLeave -> !sickLeave.isTestCertificate())
          .toList();
    }

    final var validSickLeaveIdsFromCS =
        csIntegrationService.getValidSickLeaveIds(
            GetValidSickLeaveCertificateIdsInternalRequest.builder()
                .certificateIds(sickLeavesStoredInCS)
                .build());

    final var validSickLeaveIdsFromIT =
        sickLeaveIds.stream().filter(id -> !sickLeavesStoredInCS.contains(id)).toList();

    final var validSickLeaveIds =
        Stream.concat(validSickLeaveIdsFromCS.stream(), validSickLeaveIdsFromIT.stream()).toList();

    return sjukfallCertificate.stream()
        .filter(sickLeave -> validSickLeaveIds.contains(sickLeave.getId()))
        .filter(sickLeave -> !sickLeave.isTestCertificate())
        .toList();
  }
}
