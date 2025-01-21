/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.integration.sickleave.converter.IntygsDataConverter;
import se.inera.intyg.intygstjanst.web.service.GetActiveSickLeaveCertificates;

@Service
public class GetActiveSickLeaveCertificatesImpl implements GetActiveSickLeaveCertificates {

    private final SjukfallCertificateDao sjukfallCertificateDao;

    private final IntygsDataConverter intygDataConverter;

    private final SjukfallEngineService sjukfallEngineService;

    public GetActiveSickLeaveCertificatesImpl(SjukfallCertificateDao sjukfallCertificateDao,
        IntygsDataConverter intygDataConverter,
        SjukfallEngineService sjukfallEngineService) {
        this.sjukfallCertificateDao = sjukfallCertificateDao;
        this.intygDataConverter = intygDataConverter;
        this.sjukfallEngineService = sjukfallEngineService;
    }

    @Override
    public List<IntygData> get(String careProviderId, List<String> unitIds, List<String> doctorIds, int maxDaysSinceSickLeaveCompleted) {
        assertCareProviderId(careProviderId);
        assertUnitIds(unitIds);
        assertDoctorIds(doctorIds);

        final var todayDate = LocalDate.now();
        final var recentlyClosed = maxDaysSinceSickLeaveCompleted > 0 ? todayDate.minusDays(maxDaysSinceSickLeaveCompleted) : null;

        final var sjukfallCertificate = sjukfallCertificateDao.findActiveSjukfallCertificate(
            careProviderId,
            unitIds,
            doctorIds,
            todayDate,
            recentlyClosed
        );

        final var intygDataList = intygDataConverter.convert(sjukfallCertificate);

        final var activeIntygIds = sjukfallEngineService
            .beraknaSjukfallForEnhet(
                intygDataList,
                new IntygParametrar(
                    0,
                    maxDaysSinceSickLeaveCompleted,
                    todayDate
                )
            ).stream()
            .map(SjukfallEnhet::getAktivIntygsId)
            .collect(Collectors.toList());

        return intygDataList.stream()
            .filter(intygData -> activeIntygIds.contains(intygData.getIntygId()))
            .collect(Collectors.toList());
    }

    private static void assertCareProviderId(String careProviderId) {
        if (careProviderId == null || careProviderId.trim().isEmpty()) {
            throw new IllegalArgumentException(String.format("CareProviderId must have a valid value: '%s'", careProviderId));
        }
    }

    private static void assertUnitIds(List<String> unitIds) {
        if (unitIds == null || unitIds.isEmpty() || unitIds.stream().anyMatch(unitId -> unitId == null || unitId.trim().isEmpty())) {
            throw new IllegalArgumentException(String.format("UnitIds must have a valid value: '%s'", unitIds));
        }
    }

    private static void assertDoctorIds(List<String> doctorIds) {
        if (doctorIds != null && doctorIds.stream().anyMatch(doctorId -> doctorId == null || doctorId.trim().isEmpty())) {
            throw new IllegalArgumentException(String.format("DoctorIds must have a valid value: '%s'", doctorIds));
        }
    }
}
