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

import static se.inera.intyg.intygstjanst.web.integration.sickleave.SickLeaveLogMessageFactory.GET_ACTIVE_SICK_LEAVE_CERTIFICATES;
import static se.inera.intyg.intygstjanst.web.integration.sickleave.SickLeaveLogMessageFactory.GET_SICK_LEAVES;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.inera.intyg.intygstjanst.web.integration.sickleave.SickLeaveLogMessageFactory;
import se.inera.intyg.intygstjanst.web.service.FilterSickLeaves;
import se.inera.intyg.intygstjanst.web.service.GetActiveSickLeaveCertificates;
import se.inera.intyg.intygstjanst.web.service.GetSickLeaveCertificates;
import se.inera.intyg.intygstjanst.web.service.GetSickLeavesService;
import se.inera.intyg.intygstjanst.web.service.RekoStatusDecorator;
import se.inera.intyg.intygstjanst.web.service.dto.GetSickLeaveServiceRequest;

@Service
public class GetSickLeavesServiceImpl implements GetSickLeavesService {

    private static final Logger LOG = LoggerFactory.getLogger(GetSickLeavesServiceImpl.class);
    private final HsaService hsaService;
    private final GetActiveSickLeaveCertificates getActiveSickLeaveCertificates;
    private final GetSickLeaveCertificates getSickLeaveCertificates;
    private final FilterSickLeaves filterSickLeaves;
    private final RekoStatusDecorator rekoStatusDecorator;

    public GetSickLeavesServiceImpl(HsaService hsaService,
        GetActiveSickLeaveCertificates getActiveSickLeaveCertificates,
        GetSickLeaveCertificates getSickLeaveCertificates,
        FilterSickLeaves filterSickLeaves, RekoStatusDecorator rekoStatusDecorator) {
        this.hsaService = hsaService;
        this.getActiveSickLeaveCertificates = getActiveSickLeaveCertificates;
        this.getSickLeaveCertificates = getSickLeaveCertificates;
        this.filterSickLeaves = filterSickLeaves;
        this.rekoStatusDecorator = rekoStatusDecorator;
    }

    @Override
    public List<SjukfallEnhet> get(GetSickLeaveServiceRequest getSickLeaveServiceRequest) {
        final var careProviderId = hsaService.getHsaIdForVardgivare(getSickLeaveServiceRequest.getCareUnitId());
        final var careUnitAndSubUnits = hsaService.getHsaIdsForCareUnitAndSubUnits(getSickLeaveServiceRequest.getCareUnitId());

        final var sickLeaveLogMessageFactory = new SickLeaveLogMessageFactory(System.currentTimeMillis());
        final var intygData = getActiveSickLeaveCertificates.get(
            careProviderId,
            getUnitIdFromRequestIfProvided(getSickLeaveServiceRequest, careUnitAndSubUnits),
            getSickLeaveServiceRequest.getDoctorIds(),
            getSickLeaveServiceRequest.getMaxDaysSinceSickLeaveCompleted()
        );
        LOG.info(sickLeaveLogMessageFactory.message(GET_ACTIVE_SICK_LEAVE_CERTIFICATES, intygData.size()));

        if (intygData.isEmpty()) {
            return Collections.emptyList();
        }

        final var patientIds = intygData.stream()
            .map(IntygData::getPatientId)
            .collect(Collectors.toList());

        sickLeaveLogMessageFactory.setStartTimer(System.currentTimeMillis());
        final var sjukfallEnhetList = getSickLeaveCertificates.get(
            careProviderId,
            careUnitAndSubUnits,
            patientIds,
            getSickLeaveServiceRequest.getMaxCertificateGap(),
            getSickLeaveServiceRequest.getMaxDaysSinceSickLeaveCompleted(),
            getSickLeaveServiceRequest.getProtectedPersonFilterId()
        );
        LOG.info(sickLeaveLogMessageFactory.message(GET_SICK_LEAVES, intygData.size()));

        sickLeaveLogMessageFactory.setStartTimer(System.currentTimeMillis());
        rekoStatusDecorator.decorate(sjukfallEnhetList, getSickLeaveServiceRequest.getCareUnitId());
        LOG.info(sickLeaveLogMessageFactory.message(SickLeaveLogMessageFactory.DECORATE_REKO_STATUS, sjukfallEnhetList.size()));

        return filterSickLeaves.filter(
            sjukfallEnhetList,
            getSickLeaveServiceRequest.getSickLeaveLengthIntervals(),
            getSickLeaveServiceRequest.getDiagnosisChapters(),
            getSickLeaveServiceRequest.getFromPatientAge(),
            getSickLeaveServiceRequest.getToPatientAge(),
            getSickLeaveServiceRequest.getFromSickLeaveEndDate(),
            getSickLeaveServiceRequest.getToSickLeaveEndDate(),
            getSickLeaveServiceRequest.getDoctorIds(),
            getSickLeaveServiceRequest.getRekoStatusTypeIds(),
            getSickLeaveServiceRequest.getOccupationTypeIds(),
            getSickLeaveServiceRequest.getTextSearch());
    }

    private static List<String> getUnitIdFromRequestIfProvided(GetSickLeaveServiceRequest getSickLeaveServiceRequest,
        List<String> careUnitAndSubUnits) {
        return getSickLeaveServiceRequest.getUnitId() != null ? List.of(getSickLeaveServiceRequest.getUnitId()) : careUnitAndSubUnits;
    }
}
