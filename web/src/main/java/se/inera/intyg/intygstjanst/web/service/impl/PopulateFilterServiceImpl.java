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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.service.DiagnosisChapterService;
import se.inera.intyg.intygstjanst.web.service.DoctorsForCareUnitComponent;
import se.inera.intyg.intygstjanst.web.service.HsaServiceProvider;
import se.inera.intyg.intygstjanst.web.service.PopulateFilterService;
import se.inera.intyg.intygstjanst.web.service.dto.PopulateFiltersRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.PopulateFiltersResponseDTO;

@Service
public class PopulateFilterServiceImpl implements PopulateFilterService {

    private static final Logger LOG = LoggerFactory.getLogger(PopulateFilterServiceImpl.class);
    private final SjukfallCertificateDao sjukfallCertificateDao;
    private final DoctorsForCareUnitComponent doctorsForCareUnitComponent;
    private final HsaServiceProvider hsaServiceProvider;
    private final DiagnosisChapterService diagnosisChapterService;

    public PopulateFilterServiceImpl(SjukfallCertificateDao sjukfallCertificateDao, DoctorsForCareUnitComponent doctorsForCareUnitComponent,
        HsaServiceProvider hsaServiceProvider, DiagnosisChapterService diagnosisChapterService) {
        this.sjukfallCertificateDao = sjukfallCertificateDao;
        this.doctorsForCareUnitComponent = doctorsForCareUnitComponent;
        this.hsaServiceProvider = hsaServiceProvider;
        this.diagnosisChapterService = diagnosisChapterService;
    }

    @Override
    public PopulateFiltersResponseDTO populateFilters(PopulateFiltersRequestDTO populateFiltersRequestDTO) {
        final var careUnitId = populateFiltersRequestDTO.getCareUnitId();
        final var maxDaysSinceSickLeaveCompleted = populateFiltersRequestDTO.getMaxDaysSinceSickLeaveCompleted();

        if (careUnitId == null || careUnitId.isEmpty()) {
            LOG.debug("No care unit id was provided, returning empty response.");
            return new PopulateFiltersResponseDTO(Collections.emptyList(), Collections.emptyList());
        }
        final var careGiverHsaId = hsaServiceProvider.getCareGiverHsaId(careUnitId);
        final var unitAndRelatedSubUnits = hsaServiceProvider.getUnitAndRelatedSubUnits(careUnitId);

        LOG.debug("Getting active sick leaves for care unit:  {}", careUnitId);

        final var sickLeaveCertificates = filterOnUnitIdIfProvidedAndTestCertificate(
            sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(careGiverHsaId,
                unitAndRelatedSubUnits, maxDaysSinceSickLeaveCompleted), populateFiltersRequestDTO.getUnitId());

        final var doctorsForCareUnit = doctorsForCareUnitComponent.getDoctorsForCareUnit(sickLeaveCertificates,
            populateFiltersRequestDTO.getDoctorId());
        final var diagnosisChaptersForCareUnit = diagnosisChapterService.getDiagnosisChaptersFromSickLeaveCertificate(
            sickLeaveCertificates);

        return new PopulateFiltersResponseDTO(doctorsForCareUnit, diagnosisChaptersForCareUnit);
    }

    private List<SjukfallCertificate> filterOnUnitIdIfProvidedAndTestCertificate(List<SjukfallCertificate> sickLeaveCertificates, String unitId) {
        List<SjukfallCertificate> filteredSickLeaves = new ArrayList<>(sickLeaveCertificates);
        if (unitId != null) {
            filteredSickLeaves = filteredSickLeaves.stream()
                .filter(sickLeave -> sickLeave.getCareUnitId().equals(unitId))
                .collect(Collectors.toList());
        }
        return filteredSickLeaves.stream().filter(sickLeave -> !sickLeave.isTestCertificate()).collect(Collectors.toList());
    }

}
