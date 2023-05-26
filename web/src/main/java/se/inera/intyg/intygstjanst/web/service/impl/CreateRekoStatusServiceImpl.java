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
import se.inera.intyg.infra.sjukfall.dto.RekoStatusTypeDTO;
import se.inera.intyg.intygstjanst.persistence.model.dao.Reko;
import se.inera.intyg.intygstjanst.persistence.model.dao.RekoRepository;
import se.inera.intyg.intygstjanst.web.service.CreateRekoStatusService;
import se.inera.intyg.infra.sjukfall.dto.RekoStatusDTO;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusType;

import java.time.LocalDateTime;

@Service
public class CreateRekoStatusServiceImpl implements CreateRekoStatusService {
    private final RekoRepository rekoRepository;

    public CreateRekoStatusServiceImpl(RekoRepository rekoRepository) {
        this.rekoRepository = rekoRepository;
    }

    @Override
    public RekoStatusDTO create(String patientId,
                                String status,
                                String careProviderId,
                                String careUnitId,
                                String unitId,
                                String staffId,
                                String staffName,
                                LocalDateTime sickLeaveTimestamp) {

        final var reko = getReko(
                status,
                patientId,
                careProviderId,
                careUnitId,
                unitId,
                staffId,
                staffName,
                sickLeaveTimestamp
        );

        rekoRepository.save(reko);

        return new RekoStatusDTO(
                new RekoStatusTypeDTO(status, RekoStatusType.fromId(status).getName()),
                patientId,
                careProviderId,
                careUnitId,
                unitId,
                staffId,
                staffName,
                sickLeaveTimestamp,
                reko.getRegistrationTimestamp()
        );
    }

    private Reko getReko(String status,
                         String patientId,
                         String careProviderId,
                         String careUnitId,
                         String unitId,
                         String staffId,
                         String staffName,
                         LocalDateTime sickLeaveTimestamp) {
        final var reko = new Reko();
        reko.setPatientId(patientId);
        reko.setStatus(status);
        reko.setSickLeaveTimestamp(sickLeaveTimestamp);
        reko.setCareProviderId(careProviderId);
        reko.setCareUnitId(careUnitId);
        reko.setUnitId(unitId);
        reko.setRegistrationTimestamp(LocalDateTime.now());
        reko.setStaffId(staffId);
        reko.setStaffName(staffName);

        return reko;
    }
}
