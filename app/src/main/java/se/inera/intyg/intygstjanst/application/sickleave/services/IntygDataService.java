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

package se.inera.intyg.intygstjanst.application.sickleave.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.application.sickleave.converter.IntygsDataConverter;
import se.inera.intyg.intygstjanst.application.sickleave.converter.SjukfallCertificateIntygsDataConverter;
import se.inera.intyg.intygstjanst.application.sickleave.dto.IntygData;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.SjukfallCertificateDao;

@Service
public class IntygDataService {

  private final HsaService hsaService;
  private final SjukfallCertificateDao sjukfallCertificateDao;
  private final IntygsDataConverter intygDataConverter;

  public IntygDataService(
      HsaService hsaService,
      SjukfallCertificateDao sjukfallCertificateDao,
      IntygsDataConverter intygDataConverter) {
    this.hsaService = hsaService;
    this.sjukfallCertificateDao = sjukfallCertificateDao;
    this.intygDataConverter = intygDataConverter;
  }

  public List<IntygData> getIntygData(String careUnitId, int maxDaysSinceSickLeaveCompleted) {
    final var careProviderId = hsaService.getHsaIdForVardgivare(careUnitId);
    final var careUnitAndSubUnits = hsaService.getHsaIdsForCareUnitAndSubUnits(careUnitId);
    final var activeSickLeaveCertificateForCareUnits =
        sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(
            careProviderId, careUnitAndSubUnits, maxDaysSinceSickLeaveCompleted);
    final var filteredSickLeaveCertificates =
        filterTestCertificates(activeSickLeaveCertificateForCareUnits);
    return convertToIntygData(filteredSickLeaveCertificates);
  }

  private static List<SjukfallCertificate> filterTestCertificates(
      List<SjukfallCertificate> activeSickLeaveCertificateForCareUnits) {
    return activeSickLeaveCertificateForCareUnits.stream()
        .filter(sjukfallCertificate -> !sjukfallCertificate.isTestCertificate())
        .collect(Collectors.toList());
  }

  private List<IntygData> convertToIntygData(
      List<SjukfallCertificate> activeSjukfallCertificateForCareUnits) {
    return new ArrayList<>(
            new SjukfallCertificateIntygsDataConverter()
                .buildIntygsData(activeSjukfallCertificateForCareUnits))
        .stream().map((intygDataConverter::map)).collect(Collectors.toList());
  }
}
