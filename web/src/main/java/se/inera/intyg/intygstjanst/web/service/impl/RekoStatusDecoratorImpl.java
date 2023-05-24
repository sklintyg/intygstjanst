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
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.persistence.model.dao.Reko;
import se.inera.intyg.intygstjanst.persistence.model.dao.RekoRepository;
import se.inera.intyg.intygstjanst.web.service.RekoStatusDecorator;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusType;

import java.time.LocalDate;
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
        final var rekoStatuses = rekoRepository.findByPatientIdInAndCareUnitId(
                sickLeaves
                        .stream()
                        .map((sickLeave) -> sickLeave.getPatient().getId())
                        .collect(Collectors.toList()), careUnitId

        );

        sickLeaves.forEach((sickLeave) -> sickLeave.setRekoStatus(
                getRekoStatus(rekoStatuses,
                        sickLeave.getPatient().getId(),
                        sickLeave.getStart(),
                        sickLeave.getSlut())
                )
        );
    }

    private String getRekoStatus(List<Reko> rekoStatuses,
                                 String patientId,
                                 LocalDate startDate,
                                 LocalDate endDate) {
        final var rekoStatus = rekoStatuses
                .stream()
                .filter((status) -> status.getPatientId().equals(patientId)
                        && status.getSickLeaveTimestamp().isAfter(startDate.atStartOfDay())
                        && status.getSickLeaveTimestamp().isBefore(endDate.plusDays(1).atStartOfDay())
                ).max(Comparator.comparing(Reko::getRegistrationTimestamp));

        if (rekoStatus.isPresent()) {
            return RekoStatusType.fromId(rekoStatus.get().getStatus()).getName();
        }

        return RekoStatusType.REKO_1.getName();
    }
}
