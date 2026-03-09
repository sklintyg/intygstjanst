/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.application.reko.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.application.sickleave.dto.RekoStatusDTO;
import se.inera.intyg.intygstjanst.application.sickleave.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.Reko;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.RekoRepository;

@Service
public class RekoStatusDecorator {

  final RekoRepository rekoRepository;
  final RekoStatusFilter rekoStatusFilter;
  final RekoStatusConverter rekoStatusConverter;

  public RekoStatusDecorator(
      RekoRepository rekoRepository,
      RekoStatusFilter rekoStatusFilter,
      RekoStatusConverter rekoStatusConverter) {
    this.rekoRepository = rekoRepository;
    this.rekoStatusFilter = rekoStatusFilter;
    this.rekoStatusConverter = rekoStatusConverter;
  }

  public void decorate(List<SjukfallEnhet> sickLeaves, String careUnitId) {
    if (sickLeaves.isEmpty()) {
      return;
    }

    final var rekoStatuses =
        rekoRepository.findByPatientIdInAndCareUnitId(
            sickLeaves.stream()
                .map((sickLeave) -> sickLeave.getPatient().getId())
                .collect(Collectors.toList()),
            careUnitId);

    sickLeaves.forEach(
        (sickLeave) -> sickLeave.setRekoStatus(getRekoStatus(sickLeave, rekoStatuses)));
  }

  private RekoStatusDTO getRekoStatus(SjukfallEnhet sickLeave, List<Reko> rekoStatuses) {
    final var filteredRekoStatus =
        rekoStatusFilter.filter(
            rekoStatuses,
            sickLeave.getPatient().getId(),
            sickLeave.getSlut(),
            sickLeave.getStart());

    return filteredRekoStatus.map(rekoStatusConverter::convert).orElse(null);
  }
}
