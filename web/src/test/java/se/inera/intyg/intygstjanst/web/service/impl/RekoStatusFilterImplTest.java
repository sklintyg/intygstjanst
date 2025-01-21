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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.sjukfall.dto.*;
import se.inera.intyg.intygstjanst.persistence.model.dao.Reko;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RekoStatusFilterImplTest {

    @InjectMocks
    private RekoStatusFilterImpl rekoStatusFilter;

    private static final String PATIENT_ID_1 = "191212121213";
    private static final String PATIENT_ID_2 = "191212121212";
    private static final String PATIENT_ID_3 = "191212121211";
    private static final String PATIENT_ID_4 = "191212121210";
    private static final String PATIENT_ID_5 = "191212121219";

    private static final String WRONG_PATIENT_ID = "Wrong";
    private static final String CARE_UNIT_ID = "CareUnitId";
    private static List<SjukfallEnhet> SICK_LEAVES;
    private static final LocalDate SICK_LEAVE_TIMESTAMP = LocalDate.now();

    private static Reko getRekoStatus(String patientId, String status, LocalDateTime registrationTimestamp) {
        final var reko = new Reko();
        reko.setPatientId(patientId);
        reko.setStatus(status);
        reko.setCareUnitId(CARE_UNIT_ID);
        reko.setSickLeaveTimestamp(SICK_LEAVE_TIMESTAMP.atStartOfDay());
        reko.setRegistrationTimestamp(registrationTimestamp);
        return reko;
    }

    private static final List<Reko> REKO_STATUSES = Arrays.asList(
        getRekoStatus(PATIENT_ID_1, RekoStatusType.REKO_3.toString(), LocalDateTime.now()),
        getRekoStatus(PATIENT_ID_2, RekoStatusType.REKO_3.toString(), LocalDateTime.now()),
        getRekoStatus(PATIENT_ID_3, RekoStatusType.REKO_3.toString(), LocalDateTime.now()),
        getRekoStatus(PATIENT_ID_3, RekoStatusType.REKO_4.toString(), LocalDateTime.now().plusDays(1)),
        getRekoStatus(PATIENT_ID_4, RekoStatusType.REKO_5.toString(), LocalDateTime.now()),
        getRekoStatus(PATIENT_ID_5, RekoStatusType.REKO_6.toString(), LocalDateTime.now())
    );

    private SjukfallEnhet setUpSickLeave(String patientId, LocalDate start, LocalDate end) {
        final var sickLeave = new SjukfallEnhet();
        sickLeave.setPatient(Patient.create(patientId, "Name"));
        sickLeave.setStart(start);
        sickLeave.setSlut(end);
        sickLeave.setVardgivare(Vardgivare.create("id", "name"));
        sickLeave.setVardenhet(Vardenhet.create("id", "name"));
        sickLeave.setLakare(Lakare.create("id", "name"));
        return sickLeave;
    }

    @BeforeEach
    void setup() {
        final var sickLeaveWrongStartDate = setUpSickLeave(PATIENT_ID_1,
            SICK_LEAVE_TIMESTAMP.plusDays(10),
            SICK_LEAVE_TIMESTAMP.plusDays(20)
        );

        final var sickLeaveWrongEndDate = setUpSickLeave(PATIENT_ID_2,
            SICK_LEAVE_TIMESTAMP.minusDays(3),
            SICK_LEAVE_TIMESTAMP.minusDays(2)
        );

        final var sickLeaveWrongPatientId = setUpSickLeave(WRONG_PATIENT_ID,
            SICK_LEAVE_TIMESTAMP.minusDays(1),
            SICK_LEAVE_TIMESTAMP.plusDays(4)
        );

        final var sickLeaveSeveralStatuses = setUpSickLeave(PATIENT_ID_3,
            SICK_LEAVE_TIMESTAMP.minusDays(1),
            SICK_LEAVE_TIMESTAMP.plusDays(4)
        );

        final var sickLeaveWithSameDateAsStartDate = setUpSickLeave(PATIENT_ID_4,
            SICK_LEAVE_TIMESTAMP,
            SICK_LEAVE_TIMESTAMP.plusDays(4)
        );

        final var sickLeaveWithSameDateAsEndDate = setUpSickLeave(PATIENT_ID_5,
            SICK_LEAVE_TIMESTAMP.minusDays(1),
            SICK_LEAVE_TIMESTAMP
        );

        SICK_LEAVES = Arrays.asList(
            sickLeaveWrongStartDate,
            sickLeaveWrongEndDate,
            sickLeaveWrongPatientId,
            sickLeaveSeveralStatuses,
            sickLeaveWithSameDateAsStartDate,
            sickLeaveWithSameDateAsEndDate
        );
    }

    @Nested
    class FilterRekoStatus {

        @Test
        void shouldNotReturnRekoStatusForWrongPatientId() {
            final var result = rekoStatusFilter.filter(REKO_STATUSES,
                SICK_LEAVES.get(2).getPatient().getId(),
                SICK_LEAVES.get(2).getSlut(),
                SICK_LEAVES.get(2).getStart()
            );

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldNotReturnRekoStatusIfSickLeaveTimeStampIsBeforeStartDate() {
            final var result = rekoStatusFilter.filter(REKO_STATUSES,
                SICK_LEAVES.get(0).getPatient().getId(),
                SICK_LEAVES.get(0).getSlut(),
                SICK_LEAVES.get(0).getStart()
            );

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldNotReturnRekoStatusIfSickLeaveTimeStampIsAfterEndDate() {
            final var result = rekoStatusFilter.filter(REKO_STATUSES,
                SICK_LEAVES.get(1).getPatient().getId(),
                SICK_LEAVES.get(1).getSlut(),
                SICK_LEAVES.get(1).getStart()
            );

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldReturnLatestRekoStatusIfTwoAreCorrect() {
            final var result = rekoStatusFilter.filter(REKO_STATUSES,
                SICK_LEAVES.get(3).getPatient().getId(),
                SICK_LEAVES.get(3).getSlut(),
                SICK_LEAVES.get(3).getStart()
            );

            assertEquals(RekoStatusType.REKO_4.toString(), result.get().getStatus());
        }

        @Test
        void shouldReturnRekoStatusWithStartDateEqualToSickLeaveDate() {
            final var result = rekoStatusFilter.filter(
                REKO_STATUSES,
                SICK_LEAVES.get(4).getPatient().getId(),
                SICK_LEAVES.get(4).getSlut(),
                SICK_LEAVES.get(4).getStart()
            );

            assertEquals(RekoStatusType.REKO_5.toString(), result.get().getStatus());
        }

        @Test
        void shouldSetRekoStatusForSickLeaveWithEndDateEqualToSickLeaveDate() {
            final var result = rekoStatusFilter.filter(
                REKO_STATUSES,
                SICK_LEAVES.get(5).getPatient().getId(),
                SICK_LEAVES.get(5).getSlut(),
                SICK_LEAVES.get(5).getStart()
            );

            assertEquals(RekoStatusType.REKO_6.toString(), result.get().getStatus());
        }
    }

}

