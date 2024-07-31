/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKapitel;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.inera.intyg.intygstjanst.web.service.CreateSickLeaveFilter;
import se.inera.intyg.intygstjanst.web.service.GetActiveSickLeaveCertificates;
import se.inera.intyg.intygstjanst.web.service.PuFilterService;
import se.inera.intyg.intygstjanst.web.service.dto.GetSickLeaveFilterServiceRequest;
import se.inera.intyg.intygstjanst.web.service.dto.GetSickLeaveFilterServiceResponse;

@ExtendWith(MockitoExtension.class)
class GetSickLeaveFilterServiceImplTest {

    @Mock
    private HsaService hsaService;
    @Mock
    private GetActiveSickLeaveCertificates getActiveSickLeaveCertificates;
    @Mock
    private CreateSickLeaveFilter createSickLeaveFilter;
    @Mock
    private PuFilterService puFilterService;
    @InjectMocks
    private GetSickLeaveFilterServiceImpl getSickLeaveFilterService;

    private static final String CARE_UNIT_ID = "CareUnitId";
    private static final String CARE_PROVIDER_ID = "CareProviderId";
    private static final String UNIT_ID = "UnitId1";
    private static final List<String> UNIT_IDS = List.of("CareUnitId", "UnitId1", "UnitId2");
    private static final String DOCTOR_ID = "DoctorId1";
    private static final List<String> DOCTOR_IDS = List.of("DoctorId1");
    private static final Integer MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED = 3;
    private static final String DIAGNOSIS_CHAPTER = "A00-B99Vissa infektionssjukdomar och parasitsjukdomar";
    private static final List<DiagnosKapitel> DIAGNOSIS_CHAPTERS = List.of(new DiagnosKapitel(DIAGNOSIS_CHAPTER));
    private static final String FILTER_PROTECTED_PERSON = "ID";
    private GetSickLeaveFilterServiceRequest.GetSickLeaveFilterServiceRequestBuilder getSickLeaveFilterServiceRequestBuilder;

    @BeforeEach
    void setUp() {
        getSickLeaveFilterServiceRequestBuilder = GetSickLeaveFilterServiceRequest.builder()
            .careUnitId(CARE_UNIT_ID)
            .doctorId(DOCTOR_ID)
            .protectedPersonFilterId(FILTER_PROTECTED_PERSON)
            .maxDaysSinceSickLeaveCompleted(MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);

        doReturn(CARE_PROVIDER_ID)
            .when(hsaService)
            .getHsaIdForVardgivare(CARE_UNIT_ID);

        doReturn(UNIT_IDS)
            .when(hsaService)
            .getHsaIdsForCareUnitAndSubUnits(CARE_UNIT_ID);
    }

    @Nested
    class GetActiveSickLeaveCertificatesTest {

        @Test
        void shallIncludeCareProviderId() {
            final var careProviderIdCaptor = ArgumentCaptor.forClass(String.class);
            getSickLeaveFilterService.get(getSickLeaveFilterServiceRequestBuilder.build());
            verify(getActiveSickLeaveCertificates).get(careProviderIdCaptor.capture(), anyList(), anyList(), anyInt());
            assertEquals(CARE_PROVIDER_ID, careProviderIdCaptor.getValue());
        }

        @Test
        void shallIncludeCareUnitsAndSubUnits() {
            final var unitIdsCaptor = ArgumentCaptor.forClass(List.class);
            getSickLeaveFilterService.get(getSickLeaveFilterServiceRequestBuilder.build());
            verify(getActiveSickLeaveCertificates).get(anyString(), unitIdsCaptor.capture(), anyList(), anyInt());
            assertEquals(UNIT_IDS, unitIdsCaptor.getValue());
        }

        @Test
        void shallIncludeUnitIdIfProvided() {
            final var unitIdsCaptor = ArgumentCaptor.forClass(List.class);
            getSickLeaveFilterService.get(getSickLeaveFilterServiceRequestBuilder.unitId(UNIT_ID).build());
            verify(getActiveSickLeaveCertificates).get(anyString(), unitIdsCaptor.capture(), anyList(), anyInt());
            assertEquals(List.of(UNIT_ID), unitIdsCaptor.getValue());
        }

