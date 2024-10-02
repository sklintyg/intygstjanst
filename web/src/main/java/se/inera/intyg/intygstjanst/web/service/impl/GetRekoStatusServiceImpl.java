/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.RekoStatusDTO;
import se.inera.intyg.intygstjanst.persistence.model.dao.RekoRepository;
import se.inera.intyg.intygstjanst.web.service.GetRekoStatusService;
import se.inera.intyg.intygstjanst.web.service.RekoStatusConverter;
import se.inera.intyg.intygstjanst.web.service.RekoStatusFilter;

import java.time.LocalDate;

@Service
public class GetRekoStatusServiceImpl implements GetRekoStatusService {

    private final RekoRepository rekoRepository;
    private final RekoStatusFilter rekoStatusFilter;
    private final RekoStatusConverter rekoStatusConverter;

    public GetRekoStatusServiceImpl(RekoRepository rekoRepository,
        RekoStatusFilter rekoStatusFilter,
        RekoStatusConverter rekoStatusConverter) {
        this.rekoRepository = rekoRepository;
        this.rekoStatusFilter = rekoStatusFilter;
        this.rekoStatusConverter = rekoStatusConverter;
    }

    @Override
    public RekoStatusDTO get(
        String patientId,
        LocalDate endDate,
        LocalDate startDate,
        String careUnitId) {

        final var rekoStatuses = rekoRepository.findByPatientIdAndCareUnitId(patientId, careUnitId);
        final var filteredRekoStatus = rekoStatusFilter.filter(rekoStatuses, patientId, endDate, startDate);
        return filteredRekoStatus.map(rekoStatusConverter::convert).orElse(null);
    }
}
