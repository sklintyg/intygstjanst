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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
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
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.inera.intyg.intygstjanst.web.service.FilterSickLeaves;
import se.inera.intyg.intygstjanst.web.service.GetActiveSickLeaveCertificates;
import se.inera.intyg.intygstjanst.web.service.GetSickLeaveCertificates;
import se.inera.intyg.intygstjanst.web.service.RekoStatusDecorator;
import se.inera.intyg.intygstjanst.web.service.dto.GetSickLeaveServiceRequest;
import se.inera.intyg.intygstjanst.web.service.dto.GetSickLeaveServiceRequest.GetSickLeaveServiceRequestBuilder;
import se.inera.intyg.intygstjanst.web.service.dto.OccupationType;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveLengthInterval;

@ExtendWith(MockitoExtension.class)
class GetSickLeavesServiceImplTest {

    @Mock
    private HsaService hsaService;

    @Mock
    private GetActiveSickLeaveCertificates getActiveSickLeaveCertificates;

    @Mock
    private GetSickLeaveCertificates getSickLeaveCertificates;

    @Mock
    private FilterSickLeaves filterSickLeaves;

    @Mock
    private RekoStatusDecorator rekoStatusDecorator;

    @InjectMocks
    private GetSickLeavesServiceImpl getSickLeavesService;

    private static final String CARE_UNIT_ID = "CareUnitId";
    private static final String CARE_PROVIDER_ID = "CareProviderId";
    private static final String UNIT_ID = "UnitId1";
    private static final List<String> UNIT_IDS = List.of("CareUnitId", "UnitId1", "UnitId2");
    private static final List<String> DOCTOR_IDS = List.of("DoctorId1", "DoctorId2");
    private static final List<String> REKO_STATUSES = List.of("RekoStatus1", "RekoStatus2");
    private static final List<String> PATIENT_IDS = List.of("PatientId1", "PatientId2");
    private static final Integer MAX_CERTIFICATE_GAP = 5;
    private static final Integer MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED = 3;
    private static final Integer FROM_PATIENT_AGE = 0;
    private static final Integer TO_PATIENT_AGE = 150;
    private static final LocalDate FROM_END_DATE = LocalDate.now();
    private static final LocalDate TO_END_DATE = FROM_END_DATE.plusDays(10);
    private static final String DIAGNOSIS_CHAPTER = "A00-B99Vissa infektionssjukdomar och parasitsjukdomar";
    private static final String PROTECTED_PERSON_FILTER_ID = "filterId";
    private static final List<DiagnosKapitel> DIAGNOSIS_CHAPTERS = List.of(new DiagnosKapitel(DIAGNOSIS_CHAPTER));
    private static final List<SjukfallEnhet> SICK_LEAVES = List.of(new SjukfallEnhet(), new SjukfallEnhet(), new SjukfallEnhet());
    private static final List<SjukfallEnhet> FILTERED_SICK_LEAVES = List.of(new SjukfallEnhet(), new SjukfallEnhet());
    private static final List<SickLeaveLengthInterval> SICK_LEAVE_LENGTH_INTERVALS = List.of(new SickLeaveLengthInterval(1, 365));
    private static final List<String> OCCUPATION_TYPE_IDS = List.of(OccupationType.ARBETSSOKANDE.toString());
    private static final String TEXT_SEARCH = "textSearch";

    private GetSickLeaveServiceRequestBuilder getSickLeaveServiceRequestBuilder;

    @BeforeEach
    void setUp() {
        getSickLeaveServiceRequestBuilder = GetSickLeaveServiceRequest.builder()
            .careUnitId(CARE_UNIT_ID)
            .doctorIds(DOCTOR_IDS)
            .sickLeaveLengthIntervals(SICK_LEAVE_LENGTH_INTERVALS)
            .maxCertificateGap(MAX_CERTIFICATE_GAP)
            .maxDaysSinceSickLeaveCompleted(MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED)
            .diagnosisChapters(DIAGNOSIS_CHAPTERS)
            .fromPatientAge(FROM_PATIENT_AGE)
            .protectedPersonFilterId(PROTECTED_PERSON_FILTER_ID)
            .fromSickLeaveEndDate(FROM_END_DATE)
            .toSickLeaveEndDate(TO_END_DATE)
            .toPatientAge(TO_PATIENT_AGE)
            .rekoStatusTypeIds(REKO_STATUSES)
            .occupationTypeIds(OCCUPATION_TYPE_IDS)
            .textSearch(TEXT_SEARCH);

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
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(getActiveSickLeaveCertificates).get(careProviderIdCaptor.capture(), anyList(), anyList(), anyInt());
            assertEquals(CARE_PROVIDER_ID, careProviderIdCaptor.getValue());
        }

