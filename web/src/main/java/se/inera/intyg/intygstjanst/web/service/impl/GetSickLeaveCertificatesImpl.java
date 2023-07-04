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

import static se.inera.intyg.intygstjanst.web.integration.sickleave.SickLeaveLogMessageFactory.GET_AND_FILTER_PROTECTED_PATIENTS;
import static se.inera.intyg.intygstjanst.web.integration.sickleave.SickLeaveLogMessageFactory.GET_SICK_LEAVES_FROM_DB;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.integration.sickleave.SickLeaveLogMessageFactory;
import se.inera.intyg.intygstjanst.web.integration.sickleave.converter.IntygsDataConverter;
import se.inera.intyg.intygstjanst.web.service.GetSickLeaveCertificates;
import se.inera.intyg.intygstjanst.web.service.PuFilterService;

@Service
public class GetSickLeaveCertificatesImpl implements GetSickLeaveCertificates {

    private static final Logger LOG = LoggerFactory.getLogger(GetSickLeaveCertificates.class);

    private final SjukfallCertificateDao sjukfallCertificateDao;

    private final IntygsDataConverter intygDataConverter;

    private final SjukfallEngineService sjukfallEngineService;

    private final PuFilterService puFilterService;


    public GetSickLeaveCertificatesImpl(SjukfallCertificateDao sjukfallCertificateDao, IntygsDataConverter intygDataConverter,
        SjukfallEngineService sjukfallEngineService, PuFilterService puFilterService) {
        this.sjukfallCertificateDao = sjukfallCertificateDao;
        this.intygDataConverter = intygDataConverter;
        this.sjukfallEngineService = sjukfallEngineService;
        this.puFilterService = puFilterService;
    }

    @Override
    public List<SjukfallEnhet> get(String careProviderId, List<String> unitIds, List<String> patientIds, int maxCertificateGap,
        int maxDaysSinceSickLeaveCompleted, String protectedPersonFilterId) {
        assertCareProviderId(careProviderId);
        assertUnitIds(unitIds);
        assertPatientIds(patientIds);

        final var sickLeaveLogMessageFactory = new SickLeaveLogMessageFactory(System.currentTimeMillis());
        final var sjukfallCertificate = sjukfallCertificateDao.findAllSjukfallCertificate(
            careProviderId,
            unitIds,
            patientIds
        );
        LOG.info(sickLeaveLogMessageFactory.message(GET_SICK_LEAVES_FROM_DB, sjukfallCertificate.size()));

        final var intygDataList = intygDataConverter.convert(sjukfallCertificate);

        sickLeaveLogMessageFactory.setStartTimer(System.currentTimeMillis());
        puFilterService.enrichWithPatientNameAndFilter(intygDataList, protectedPersonFilterId);
        LOG.info(sickLeaveLogMessageFactory.message(GET_AND_FILTER_PROTECTED_PATIENTS, intygDataList.size()));

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
