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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.application.sickleave.converter.IntygsDataConverter;
import se.inera.intyg.intygstjanst.application.sickleave.dto.IntygData;
import se.inera.intyg.intygstjanst.application.sickleave.dto.IntygParametrar;
import se.inera.intyg.intygstjanst.application.sickleave.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.aggregator.ValidSickLeaveAggregator;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.SjukfallCertificateDao;

@Service
public class GetActiveSickLeaveCertificates {

  private final SjukfallCertificateDao sjukfallCertificateDao;

  private final IntygsDataConverter intygDataConverter;

  private final SjukfallEngineService sjukfallEngineService;

  private final ValidSickLeaveAggregator validSickLeaveAggregator;

  public GetActiveSickLeaveCertificates(
      SjukfallCertificateDao sjukfallCertificateDao,
      IntygsDataConverter intygDataConverter,
      SjukfallEngineService sjukfallEngineService,
      ValidSickLeaveAggregator validSickLeaveAggregator) {
    this.sjukfallCertificateDao = sjukfallCertificateDao;
    this.intygDataConverter = intygDataConverter;
    this.sjukfallEngineService = sjukfallEngineService;
    this.validSickLeaveAggregator = validSickLeaveAggregator;
  }

  public List<IntygData> get(
      String careProviderId,
      List<String> unitIds,
      List<String> doctorIds,
      int maxDaysSinceSickLeaveCompleted) {
    assertCareProviderId(careProviderId);
    assertUnitIds(unitIds);
    assertDoctorIds(doctorIds);

    final var todayDate = LocalDate.now();
    final var recentlyClosed =
        maxDaysSinceSickLeaveCompleted > 0
            ? todayDate.minusDays(maxDaysSinceSickLeaveCompleted)
            : null;

    final var sjukfallCertificate =
        sjukfallCertificateDao.findActiveSjukfallCertificate(
            careProviderId, unitIds, doctorIds, todayDate, recentlyClosed);

    final var sjukfallCertificates = validSickLeaveAggregator.get(sjukfallCertificate);

    final var intygDataList = intygDataConverter.convert(sjukfallCertificates);

    final var activeIntygIds =
        sjukfallEngineService
            .beraknaSjukfallForEnhet(
                intygDataList, new IntygParametrar(0, maxDaysSinceSickLeaveCompleted, todayDate))
            .stream()
            .map(SjukfallEnhet::getAktivIntygsId)
            .collect(Collectors.toList());

    return intygDataList.stream()
        .filter(intygData -> activeIntygIds.contains(intygData.getIntygId()))
        .collect(Collectors.toList());
  }

  private static void assertCareProviderId(String careProviderId) {
    if (careProviderId == null || careProviderId.trim().isEmpty()) {
      throw new IllegalArgumentException(
          String.format("CareProviderId must have a valid value: '%s'", careProviderId));
    }
  }

  private static void assertUnitIds(List<String> unitIds) {
    if (unitIds == null
        || unitIds.isEmpty()
        || unitIds.stream().anyMatch(unitId -> unitId == null || unitId.trim().isEmpty())) {
      throw new IllegalArgumentException(
          String.format("UnitIds must have a valid value: '%s'", unitIds));
    }
  }

  private static void assertDoctorIds(List<String> doctorIds) {
    if (doctorIds != null
        && doctorIds.stream().anyMatch(doctorId -> doctorId == null || doctorId.trim().isEmpty())) {
      throw new IllegalArgumentException(
          String.format("DoctorIds must have a valid value: '%s'", doctorIds));
    }
  }
}
