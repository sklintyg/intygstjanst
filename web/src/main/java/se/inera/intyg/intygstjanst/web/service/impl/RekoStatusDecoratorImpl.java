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
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.persistence.model.dao.Reko;
import se.inera.intyg.intygstjanst.persistence.model.dao.RekoRepository;
import se.inera.intyg.intygstjanst.web.service.RekoStatusDecorator;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusType;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RekoStatusDecoratorImpl implements RekoStatusDecorator {

    final RekoRepository rekoRepository;

    public RekoStatusDecoratorImpl(RekoRepository rekoRepository) {
        this.rekoRepository = rekoRepository;
    }

    @Override
    public void decorate(List<SjukfallEnhet> sickLeaves, String careUnitId) {
        if (sickLeaves.size() == 0) {
            return;
        }

        final var rekoStatuses = rekoRepository.findByPatientIdInAndCareUnitId(
                sickLeaves
                        .stream()
                        .map((sickLeave) -> sickLeave.getPatient().getId())
                        .collect(Collectors.toList()), careUnitId

        );

        sickLeaves.forEach((sickLeave) -> sickLeave.setRekoStatus(
                getRekoStatus(rekoStatuses,
                        sickLeave.getPatient().getId(),
                        sickLeave)
                )
        );
    }

    private RekoStatusDTO getRekoStatus(List<Reko> rekoStatuses,
                                        String patientId,
                                        SjukfallEnhet sickLeave) {
        final var rekoStatusFromDb = rekoStatuses
                .stream()
                .filter(status -> status.getPatientId().equals(patientId))
                .filter(status -> equalsOrAfterStartDate(sickLeave, status))
                .filter(status -> beforeEndDate(sickLeave, status)
                ).max(Comparator.comparing(Reko::getRegistrationTimestamp));

        if (rekoStatusFromDb.isPresent()) {
            final var rekoStatus = new RekoStatusDTO();
            rekoStatus.setStatus(
                    new RekoStatusTypeDTO(
                            rekoStatusFromDb.get().getStatus(),
                            RekoStatusType.fromId(rekoStatusFromDb.get().getStatus()).getName()
                    )
            );
            rekoStatus.setRegistrationTimestamp(rekoStatusFromDb.get().getRegistrationTimestamp());
            rekoStatus.setPatientId(rekoStatusFromDb.get().getPatientId());
            rekoStatus.setCareProviderId(rekoStatusFromDb.get().getCareProviderId());
            rekoStatus.setCareUnitId(rekoStatusFromDb.get().getCareUnitId());
            rekoStatus.setUnitId(rekoStatusFromDb.get().getUnitId());
            rekoStatus.setPatientId(rekoStatusFromDb.get().getPatientId());
            rekoStatus.setStaffId(rekoStatusFromDb.get().getStaffId());
            rekoStatus.setStaffName(rekoStatusFromDb.get().getStaffName());
            rekoStatus.setSickLeaveTimestamp(rekoStatusFromDb.get().getSickLeaveTimestamp());

            return rekoStatus;
        }

        return null;
    }

    private static boolean beforeEndDate(SjukfallEnhet sickLeave, Reko status) {
        return status.getSickLeaveTimestamp().isBefore(sickLeave.getSlut().plusDays(1).atStartOfDay());
    }

    private static boolean equalsOrAfterStartDate(SjukfallEnhet sickLeave, Reko status) {
        final var sickLeaveStartLocalDatetime = sickLeave.getStart().atStartOfDay();
        return status.getSickLeaveTimestamp().isAfter(sickLeaveStartLocalDatetime)
                || sickLeaveStartLocalDatetime.equals(status.getSickLeaveTimestamp());
    }
}