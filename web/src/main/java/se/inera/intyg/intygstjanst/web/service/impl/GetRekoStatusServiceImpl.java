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

import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.RekoStatusDTO;
import se.inera.intyg.infra.sjukfall.dto.RekoStatusTypeDTO;
import se.inera.intyg.intygstjanst.persistence.model.dao.Reko;
import se.inera.intyg.intygstjanst.persistence.model.dao.RekoRepository;
import se.inera.intyg.intygstjanst.web.service.GetRekoStatusService;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusType;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class GetRekoStatusServiceImpl implements GetRekoStatusService {
    private final RekoRepository rekoRepository;

    public GetRekoStatusServiceImpl(RekoRepository rekoRepository) {
        this.rekoRepository = rekoRepository;
    }

    @Override
    public RekoStatusDTO get(String patientId, LocalDate endDate, LocalDate startDate) {
        final var rekoStatuses = rekoRepository.findByPatientId(patientId);
        return get(rekoStatuses, patientId, endDate, startDate);
    }

    @Override
    public RekoStatusDTO get(List<Reko> rekoStatuses, String patientId, LocalDate endDate, LocalDate startDate) {
        final var rekoStatusFromDb = filterRekoStatus(rekoStatuses, patientId, endDate,startDate);

        return rekoStatusFromDb.map(GetRekoStatusServiceImpl::convertRekoStatus).orElse(null);
    }

    private static Optional<Reko> filterRekoStatus(List<Reko> rekoStatuses, String patientId, LocalDate endDate, LocalDate startDate) {
        return rekoStatuses
                .stream()
                .filter(status -> status.getPatientId().equals(patientId))
                .filter(status -> equalsOrAfterStartDate(startDate, status))
                .filter(status -> beforeEndDate(endDate, status)
                ).max(Comparator.comparing(Reko::getRegistrationTimestamp));
    }

    private static RekoStatusDTO convertRekoStatus(Reko rekoStatusFromDb) {
        final var rekoStatus = new RekoStatusDTO();
        rekoStatus.setStatus(
                new RekoStatusTypeDTO(
                        rekoStatusFromDb.getStatus(),
                        RekoStatusType.fromId(rekoStatusFromDb.getStatus()).getName()
                )
        );
        rekoStatus.setRegistrationTimestamp(rekoStatusFromDb.getRegistrationTimestamp());
        rekoStatus.setPatientId(rekoStatusFromDb.getPatientId());
        rekoStatus.setCareProviderId(rekoStatusFromDb.getCareProviderId());
        rekoStatus.setCareUnitId(rekoStatusFromDb.getCareUnitId());
        rekoStatus.setUnitId(rekoStatusFromDb.getUnitId());
        rekoStatus.setPatientId(rekoStatusFromDb.getPatientId());
        rekoStatus.setStaffId(rekoStatusFromDb.getStaffId());
        rekoStatus.setStaffName(rekoStatusFromDb.getStaffName());
        rekoStatus.setSickLeaveTimestamp(rekoStatusFromDb.getSickLeaveTimestamp());

        return rekoStatus;
    }

    private static boolean beforeEndDate(LocalDate endDate, Reko status) {
        return status.getSickLeaveTimestamp().isBefore(endDate.plusDays(1).atStartOfDay());
    }

    private static boolean equalsOrAfterStartDate(LocalDate startDate, Reko status) {
        final var sickLeaveStartLocalDatetime = startDate.atStartOfDay();
        return status.getSickLeaveTimestamp().isAfter(sickLeaveStartLocalDatetime)
                || sickLeaveStartLocalDatetime.equals(status.getSickLeaveTimestamp());
    }
}
