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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.persistence.model.dao.Reko;
import se.inera.intyg.intygstjanst.persistence.model.dao.RekoRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateRekoStatusServiceImplTest {
    @Mock
    private RekoRepository rekoRepository;

    @InjectMocks
    private CreateRekoStatusServiceImpl creat;

    private static final String PATIENT_ID = "191212121212";
    private static final String STATUS = "REKO_1";
    private static final String CARE_PROVIDER_ID = "CareProviderId";
    private static final String CARE_UNIT_ID = "CareUnitId";
    private static final String UNIT_ID = "UnitId";
    private static final String STAFF_ID = "StaffId";
    private static final String STAFF_NAME = "StaffName";
    private static final LocalDateTime SICK_LEAVE_TIMESTAMP = LocalDateTime.now().plusDays(1);

    @Nested
    class TestRekoRepository {
        @Test
        void shouldCallWithCorrectPatientId() {
            final var captor = ArgumentCaptor.forClass(Reko.class);

            creat.create(
                    PATIENT_ID,
                    STATUS,
                    CARE_PROVIDER_ID,
                    CARE_UNIT_ID,
                    UNIT_ID,
                    STAFF_ID,
                    STAFF_NAME,
                    SICK_LEAVE_TIMESTAMP
            );
            verify(rekoRepository).save(captor.capture());

            assertEquals(PATIENT_ID, captor.getValue().getPatientId());
        }

        @Test
        void shouldCallWithCorrectStatus() {
            final var captor = ArgumentCaptor.forClass(Reko.class);

            creat.create(
                    PATIENT_ID,
                    STATUS,
                    CARE_PROVIDER_ID,
                    CARE_UNIT_ID,
                    UNIT_ID,
                    STAFF_ID,
                    STAFF_NAME,
                    SICK_LEAVE_TIMESTAMP
            );
            verify(rekoRepository).save(captor.capture());

            assertEquals(STATUS, captor.getValue().getStatus());
        }

        @Test
        void shouldCallWithCorrectCareProviderId() {
            final var captor = ArgumentCaptor.forClass(Reko.class);

            creat.create(
                    PATIENT_ID,
                    STATUS,
                    CARE_PROVIDER_ID,
                    CARE_UNIT_ID,
                    UNIT_ID,
                    STAFF_ID,
                    STAFF_NAME,
                    SICK_LEAVE_TIMESTAMP
            );
            verify(rekoRepository).save(captor.capture());

            assertEquals(CARE_PROVIDER_ID, captor.getValue().getCareProviderId());
        }

        @Test
        void shouldCallWithCorrectCareUnitId() {
            final var captor = ArgumentCaptor.forClass(Reko.class);

            creat.create(
                    PATIENT_ID,
                    STATUS,
                    CARE_PROVIDER_ID,
                    CARE_UNIT_ID,
                    UNIT_ID,
                    STAFF_ID,
                    STAFF_NAME,
                    SICK_LEAVE_TIMESTAMP
            );
            verify(rekoRepository).save(captor.capture());

            assertEquals(CARE_UNIT_ID, captor.getValue().getCareUnitId());
        }

        @Test
        void shouldCallWithCorrectUnitId() {
            final var captor = ArgumentCaptor.forClass(Reko.class);

            creat.create(
                    PATIENT_ID,
                    STATUS,
                    CARE_PROVIDER_ID,
                    CARE_UNIT_ID,
                    UNIT_ID,
                    STAFF_ID,
                    STAFF_NAME,
                    SICK_LEAVE_TIMESTAMP
            );
            verify(rekoRepository).save(captor.capture());

            assertEquals(UNIT_ID, captor.getValue().getUnitId());
        }

        @Test
        void shouldCallWithCorrectStaffId() {
            final var captor = ArgumentCaptor.forClass(Reko.class);

            creat.create(
                    PATIENT_ID,
                    STATUS,
                    CARE_PROVIDER_ID,
                    CARE_UNIT_ID,
                    UNIT_ID,
                    STAFF_ID,
                    STAFF_NAME,
                    SICK_LEAVE_TIMESTAMP
            );
            verify(rekoRepository).save(captor.capture());

            assertEquals(STAFF_ID, captor.getValue().getStaffId());
        }

        @Test
        void shouldCallWithCorrectStaffName() {
            final var captor = ArgumentCaptor.forClass(Reko.class);

            creat.create(
                    PATIENT_ID,
                    STATUS,
                    CARE_PROVIDER_ID,
                    CARE_UNIT_ID,
                    UNIT_ID,
                    STAFF_ID,
                    STAFF_NAME,
                    SICK_LEAVE_TIMESTAMP
            );
            verify(rekoRepository).save(captor.capture());

            assertEquals(STAFF_NAME, captor.getValue().getStaffName());
        }

        @Test
        void shouldCallWithCorrectSickLeaveTimestamp() {
            final var captor = ArgumentCaptor.forClass(Reko.class);

            creat.create(
                    PATIENT_ID,
                    STATUS,
                    CARE_PROVIDER_ID,
                    CARE_UNIT_ID,
                    UNIT_ID,
                    STAFF_ID,
                    STAFF_NAME,
                    SICK_LEAVE_TIMESTAMP
            );
            verify(rekoRepository).save(captor.capture());

            assertEquals(SICK_LEAVE_TIMESTAMP, captor.getValue().getSickLeaveTimestamp());
        }

        @Test
        void shouldCallWithCorrectRegistrationTimestamp() {
            final var captor = ArgumentCaptor.forClass(Reko.class);

            creat.create(
                    PATIENT_ID,
                    STATUS,
                    CARE_PROVIDER_ID,
                    CARE_UNIT_ID,
                    UNIT_ID,
                    STAFF_ID,
                    STAFF_NAME,
                    SICK_LEAVE_TIMESTAMP
            );
            verify(rekoRepository).save(captor.capture());

            assertNotNull(captor.getValue().getRegistrationTimestamp());
        }
    }
}

