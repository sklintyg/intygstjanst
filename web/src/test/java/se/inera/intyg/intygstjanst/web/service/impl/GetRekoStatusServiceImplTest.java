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
import se.inera.intyg.intygstjanst.web.service.RekoStatusConverter;
import se.inera.intyg.intygstjanst.web.service.RekoStatusFilter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetRekoStatusServiceImplTest {

    private static final String PATIENT_ID = "PATIENT_ID";
    private static final LocalDate END_DATE = LocalDate.now();
    private static final LocalDate START_DATE = LocalDate.now();
    private static final List<Reko> REKO_STATUSES = Collections.emptyList();
    private static final Optional<Reko> FILTERED_REKO = Optional.of(new Reko());
    private static final String CARE_UNIT_ID = "Care-unit-id";

    @Mock
    private RekoRepository rekoRepository;
    @Mock
    private RekoStatusConverter rekoStatusConverter;
    @Mock
    private RekoStatusFilter rekoStatusFilter;
    @InjectMocks
    private GetRekoStatusServiceImpl getRekoStatusService;

    @Nested
    class TestRekoRepository {
        @Test
        void shouldCallRepositoryWithPatientId() {
            getRekoStatusService.get(PATIENT_ID, END_DATE, START_DATE, CARE_UNIT_ID);

            final var captor = ArgumentCaptor.forClass(String.class);

            verify(rekoRepository).findByPatientIdAndCareUnitId(captor.capture(), anyString());
            assertEquals(PATIENT_ID, captor.getValue());
        }

        @Test
        void shouldCallRepositoryWithCareUnitId() {
            getRekoStatusService.get(PATIENT_ID, END_DATE, START_DATE, CARE_UNIT_ID);

            final var captor = ArgumentCaptor.forClass(String.class);

            verify(rekoRepository).findByPatientIdAndCareUnitId(anyString(), captor.capture());
            assertEquals(CARE_UNIT_ID, captor.getValue());
        }
    }

    @Nested
    class TestRekoStatusFilter {
        @BeforeEach
        void setup() {
            when(rekoRepository.findByPatientIdAndCareUnitId(anyString(), anyString()))
                .thenReturn(REKO_STATUSES);
        }

        @Test
        void shouldCallFilterWithRekoStatuses() {
            getRekoStatusService.get(PATIENT_ID, END_DATE, START_DATE, CARE_UNIT_ID);

            final var captor = ArgumentCaptor.forClass(List.class);
            verify(rekoStatusFilter).filter(captor.capture(), any(), any(), any());

            assertEquals(REKO_STATUSES, captor.getValue());
        }

        @Test
        void shouldCallFilterWithPatientId() {
            getRekoStatusService.get(PATIENT_ID, END_DATE, START_DATE, CARE_UNIT_ID);

            final var captor = ArgumentCaptor.forClass(String.class);
            verify(rekoStatusFilter).filter(any(), captor.capture(), any(), any());

            assertEquals(PATIENT_ID, captor.getValue());
        }

        @Test
        void shouldCallFilterWithEndDate() {
            getRekoStatusService.get(PATIENT_ID, END_DATE, START_DATE, CARE_UNIT_ID);

            final var captor = ArgumentCaptor.forClass(LocalDate.class);
            verify(rekoStatusFilter).filter(any(), any(), captor.capture(), any());

            assertEquals(END_DATE, captor.getValue());
        }

        @Test
        void shouldCallFilterWithStartDate() {
            getRekoStatusService.get(PATIENT_ID, END_DATE, START_DATE, CARE_UNIT_ID);

            final var captor = ArgumentCaptor.forClass(LocalDate.class);
            verify(rekoStatusFilter).filter(any(), any(), any(), captor.capture());

            assertEquals(START_DATE, captor.getValue());
        }
    }

    @Nested
    class TestRekoStatusConverter {

        @BeforeEach
        void setup() {
            when(rekoRepository.findByPatientIdAndCareUnitId(anyString(), anyString()))
                .thenReturn(REKO_STATUSES);
        }

        @Test
        void shouldCallConverterWithRekoReturnedFromFilter() {
            when(rekoStatusFilter.filter(anyList(), anyString(), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(FILTERED_REKO);
            getRekoStatusService.get(PATIENT_ID, END_DATE, START_DATE, CARE_UNIT_ID);

            final var captor = ArgumentCaptor.forClass(Reko.class);

            verify(rekoStatusConverter).convert(captor.capture());
            assertEquals(FILTERED_REKO.get(), captor.getValue());
        }

        @Test
        void shouldNotCallConverterIfFilteredRekoIsEmpty() {
            when(rekoStatusFilter.filter(anyList(), anyString(), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(Optional.empty());
            getRekoStatusService.get(PATIENT_ID, END_DATE, START_DATE, CARE_UNIT_ID);

            verify(rekoStatusConverter, times(0)).convert(any());
        }
    }

    @Nested
    class TestResponse {
        @Test
        void shouldReturnNullIfRekoStatusIsNotFound() {
            when(rekoStatusFilter.filter(any(), any(), any(), any())).thenReturn(Optional.empty());

            final var response = getRekoStatusService.get(PATIENT_ID, END_DATE, START_DATE, CARE_UNIT_ID);

            assertNull(response);
        }

        @Test
        void shouldReturnStatusReturnedFromConverter() {
            final var expectedResponse = new RekoStatusDTO();
            when(rekoStatusFilter.filter(any(), any(), any(), any())).thenReturn(Optional.of(new Reko()));
            when(rekoStatusConverter.convert(any())).thenReturn(expectedResponse);

            final var response = getRekoStatusService.get(PATIENT_ID, END_DATE, START_DATE, CARE_UNIT_ID);

            assertEquals(expectedResponse, response);
        }
    }
}

