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

import org.springframework.stereotype.Component;
import se.inera.intyg.intygstjanst.application.reko.dto.RekoStatusType;
import se.inera.intyg.intygstjanst.application.sickleave.dto.RekoStatusDTO;
import se.inera.intyg.intygstjanst.application.sickleave.dto.RekoStatusTypeDTO;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.Reko;

@Component
public class RekoStatusConverter {

  public RekoStatusDTO convert(Reko rekoStatus) {
    final var convertedRekoStatus = new RekoStatusDTO();
    convertedRekoStatus.setStatus(
        new RekoStatusTypeDTO(
            rekoStatus.getStatus(), RekoStatusType.fromId(rekoStatus.getStatus()).getName()));
    convertedRekoStatus.setRegistrationTimestamp(rekoStatus.getRegistrationTimestamp());
    convertedRekoStatus.setPatientId(rekoStatus.getPatientId());
    convertedRekoStatus.setCareProviderId(rekoStatus.getCareProviderId());
    convertedRekoStatus.setCareUnitId(rekoStatus.getCareUnitId());
    convertedRekoStatus.setUnitId(rekoStatus.getUnitId());
    convertedRekoStatus.setPatientId(rekoStatus.getPatientId());
    convertedRekoStatus.setStaffId(rekoStatus.getStaffId());
    convertedRekoStatus.setStaffName(rekoStatus.getStaffName());
    convertedRekoStatus.setSickLeaveTimestamp(rekoStatus.getSickLeaveTimestamp());

    return convertedRekoStatus;
  }
}
