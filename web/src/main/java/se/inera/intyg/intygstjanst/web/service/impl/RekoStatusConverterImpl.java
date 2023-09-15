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

import org.springframework.stereotype.Component;
import se.inera.intyg.infra.sjukfall.dto.RekoStatusDTO;
import se.inera.intyg.infra.sjukfall.dto.RekoStatusTypeDTO;
import se.inera.intyg.intygstjanst.persistence.model.dao.Reko;
import se.inera.intyg.intygstjanst.web.service.RekoStatusConverter;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusType;

@Component
public class RekoStatusConverterImpl implements RekoStatusConverter {
    @Override
    public RekoStatusDTO convert(Reko rekoStatus) {
        final var convertedRekoStatus = new RekoStatusDTO();
        convertedRekoStatus.setStatus(
                new RekoStatusTypeDTO(
                        rekoStatus.getStatus(),
                        RekoStatusType.fromId(rekoStatus.getStatus()).getName()
                )
        );
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
