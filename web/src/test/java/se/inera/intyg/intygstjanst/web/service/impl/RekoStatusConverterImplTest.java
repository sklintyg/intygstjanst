/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.persistence.model.dao.Reko;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RekoStatusConverterImplTest {

    private static final String PATIENT_ID = "PATIENT_ID";
    private static final String CARE_PROVIDER = "CARE_PROVIDER_ID";
    private static final String CARE_UNIT_ID = "CARE_UNIT_ID";
    private static final String UNIT_ID = "UNIT_ID";
    private static final String STAFF_ID = "STAFF_ID";
    private static final String STAFF_NAME = "STAFF_NAME";
    private static final LocalDateTime SICKLEAVE_TIMESTAMP = LocalDateTime.now();
    private static final LocalDateTime REGISTRATION_TIMESTAMP = LocalDateTime.now();
    private static final RekoStatusType STATUS = RekoStatusType.REKO_3;


    private Reko originalReko = new Reko();

    @BeforeEach
    void setup() {
        originalReko.setPatientId(PATIENT_ID);
        originalReko.setStatus(STATUS.toString());
        originalReko.setRegistrationTimestamp(REGISTRATION_TIMESTAMP);
        originalReko.setCareProviderId(CARE_PROVIDER);
        originalReko.setCareUnitId(CARE_UNIT_ID);
        originalReko.setUnitId(UNIT_ID);
        originalReko.setStaffId(STAFF_ID);
        originalReko.setStaffName(STAFF_NAME);
        originalReko.setSickLeaveTimestamp(SICKLEAVE_TIMESTAMP);
    }

    @InjectMocks
    RekoStatusConverterImpl rekoStatusConverter;

    @Test
    void shouldConvertPatientId() {
        final var response = rekoStatusConverter.convert(originalReko);

        assertEquals(PATIENT_ID, response.getPatientId());
    }

    @Test
    void shouldConvertCareProviderId() {
        final var response = rekoStatusConverter.convert(originalReko);

        assertEquals(CARE_PROVIDER, response.getCareProviderId());
    }

    @Test
    void shouldConvertCareUnitId() {
        final var response = rekoStatusConverter.convert(originalReko);

        assertEquals(CARE_UNIT_ID, response.getCareUnitId());
    }

    @Test
    void shouldConvertUnitId() {
        final var response = rekoStatusConverter.convert(originalReko);

        assertEquals(UNIT_ID, response.getUnitId());
    }

    @Test
    void shouldConvertStaffId() {
        final var response = rekoStatusConverter.convert(originalReko);

        assertEquals(STAFF_ID, response.getStaffId());
    }

    @Test
    void shouldConvertStaffName() {
        final var response = rekoStatusConverter.convert(originalReko);

        assertEquals(STAFF_NAME, response.getStaffName());
    }

    @Test
    void shouldConvertSickLeaveTimestamp() {
        final var response = rekoStatusConverter.convert(originalReko);

        assertEquals(SICKLEAVE_TIMESTAMP, response.getSickLeaveTimestamp());
    }

    @Test
    void shouldConvertRegistrationTimestamp() {
        final var response = rekoStatusConverter.convert(originalReko);

        assertEquals(REGISTRATION_TIMESTAMP, response.getRegistrationTimestamp());
    }

    @Test
    void shouldConvertStatusId() {
        final var response = rekoStatusConverter.convert(originalReko);

        assertEquals(STATUS.toString(), response.getStatus().getId());
    }

    @Test
    void shouldConvertStatusName() {
        final var response = rekoStatusConverter.convert(originalReko);

        assertEquals(STATUS.getName(), response.getStatus().getName());
    }
}
