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

import static se.inera.intyg.intygstjanst.web.integration.sickleave.SickLeaveLogMessageFactory.GET_ACTIVE_SICK_LEAVE_CERTIFICATES;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.inera.intyg.intygstjanst.web.integration.sickleave.SickLeaveLogMessageFactory;
import se.inera.intyg.intygstjanst.web.service.CreateSickLeaveFilter;
import se.inera.intyg.intygstjanst.web.service.PuFilterService;
import se.inera.intyg.intygstjanst.web.service.GetActiveSickLeaveCertificates;
import se.inera.intyg.intygstjanst.web.service.GetSickLeaveFilterService;
import se.inera.intyg.intygstjanst.web.service.dto.GetSickLeaveFilterServiceRequest;
import se.inera.intyg.intygstjanst.web.service.dto.GetSickLeaveFilterServiceResponse;

@Service
public class GetSickLeaveFilterServiceImpl implements GetSickLeaveFilterService {

    private static final Logger LOG = LoggerFactory.getLogger(GetSickLeaveFilterServiceImpl.class);
    private final HsaService hsaService;
    private final GetActiveSickLeaveCertificates getActiveSickLeaveCertificates;
    private final CreateSickLeaveFilter createSickLeaveFilter;
    private final PuFilterService puFilterService;

    public GetSickLeaveFilterServiceImpl(HsaService hsaService, GetActiveSickLeaveCertificates getActiveSickLeaveCertificates,
                                         CreateSickLeaveFilter createSickLeaveFilter, PuFilterService puFilterService) {
        this.getActiveSickLeaveCertificates = getActiveSickLeaveCertificates;
        this.hsaService = hsaService;
        this.createSickLeaveFilter = createSickLeaveFilter;
        this.puFilterService = puFilterService;
    }

    @Override
    public GetSickLeaveFilterServiceResponse get(GetSickLeaveFilterServiceRequest getSickLeaveFilterServiceRequest) {
        final var careProviderId = hsaService.getHsaIdForVardgivare(getSickLeaveFilterServiceRequest.getCareUnitId());
        final var careUnitAndSubUnits = hsaService.getHsaIdsForCareUnitAndSubUnits(getSickLeaveFilterServiceRequest.getCareUnitId());

        final var sickLeaveLogMessageFactory = new SickLeaveLogMessageFactory(System.currentTimeMillis());
        final var intygDataList = getActiveSickLeaveCertificates.get(
            careProviderId,
            getUnitIdFromRequestIfProvided(getSickLeaveFilterServiceRequest.getUnitId(), careUnitAndSubUnits),
            getSickLeaveFilterServiceRequest.getDoctorId() != null ? List.of(getSickLeaveFilterServiceRequest.getDoctorId()) : null,
            getSickLeaveFilterServiceRequest.getMaxDaysSinceSickLeaveCompleted()
        );
        puFilterService.enrichWithPatientNameAndFilter(intygDataList, getSickLeaveFilterServiceRequest.isFilterProtectedPerson());
        LOG.info(sickLeaveLogMessageFactory.message(GET_ACTIVE_SICK_LEAVE_CERTIFICATES, intygDataList.size()));

        return createSickLeaveFilter.create(intygDataList);
    }

    private static List<String> getUnitIdFromRequestIfProvided(String unitId, List<String> careUnitAndSubUnits) {
        return unitId != null ? List.of(unitId) : careUnitAndSubUnits;
    }
}