        @Test
        void shallIncludeCareUnitsAndSubUnits() {
            final var unitIdsCaptor = ArgumentCaptor.forClass(List.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(getActiveSickLeaveCertificates).get(anyString(), unitIdsCaptor.capture(), anyList(), anyInt());
            assertEquals(UNIT_IDS, unitIdsCaptor.getValue());
        }

        @Test
        void shallIncludeUnitIdIfProvided() {
            final var unitIdsCaptor = ArgumentCaptor.forClass(List.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.unitId(UNIT_ID).build());
            verify(getActiveSickLeaveCertificates).get(anyString(), unitIdsCaptor.capture(), anyList(), anyInt());
            assertEquals(List.of(UNIT_ID), unitIdsCaptor.getValue());
        }

        @Test
        void shallIncludeDoctorIds() {
            final var doctorIdsCaptor = ArgumentCaptor.forClass(List.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(getActiveSickLeaveCertificates).get(anyString(), anyList(), doctorIdsCaptor.capture(), anyInt());
            assertEquals(DOCTOR_IDS, doctorIdsCaptor.getValue());
        }

        @Test
        void shallIncludeMaxDaysSinceSickLeaveCompleted() {
            final var maxDaysSinceSickLeaveCompletedCaptor = ArgumentCaptor.forClass(Integer.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(getActiveSickLeaveCertificates).get(anyString(), anyList(), anyList(), maxDaysSinceSickLeaveCompletedCaptor.capture());
            assertEquals(MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, maxDaysSinceSickLeaveCompletedCaptor.getValue());
        }
    }

    @Nested
    class GetSickLeavesTest {

        List<IntygData> intygDataList;

        @BeforeEach
        void setUp() {
            final var intygDataOne = new IntygData();
            intygDataOne.setPatientId("PatientId1");
            final var intygDataTwo = new IntygData();
            intygDataTwo.setPatientId("PatientId2");
            intygDataList = List.of(intygDataOne, intygDataTwo);

            doReturn(intygDataList)
                .when(getActiveSickLeaveCertificates)
                .get(CARE_PROVIDER_ID, UNIT_IDS, DOCTOR_IDS, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);
        }

        @Test
        void shallIncludeCareProviderId() {
            final var careProviderIdCaptor = ArgumentCaptor.forClass(String.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(getSickLeaveCertificates).get(careProviderIdCaptor.capture(), anyList(), anyList(), anyInt(), anyInt(), anyString());
            assertEquals(CARE_PROVIDER_ID, careProviderIdCaptor.getValue());
        }

        @Test
        void shallIncludeCareUnitsAndSubUnits() {
            final var unitIdsCaptor = ArgumentCaptor.forClass(List.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(getSickLeaveCertificates).get(anyString(), unitIdsCaptor.capture(), anyList(), anyInt(), anyInt(), anyString());
            assertEquals(UNIT_IDS, unitIdsCaptor.getValue());
        }

        @Test
        void shallIncludePatientIds() {
            final var patientIdsCaptor = ArgumentCaptor.forClass(List.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(getSickLeaveCertificates).get(anyString(), anyList(), patientIdsCaptor.capture(), anyInt(), anyInt(), anyString());
            assertEquals(PATIENT_IDS, patientIdsCaptor.getValue());
        }

        @Test
        void shallIncludeMaxCertificateGap() {
            final var maxCertificateGapCaptor = ArgumentCaptor.forClass(Integer.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(getSickLeaveCertificates).get(anyString(), anyList(), anyList(), maxCertificateGapCaptor.capture(),
                anyInt(), anyString());
            assertEquals(MAX_CERTIFICATE_GAP, maxCertificateGapCaptor.getValue());
        }

        @Test
        void shallIncludeMaxDaysSinceSickLeaveCompleted() {
            final var maxDaysSinceSickLeaveCompletedCaptor = ArgumentCaptor.forClass(Integer.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(getSickLeaveCertificates).get(anyString(), anyList(), anyList(), anyInt(),
                maxDaysSinceSickLeaveCompletedCaptor.capture(), anyString());
            assertEquals(MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, maxDaysSinceSickLeaveCompletedCaptor.getValue());
        }

        @Test
        void shallIncludeProtectedPersonFilterId() {
            final var captor = ArgumentCaptor.forClass(String.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(getSickLeaveCertificates).get(anyString(), anyList(), anyList(), anyInt(),
                anyInt(), captor.capture());
            assertEquals(PROTECTED_PERSON_FILTER_ID, captor.getValue());
        }
    }

    @Nested
    class FilterSickLeavesTest {

        @BeforeEach
        void setUp() {
            final var intygDataOne = new IntygData();
            intygDataOne.setPatientId("PatientId1");
            final var intygDataTwo = new IntygData();
            intygDataTwo.setPatientId("PatientId2");
            final var intygDataList = List.of(intygDataOne, intygDataTwo);

            doReturn(intygDataList)
                .when(getActiveSickLeaveCertificates)
                .get(CARE_PROVIDER_ID, UNIT_IDS, DOCTOR_IDS, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);

            doReturn(SICK_LEAVES)
                .when(getSickLeaveCertificates)
                .get(CARE_PROVIDER_ID,
                    UNIT_IDS,
                    PATIENT_IDS,
                    MAX_CERTIFICATE_GAP,
                    MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED,
                    PROTECTED_PERSON_FILTER_ID
                );
        }

        @Test
        void shallIncludeSickLeaves() {
            final var sickLeaveListCaptor = ArgumentCaptor.forClass(List.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(filterSickLeaves).filter(
                sickLeaveListCaptor.capture(), anyList(), anyList(), anyInt(), anyInt(),
                any(LocalDate.class), any(LocalDate.class), anyList(), anyList(), anyList(),
                anyString());
            assertEquals(SICK_LEAVES, sickLeaveListCaptor.getValue());
        }

        @Test
        void shallIncludeSickLeaveLengthIntervals() {
            final var sickLeaveLengthIntervalsCaptor = ArgumentCaptor.forClass(List.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(filterSickLeaves).filter(anyList(), sickLeaveLengthIntervalsCaptor.capture(),
                anyList(), anyInt(), anyInt(), any(LocalDate.class), any(LocalDate.class), anyList(), anyList(), anyList(),
                anyString());
            assertEquals(SICK_LEAVE_LENGTH_INTERVALS, sickLeaveLengthIntervalsCaptor.getValue());
        }

        @Test
        void shallIncludeDiagnosisChapter() {
            final var diagnosisChapterCaptor = ArgumentCaptor.forClass(List.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(filterSickLeaves).filter(anyList(), anyList(), diagnosisChapterCaptor.capture(),
                anyInt(), anyInt(), any(LocalDate.class), any(LocalDate.class), anyList(), anyList(), anyList(),
                anyString());
            assertEquals(DIAGNOSIS_CHAPTERS, diagnosisChapterCaptor.getValue());
        }

        @Test
        void shallIncludeFromPatientAge() {
            final var patientAge = ArgumentCaptor.forClass(Integer.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(filterSickLeaves).filter(anyList(), anyList(), anyList(), patientAge.capture(),
                anyInt(), any(LocalDate.class), any(LocalDate.class), anyList(), anyList(), anyList(),
                anyString());
            assertEquals(FROM_PATIENT_AGE, patientAge.getValue());
        }

        @Test
        void shallIncludeToPatientAge() {
            final var patientAge = ArgumentCaptor.forClass(Integer.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(filterSickLeaves).filter(anyList(), anyList(), anyList(), anyInt(),
                patientAge.capture(), any(LocalDate.class), any(LocalDate.class), anyList(), anyList(), anyList(),
                anyString());
            assertEquals(TO_PATIENT_AGE, patientAge.getValue());
        }

        @Test
        void shallIncludeFromSickLeaveEndDate() {
            final var captor = ArgumentCaptor.forClass(LocalDate.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(filterSickLeaves).filter(anyList(), anyList(), anyList(), anyInt(),
                anyInt(), captor.capture(), any(LocalDate.class), anyList(), anyList(), anyList(), anyString());
            assertEquals(FROM_END_DATE, captor.getValue());
        }

        @Test
        void shallIncludeToSickLeaveEndDate() {
            final var captor = ArgumentCaptor.forClass(LocalDate.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(filterSickLeaves).filter(anyList(), anyList(), anyList(),
                anyInt(), anyInt(), any(LocalDate.class), captor.capture(), anyList(), anyList(), anyList(),
                anyString());
            assertEquals(TO_END_DATE, captor.getValue());
        }

        @Test
        void shallIncludeDoctorIds() {
            final var captor = ArgumentCaptor.forClass(List.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(filterSickLeaves).filter(anyList(), anyList(), anyList(),
                anyInt(), anyInt(), any(LocalDate.class), any(LocalDate.class), captor.capture(), anyList(), anyList(),
                anyString());
            assertEquals(DOCTOR_IDS, captor.getValue());
        }

        @Test
        void shallIncludeRekoStatuses() {
            final var captor = ArgumentCaptor.forClass(List.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(filterSickLeaves).filter(anyList(), anyList(), anyList(),
                anyInt(), anyInt(), any(LocalDate.class), any(LocalDate.class), anyList(), captor.capture(), anyList(),
                anyString());
            assertEquals(REKO_STATUSES, captor.getValue());
        }

        @Test
        void shallIncludeOccupationTypeIds() {
            final var captor = ArgumentCaptor.forClass(List.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(filterSickLeaves).filter(anyList(), anyList(), anyList(),
                anyInt(), anyInt(), any(LocalDate.class), any(LocalDate.class), anyList(), anyList(), captor.capture(),
                anyString());
            assertEquals(OCCUPATION_TYPE_IDS, captor.getValue());
        }

        @Test
        void shallIncludeTextSearch() {
            final var captor = ArgumentCaptor.forClass(String.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(filterSickLeaves).filter(anyList(), anyList(), anyList(),
                anyInt(), anyInt(), any(LocalDate.class), any(LocalDate.class), anyList(), anyList(), anyList(),
                captor.capture());
            assertEquals(TEXT_SEARCH, captor.getValue());
        }
    }

    @Test
    void shallReturnSickLeavesIfActiveSickLeavesFound() {
        final var intygDataOne = new IntygData();
        intygDataOne.setPatientId("PatientId1");
        final var intygDataTwo = new IntygData();
        intygDataTwo.setPatientId("PatientId2");
        final var intygDataList = List.of(intygDataOne, intygDataTwo);

        doReturn(intygDataList)
            .when(getActiveSickLeaveCertificates)
            .get(CARE_PROVIDER_ID, UNIT_IDS, DOCTOR_IDS, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);

        doReturn(SICK_LEAVES)
            .when(getSickLeaveCertificates)
            .get(CARE_PROVIDER_ID,
                UNIT_IDS,
                PATIENT_IDS,
                MAX_CERTIFICATE_GAP,
                MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED,
                PROTECTED_PERSON_FILTER_ID
            );

        doReturn(FILTERED_SICK_LEAVES)
            .when(filterSickLeaves)
            .filter(SICK_LEAVES, SICK_LEAVE_LENGTH_INTERVALS, DIAGNOSIS_CHAPTERS, FROM_PATIENT_AGE,
                TO_PATIENT_AGE, FROM_END_DATE, TO_END_DATE, DOCTOR_IDS, REKO_STATUSES, OCCUPATION_TYPE_IDS, TEXT_SEARCH);

        final var actualSickLeaveList = getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());

        assertEquals(FILTERED_SICK_LEAVES, actualSickLeaveList);
    }

    @Test
    void shallReturnEmptySickLeavesIfNoActiveSickLeavesFound() {
        final var intygDataList = Collections.EMPTY_LIST;

        doReturn(intygDataList)
            .when(getActiveSickLeaveCertificates)
            .get(CARE_PROVIDER_ID, UNIT_IDS, DOCTOR_IDS, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);

        final var actualSickLeaveList = getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());

        assertEquals(Collections.emptyList(), actualSickLeaveList);
    }

    @Nested
    class TestRekoStatusDecorator {

        @BeforeEach
        void setUp() {
            final var intygDataOne = new IntygData();
            intygDataOne.setPatientId("PatientId1");
            final var intygDataTwo = new IntygData();
            intygDataTwo.setPatientId("PatientId2");
            final var intygDataList = List.of(intygDataOne, intygDataTwo);

            doReturn(intygDataList)
                .when(getActiveSickLeaveCertificates)
                .get(CARE_PROVIDER_ID, UNIT_IDS, DOCTOR_IDS, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);

            doReturn(SICK_LEAVES)
                .when(getSickLeaveCertificates)
                .get(CARE_PROVIDER_ID,
                    UNIT_IDS,
                    PATIENT_IDS,
                    MAX_CERTIFICATE_GAP,
                    MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED,
                    PROTECTED_PERSON_FILTER_ID
                );
        }


        @Test
        void shallDecorateWithRekoStatusAndIncludeSickLeaveList() {
            final var captor = ArgumentCaptor.forClass(List.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(rekoStatusDecorator).decorate(captor.capture(), anyString());
            assertEquals(SICK_LEAVES, captor.getValue());
        }

        @Test
        void shallDecorateWithRekoStatusAndIncludeCareProvider() {
            final var captor = ArgumentCaptor.forClass(String.class);
            getSickLeavesService.get(getSickLeaveServiceRequestBuilder.build());
            verify(rekoStatusDecorator).decorate(anyList(), captor.capture());
            assertEquals(CARE_UNIT_ID, captor.getValue());
        }
    }
}
