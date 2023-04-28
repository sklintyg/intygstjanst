/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.integration.sickleave.converter.IntygsDataConverter;
import se.inera.intyg.intygstjanst.web.service.GetSickLeaveCertificates;

@Service
public class GetSickLeaveCertificatesImpl implements GetSickLeaveCertificates {

    private final SjukfallCertificateDao sjukfallCertificateDao;

    private final IntygsDataConverter intygDataConverter;

    private final SjukfallEngineService sjukfallEngineService;

    public GetSickLeaveCertificatesImpl(SjukfallCertificateDao sjukfallCertificateDao, IntygsDataConverter intygDataConverter,
        SjukfallEngineService sjukfallEngineService) {
        this.sjukfallCertificateDao = sjukfallCertificateDao;
        this.intygDataConverter = intygDataConverter;
        this.sjukfallEngineService = sjukfallEngineService;
    }

    @Override
    public List<SjukfallEnhet> get(String careProviderId, List<String> unitIds, List<String> patientIds, int maxCertificateGap,
        int maxDaysSinceSickLeaveCompleted) {
        assertCareProviderId(careProviderId);
        assertUnitIds(unitIds);
        assertPatientIds(patientIds);

        final var sjukfallCertificate = sjukfallCertificateDao.findAllSjukfallCertificate(
            careProviderId,
            unitIds,
            patientIds
        );

        final var intygDataList = intygDataConverter.convert(sjukfallCertificate);

        return sjukfallEngineService.beraknaSjukfallForEnhet(
            intygDataList,
            new IntygParametrar(
                maxCertificateGap,
                maxDaysSinceSickLeaveCompleted,
                LocalDate.now()
            )
        );
    }

    private static void assertCareProviderId(String careProviderId) {
        if (isNullOrEmpty(careProviderId)) {
            throw new IllegalArgumentException(String.format("CareProviderId must have a valid value: '%s'", careProviderId));
        }
    }

    private static void assertUnitIds(List<String> unitIds) {
        if (unitIds == null || unitIds.isEmpty() || unitIds.stream().anyMatch(GetSickLeaveCertificatesImpl::isNullOrEmpty)) {
            throw new IllegalArgumentException(String.format("UnitIds must have a valid value: '%s'", unitIds));
        }
    }

    private static void assertPatientIds(List<String> patientIds) {
        if (patientIds == null || patientIds.isEmpty() || patientIds.stream().anyMatch(GetSickLeaveCertificatesImpl::isNullOrEmpty)) {
            throw new IllegalArgumentException(String.format("PatientIds must have a valid value: '%s'", patientIds));
        }
    }

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
