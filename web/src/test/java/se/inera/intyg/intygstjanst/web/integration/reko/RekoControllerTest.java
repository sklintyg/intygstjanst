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

package se.inera.intyg.intygstjanst.web.integration.reko;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.web.service.CreateRekoStatusService;
import se.inera.intyg.intygstjanst.web.service.GetRekoStatusService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RekoControllerTest {

    @Mock
    private CreateRekoStatusService createRekoStatusService;

    @Mock
    private GetRekoStatusService getRekoStatusService;

    @InjectMocks
    private RekoController rekoController;

    private static final String PATIENT_ID = "191212121212";
    private static final String STATUS = "REKO_1";
    private static final String CARE_PROVIDER_ID = "CareProviderId";
    private static final String CARE_UNIT_ID = "CareUnitId";
    private static final String UNIT_ID = "UnitId";
    private static final String STAFF_ID = "StaffId";
    private static final String STAFF_NAME = "StaffName";
    private static final LocalDateTime SICK_LEAVE_TIMESTAMP = LocalDateTime.now().plusDays(1);

    final CreateRekoStatusRequestDTO expectedRequest = new CreateRekoStatusRequestDTO(
        PATIENT_ID,
        STATUS,
        CARE_PROVIDER_ID,
        CARE_UNIT_ID,
        UNIT_ID,
        STAFF_ID,
        STAFF_NAME,
        SICK_LEAVE_TIMESTAMP
    );

    final GetRekoStatusRequestDTO expectedGetRequest = new GetRekoStatusRequestDTO(
        PATIENT_ID,
        LocalDate.now(),
        LocalDate.now().minusDays(2),
        CARE_UNIT_ID
    );


    @Test
    void shouldCallSetRekoStatusServiceWithCorrectPatientId() {
        final var captor = ArgumentCaptor.forClass(String.class);

        rekoController.createRekoStatus(expectedRequest);
        verify(createRekoStatusService).create(
            captor.capture(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            any(LocalDateTime.class)
        );

        assertEquals(PATIENT_ID, captor.getValue());
    }

    @Test
    void shouldCallSetRekoStatusServiceWithCorrectStatus() {
        final var captor = ArgumentCaptor.forClass(String.class);

        rekoController.createRekoStatus(expectedRequest);
        verify(createRekoStatusService).create(
            anyString(),
            captor.capture(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            any(LocalDateTime.class)
        );

        assertEquals(STATUS, captor.getValue());
    }

    @Test
    void shouldCallSetRekoStatusServiceWithCorrectCareProviderId() {
        final var captor = ArgumentCaptor.forClass(String.class);

        rekoController.createRekoStatus(expectedRequest);
        verify(createRekoStatusService).create(
            anyString(),
            anyString(),
            captor.capture(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            any(LocalDateTime.class)
        );

        assertEquals(CARE_PROVIDER_ID, captor.getValue());
    }

    @Test
    void shouldCallSetRekoStatusServiceWithCorrectCareUnitId() {
        final var captor = ArgumentCaptor.forClass(String.class);

        rekoController.createRekoStatus(expectedRequest);
        verify(createRekoStatusService).create(
            anyString(),
            anyString(),
            anyString(),
            captor.capture(),
            anyString(),
            anyString(),
            anyString(),
            any(LocalDateTime.class)
        );

        assertEquals(CARE_UNIT_ID, captor.getValue());
    }

    @Test
    void shouldCallSetRekoStatusServiceWithCorrectUnitId() {
        final var captor = ArgumentCaptor.forClass(String.class);

        rekoController.createRekoStatus(expectedRequest);
        verify(createRekoStatusService).create(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            captor.capture(),
            anyString(),
            anyString(),
            any(LocalDateTime.class)
        );

        assertEquals(UNIT_ID, captor.getValue());
    }

    @Test
    void shouldCallSetRekoStatusServiceWithCorrectStaffId() {
        final var captor = ArgumentCaptor.forClass(String.class);

        rekoController.createRekoStatus(expectedRequest);
        verify(createRekoStatusService).create(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            captor.capture(),
            anyString(),
            any(LocalDateTime.class)
        );

        assertEquals(STAFF_ID, captor.getValue());
    }

    @Test
    void shouldCallSetRekoStatusServiceWithCorrectStaffName() {
        final var captor = ArgumentCaptor.forClass(String.class);

        rekoController.createRekoStatus(expectedRequest);
        verify(createRekoStatusService).create(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            captor.capture(),
            any(LocalDateTime.class)
        );

        assertEquals(STAFF_NAME, captor.getValue());
    }

    @Test
    void shouldCallSetRekoStatusServiceWithCorrectSickLeaveTimestamp() {
        final var captor = ArgumentCaptor.forClass(LocalDateTime.class);

        rekoController.createRekoStatus(expectedRequest);
        verify(createRekoStatusService).create(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            captor.capture()
        );

        assertEquals(SICK_LEAVE_TIMESTAMP, captor.getValue());
    }

    @Test
    void shouldCallGetRekoStatusServiceWithCorrectPatientId() {
        final var captor = ArgumentCaptor.forClass(String.class);

        rekoController.getRekoStatus(expectedGetRequest);
        verify(getRekoStatusService).get(
            captor.capture(),
            any(LocalDate.class),
            any(LocalDate.class),
            anyString()
        );

        assertEquals(PATIENT_ID, captor.getValue());
    }

    @Test
    void shouldCallGetRekoStatusServiceWithCorrectEndDate() {
        final var captor = ArgumentCaptor.forClass(LocalDate.class);

        rekoController.getRekoStatus(expectedGetRequest);
        verify(getRekoStatusService).get(
            anyString(),
            captor.capture(),
            any(LocalDate.class),
            anyString()
        );

        assertEquals(expectedGetRequest.getEndDate(), captor.getValue());
    }

    @Test
    void shouldCallGetRekoStatusServiceWithCorrectStartDate() {
        final var captor = ArgumentCaptor.forClass(LocalDate.class);

        rekoController.getRekoStatus(expectedGetRequest);
        verify(getRekoStatusService).get(
            anyString(),
            any(LocalDate.class),
            captor.capture(),
            anyString()
        );

        assertEquals(expectedGetRequest.getStartDate(), captor.getValue());
    }

    @Test
    void shouldCallGetRekoStatusServiceWithCorrectCareUnitId() {
        final var captor = ArgumentCaptor.forClass(String.class);

        rekoController.getRekoStatus(expectedGetRequest);
        verify(getRekoStatusService).get(
            anyString(),
            any(LocalDate.class),
            any(LocalDate.class),
            captor.capture()
        );

        assertEquals(expectedGetRequest.getCareUnitId(), captor.getValue());
    }
}