        @Test
        void shallIncludeDoctorIds() {
            final var doctorIdsCaptor = ArgumentCaptor.forClass(List.class);
            getSickLeaveFilterService.get(getSickLeaveFilterServiceRequestBuilder.build());
            verify(getActiveSickLeaveCertificates).get(anyString(), anyList(), doctorIdsCaptor.capture(), anyInt());
            assertEquals(DOCTOR_IDS, doctorIdsCaptor.getValue());
        }

        @Test
        void shallExcludeDoctorIds() {
            final var doctorIdsCaptor = ArgumentCaptor.forClass(List.class);
            getSickLeaveFilterService.get(getSickLeaveFilterServiceRequestBuilder.doctorId(null).build());
            verify(getActiveSickLeaveCertificates).get(anyString(), anyList(), doctorIdsCaptor.capture(), anyInt());
            assertNull(doctorIdsCaptor.getValue());
        }

        @Test
        void shallIncludeMaxDaysSinceSickLeaveCompleted() {
            final var maxDaysSinceSickLeaveCompletedCaptor = ArgumentCaptor.forClass(Integer.class);
            getSickLeaveFilterService.get(getSickLeaveFilterServiceRequestBuilder.build());
            verify(getActiveSickLeaveCertificates).get(anyString(), anyList(), anyList(), maxDaysSinceSickLeaveCompletedCaptor.capture());
            assertEquals(MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, maxDaysSinceSickLeaveCompletedCaptor.getValue());
        }
    }

    @Test
    void shallReturnSickLeaveFilter() {
        final var expectedSickLeaveFilter = GetSickLeaveFilterServiceResponse.builder()
            .activeDoctors(
                List.of(
                    Lakare.create(DOCTOR_ID, DOCTOR_ID)
                )
            )
            .diagnosisChapters(DIAGNOSIS_CHAPTERS)
            .build();

        doReturn(expectedSickLeaveFilter)
            .when(createSickLeaveFilter)
            .create(anyList());

        final var actualSickLeaveFilter = getSickLeaveFilterService.get(getSickLeaveFilterServiceRequestBuilder.build());
        assertEquals(expectedSickLeaveFilter, actualSickLeaveFilter);
    }

    @Test
    void shallCallFilterPuService() {
        final var list = Collections.singletonList(new IntygData());
        when(getActiveSickLeaveCertificates.get(anyString(), anyList(), anyList(), anyInt())).thenReturn(list);

        getSickLeaveFilterService.get(getSickLeaveFilterServiceRequestBuilder.build());

        verify(puFilterService).enrichWithPatientNameAndFilter(anyList(), anyString());
    }

    @Test
    void shallCallPuFilterServiceWithListOfIntygData() {
        final var captor = ArgumentCaptor.forClass(List.class);
        final var list = Collections.singletonList(new IntygData());
        when(getActiveSickLeaveCertificates.get(anyString(), anyList(), anyList(), anyInt())).thenReturn(list);

        getSickLeaveFilterService.get(getSickLeaveFilterServiceRequestBuilder.build());

        verify(puFilterService).enrichWithPatientNameAndFilter(captor.capture(), anyString());
        assertEquals(list, captor.getValue());
    }

    @Test
    void shallCallPuFilterServiceWithFilterOnProtectedPerson() {
        final var captor = ArgumentCaptor.forClass(String.class);
        final var list = Collections.singletonList(new IntygData());
        when(getActiveSickLeaveCertificates.get(anyString(), anyList(), anyList(), anyInt())).thenReturn(list);

        getSickLeaveFilterService.get(getSickLeaveFilterServiceRequestBuilder.build());

        verify(puFilterService).enrichWithPatientNameAndFilter(anyList(), captor.capture());
        assertEquals(FILTER_PROTECTED_PERSON, captor.getValue());
    }
}
