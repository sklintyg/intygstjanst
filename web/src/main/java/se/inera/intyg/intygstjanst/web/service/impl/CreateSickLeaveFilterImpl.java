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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.intygstjanst.web.service.CreateSickLeaveFilter;
import se.inera.intyg.intygstjanst.web.service.DiagnosisChapterService;
import se.inera.intyg.intygstjanst.web.service.dto.GetSickLeaveFilterServiceResponse;

@Service
public class CreateSickLeaveFilterImpl implements CreateSickLeaveFilter {

    private final DiagnosisChapterService diagnosisChapterService;

    public CreateSickLeaveFilterImpl(DiagnosisChapterService diagnosisChapterService) {
        this.diagnosisChapterService = diagnosisChapterService;
    }

    @Override
    public GetSickLeaveFilterServiceResponse create(List<IntygData> intygDataList) {
        if (intygDataList.isEmpty()) {
            return GetSickLeaveFilterServiceResponse.builder().build();
        }

        final var doctorsForCareUnit = intygDataList.stream()
            .map(IntygData::getLakareId)
            .distinct()
            .filter(Objects::nonNull)
            .map(doctorId -> Lakare.create(doctorId, doctorId))
            .collect(Collectors.toList());

        final var diagnosisChaptersForCareUnit = intygDataList.stream()
            .map(IntygData::getDiagnosKod)
            .distinct()
            .filter(Objects::nonNull)
            .map(diagnosisChapterService::getDiagnosisChapter)
            .distinct()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return GetSickLeaveFilterServiceResponse.builder()
            .activeDoctors(doctorsForCareUnit)
            .diagnosisChapters(diagnosisChaptersForCareUnit)
            .build();
    }
}
