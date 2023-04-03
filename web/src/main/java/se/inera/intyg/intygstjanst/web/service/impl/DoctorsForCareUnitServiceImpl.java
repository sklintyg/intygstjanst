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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.intygstjanst.web.service.DoctorsForCareUnitService;
import se.inera.intyg.intygstjanst.web.service.SickLeaveInformationService;
import se.inera.intyg.intygstjanst.web.service.dto.DoctorsRequestDTO;

@Service
public class DoctorsForCareUnitServiceImpl implements DoctorsForCareUnitService {

    private static final Logger LOG = LoggerFactory.getLogger(DoctorsForCareUnitServiceImpl.class);
    private final SickLeaveInformationService sickLeaveInformationService;

    public DoctorsForCareUnitServiceImpl(SickLeaveInformationService sickLeaveInformationService) {
        this.sickLeaveInformationService = sickLeaveInformationService;
    }

    @Override
    public List<Lakare> getActiveDoctorsForCareUnit(DoctorsRequestDTO doctorsRequestDTO) {
        final var careUnitId = doctorsRequestDTO.getCareUnitId();

        if (careUnitId == null || careUnitId.isEmpty()) {
            LOG.debug("No care unit id was provided, returning empty list.");
            return Collections.emptyList();
        }

        LOG.debug("Getting doctors with active sick leaves for care unit:  {}", careUnitId);

        //TODO: Implement search that gets doctors with active sick leaves for care unit.

        final var doctorIds = List.of("id1, id2");

        return doctorIds.stream()
            .map(sickLeaveInformationService::getEmployee)
            .sorted(Comparator.comparing(Lakare::getNamn))
            .collect(Collectors.toList());
    }
}
