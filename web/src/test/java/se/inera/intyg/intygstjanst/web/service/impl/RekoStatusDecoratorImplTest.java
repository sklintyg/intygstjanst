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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.sjukfall.dto.*;
import se.inera.intyg.intygstjanst.persistence.model.dao.Reko;
import se.inera.intyg.intygstjanst.persistence.model.dao.RekoRepository;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RekoStatusDecoratorImplTest {
    @Mock
    private RekoRepository rekoRepository;

    @InjectMocks
    private RekoStatusDecoratorImpl rekoStatusDecorator;

    private static final String PATIENT_ID_1 = "191212121213";
    private static final String PATIENT_ID_2 = "191212121212";
    private static final String PATIENT_ID_3 = "191212121211";
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
            getRekoStatus(PATIENT_ID_3, RekoStatusType.REKO_4.toString(), LocalDateTime.now().plusDays(1))
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

        SICK_LEAVES = Arrays.asList(
                sickLeaveWrongStartDate, sickLeaveWrongEndDate, sickLeaveWrongPatientId, sickLeaveSeveralStatuses
        );
    }

    @Nested
    class TestRekoRepository {
        @Test
        void shouldCallWithCorrectPatientIds() {
            final var captor = ArgumentCaptor.forClass(List.class);

            rekoStatusDecorator.decorate(SICK_LEAVES, CARE_UNIT_ID);
            verify(rekoRepository).findByPatientIdInAndCareUnitId(captor.capture(), anyString());

            assertEquals(4, captor.getValue().size());
            assertEquals(PATIENT_ID_1, captor.getValue().get(0));
            assertEquals(PATIENT_ID_2, captor.getValue().get(1));
            assertEquals(WRONG_PATIENT_ID, captor.getValue().get(2));
            assertEquals(PATIENT_ID_3, captor.getValue().get(3));
        }

        @Test
        void shouldCallWithCorrectCareProviderId() {
            final var captor = ArgumentCaptor.forClass(String.class);

            rekoStatusDecorator.decorate(SICK_LEAVES, CARE_UNIT_ID);
            verify(rekoRepository).findByPatientIdInAndCareUnitId(anyList(), captor.capture());

            assertEquals(CARE_UNIT_ID, captor.getValue());
        }
    }

    @Nested
    class TestRekoStatusDecoration {
        @BeforeEach
        void setup() {
            when(rekoRepository.findByPatientIdInAndCareUnitId(anyList(), anyString())).thenReturn(REKO_STATUSES);
        }

        @Test
        void shouldNotSetRekoStatusForWrongPatientId() {
            rekoStatusDecorator.decorate(SICK_LEAVES, CARE_UNIT_ID);

            assertEquals(RekoStatusType.REKO_1.getName(), getSickLeaveFromPatientId(WRONG_PATIENT_ID).getRekoStatus().getStatus());
            assertEquals(RekoStatusType.REKO_1.toString(), getSickLeaveFromPatientId(WRONG_PATIENT_ID).getRekoStatus().getId());
        }

        @Test
        void shouldNotSetRekoStatusIfSickLeaveTimeStampIsBeforeStartDate() {
            rekoStatusDecorator.decorate(SICK_LEAVES, CARE_UNIT_ID);

            assertEquals(RekoStatusType.REKO_1.getName(), getSickLeaveFromPatientId(PATIENT_ID_1).getRekoStatus().getStatus());
            assertEquals(RekoStatusType.REKO_1.toString(), getSickLeaveFromPatientId(PATIENT_ID_1).getRekoStatus().getId());
        }

        @Test
        void shouldNotSetRekoStatusIfSickLeaveTimeStampIsAfterEndDate() {
            rekoStatusDecorator.decorate(SICK_LEAVES, CARE_UNIT_ID);

            assertEquals(RekoStatusType.REKO_1.getName(), getSickLeaveFromPatientId(PATIENT_ID_2).getRekoStatus().getStatus());
            assertEquals(RekoStatusType.REKO_1.toString(), getSickLeaveFromPatientId(PATIENT_ID_2).getRekoStatus().getId());
        }

        @Test
        void shouldSetLatestRekoStatusIfTwoAreCorrect() {
            rekoStatusDecorator.decorate(SICK_LEAVES, CARE_UNIT_ID);

            assertEquals(RekoStatusType.REKO_4.getName(), getSickLeaveFromPatientId(PATIENT_ID_3).getRekoStatus().getStatus());
            assertEquals(RekoStatusType.REKO_4.toString(), getSickLeaveFromPatientId(PATIENT_ID_3).getRekoStatus().getId());
        }
    }

    private SjukfallEnhet getSickLeaveFromPatientId(String patientId) {
        return SICK_LEAVES.stream().filter((sickLeave) -> sickLeave.getPatient().getId().equals(patientId)).findFirst().get();
    }
}

