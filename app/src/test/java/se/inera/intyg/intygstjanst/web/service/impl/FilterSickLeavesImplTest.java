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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKapitel;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKod;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.infra.sjukfall.dto.Patient;
import se.inera.intyg.infra.sjukfall.dto.RekoStatusDTO;
import se.inera.intyg.infra.sjukfall.dto.RekoStatusTypeDTO;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.dto.Vardenhet;
import se.inera.intyg.intygstjanst.web.service.CalculatePatientAgeService;
import se.inera.intyg.intygstjanst.web.service.DiagnosisChapterService;
import se.inera.intyg.intygstjanst.web.service.TextSearchFilterService;
import se.inera.intyg.intygstjanst.web.service.dto.OccupationType;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusType;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveLengthInterval;

@ExtendWith(MockitoExtension.class)
class FilterSickLeavesImplTest {

    @Mock
    private DiagnosisChapterService diagnosisChapterService;
    @Mock
    private CalculatePatientAgeService calculatePatientAgeService;
    @Mock
    private TextSearchFilterService textSearchFilterService;

    @InjectMocks
    private FilterSickLeavesImpl filterSickLeaves;

    private static final String DOCTOR_ID = "doctorId";
    private static final String UNIT_ID = "unitId";
    private static final String DIAGNOSIS_CODE = "A01";
    private static final String ANOTHER_DIAGNOSIS_CODE = "C10";
    private static final String DIAGNOSIS_CHAPTER = "A00-B99Vissa infektionssjukdomar och parasitsjukdomar";
    private static final String ANOTHER_DIAGNOSIS_CHAPTER = "C00-D48Tumörer";
    private static final String PATIENT_ID = "19121212-1212";
    private static final String ANOTHER_PATIENT_ID = "20121212-1212";
    private static final String THIRD_PATIENT_ID = "20121212-1213";

    private static final LocalDate FROM_END_DATE = LocalDate.now();
    private static final LocalDate TO_END_DATE = FROM_END_DATE.plusDays(10);

    @Nested
    class FilterOnSickLeaveLength {

        @BeforeEach
        void setUp() {
            doReturn(true).when(textSearchFilterService).filter(any(), any());
        }

        @Test
        void shouldFilterOnSickLeaveLengthIfSpecified() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 36, PATIENT_ID, Collections.emptyList());
            final var expectedSickLeaveList = List.of(expectedSickLeave);
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 42, PATIENT_ID, Collections.emptyList());
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves,
                List.of(new SickLeaveLengthInterval(6, 12), new SickLeaveLengthInterval(12, 40), new SickLeaveLengthInterval(50, 100)),
                Collections.emptyList(), null, null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldFilterOnSickLeaveLengthIfFromIsNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 36, PATIENT_ID, Collections.emptyList());
            final var expectedSickLeaveList = List.of(expectedSickLeave);
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 42, PATIENT_ID, Collections.emptyList());
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves,
                List.of(new SickLeaveLengthInterval(6, 12), new SickLeaveLengthInterval(null, 40), new SickLeaveLengthInterval(50, 100)),
                Collections.emptyList(), null, null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldFilterOnSickLeaveLengthIfToIsNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 36, PATIENT_ID, Collections.emptyList());
            final var expectedSickLeaveList = List.of(expectedSickLeave);
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 26, PATIENT_ID, Collections.emptyList());
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves,
                List.of(new SickLeaveLengthInterval(6, 12), new SickLeaveLengthInterval(35, null), new SickLeaveLengthInterval(50, 100)),
                Collections.emptyList(), null, null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnSickLeaveLengthIfFromAndToIsNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 36, PATIENT_ID, Collections.emptyList());
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 26, PATIENT_ID, Collections.emptyList());
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves,
                List.of(new SickLeaveLengthInterval(6, 12), new SickLeaveLengthInterval(null, null), new SickLeaveLengthInterval(50, 100)),
                Collections.emptyList(), null, null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnSickLeaveLengthIfNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID, Collections.emptyList());
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, Collections.emptyList());
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null,
                Collections.emptyList(), null, null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnSickLeaveLengthIfEmpty() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID, Collections.emptyList());
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, Collections.emptyList());
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, Collections.emptyList(),
                Collections.emptyList(), null, null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }
    }

    @Nested
    class FilterOnDiagnosisChapter {

        @BeforeEach
        void setUp() {
            doReturn(true).when(textSearchFilterService).filter(any(), any());
        }

        @Test
        void shouldFilterOnDiagnosisChapterIfSpecified() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, Collections.emptyList());
            final var expectedSickLeaveList = List.of(expectedSickLeave);

            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12, PATIENT_ID, Collections.emptyList());
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            doReturn(new DiagnosKapitel(DIAGNOSIS_CHAPTER))
                .when(diagnosisChapterService)
                .getDiagnosisChaptersFromSickLeave(expectedSickLeave);

            doReturn(new DiagnosKapitel(ANOTHER_DIAGNOSIS_CHAPTER))
                .when(diagnosisChapterService)
                .getDiagnosisChaptersFromSickLeave(anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null,
                List.of(new DiagnosKapitel(DIAGNOSIS_CHAPTER)),
                null, null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnDiagnosisChapterIfNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID, Collections.emptyList());
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 5, PATIENT_ID, Collections.emptyList());
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null,
                null, null, null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnDiagnosisChapterIfEmpty() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID, Collections.emptyList());
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 5, PATIENT_ID, Collections.emptyList());
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null,
                null, null, null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }
    }

    @Nested
    class FilterOnPatientAge {

        @BeforeEach
        void setUp() {
            doReturn(true).when(textSearchFilterService).filter(any(), any());
        }

        @Test
        void shouldFilterOnPatientAgeIfSpecified() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, Collections.emptyList());
            final var expectedSickLeaveList = List.of(expectedSickLeave);

            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12, ANOTHER_PATIENT_ID, Collections.emptyList());
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            doReturn(50)
                .when(calculatePatientAgeService)
                .get(expectedSickLeave.getPatient().getId());

            doReturn(20)
                .when(calculatePatientAgeService)
                .get(anotherSickLeave.getPatient().getId());

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                30, 60, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnPatientAgeIfFromIsNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID, Collections.emptyList());
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 5, ANOTHER_PATIENT_ID, Collections.emptyList());
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, null,
                null, 50, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnPatientAgeIfToIsNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID, Collections.emptyList());
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 5, ANOTHER_PATIENT_ID, Collections.emptyList());
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, null,
                50, null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }
    }

    @Nested
    class FilterOnSickLeaveEndDate {

        @BeforeEach
        void setUp() {
            doReturn(true).when(textSearchFilterService).filter(any(), any());
        }

        @Test
        void shouldNotFilterOnFromDateIfNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, Collections.emptyList());
            expectedSickLeave.setSlut(FROM_END_DATE.minusDays(1));
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12, ANOTHER_PATIENT_ID, Collections.emptyList());
            anotherSickLeave.setSlut(FROM_END_DATE.minusDays(5));
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                null, null, null, TO_END_DATE, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

            assertEquals(sickLeaves, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnToDateIfNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, Collections.emptyList());
            expectedSickLeave.setSlut(TO_END_DATE.plusDays(1));
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12, ANOTHER_PATIENT_ID, Collections.emptyList());
            anotherSickLeave.setSlut(TO_END_DATE.plusDays(5));
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                null, null, FROM_END_DATE, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

            assertEquals(sickLeaves, actualSickLeaveList);
        }

        @Test
        void shouldFilterOnDateIfValue() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, Collections.emptyList());
            expectedSickLeave.setSlut(FROM_END_DATE.plusDays(5));
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12, ANOTHER_PATIENT_ID, Collections.emptyList());
            anotherSickLeave.setSlut(FROM_END_DATE.minusDays(5));
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                null, null, FROM_END_DATE, TO_END_DATE, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

            assertEquals(expectedSickLeave, actualSickLeaveList.get(0));
        }

        @Test
        void shouldFilterOnFromDateIfEquals() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, Collections.emptyList());
            expectedSickLeave.setSlut(FROM_END_DATE);
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12, ANOTHER_PATIENT_ID, Collections.emptyList());
            anotherSickLeave.setSlut(FROM_END_DATE.minusDays(1));
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                null, null, FROM_END_DATE, TO_END_DATE, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

            assertEquals(expectedSickLeave, actualSickLeaveList.get(0));
        }

        @Test
        void shouldFilterOnToDateIfEquals() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, Collections.emptyList());
            expectedSickLeave.setSlut(FROM_END_DATE.plusDays(10));
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12, ANOTHER_PATIENT_ID, Collections.emptyList());
            anotherSickLeave.setSlut(FROM_END_DATE.plusDays(11));
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                null, null, FROM_END_DATE, TO_END_DATE, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

            assertEquals(expectedSickLeave, actualSickLeaveList.get(0));
        }
    }

    @Nested
    class FilterOnDoctorIds {

        List<SjukfallEnhet> sickLeaves;
        Lakare doctor1;
        Lakare doctor2;
        SjukfallEnhet expectedSickLeave;
        SjukfallEnhet anotherSickLeave;

        @BeforeEach
        void setup() {
            doctor1 = new Lakare();
            doctor2 = new Lakare();
            doctor1.setId("ID1");
            doctor2.setId("ID2");

            expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, Collections.emptyList());
            expectedSickLeave.setLakare(doctor1);
            anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12, ANOTHER_PATIENT_ID, Collections.emptyList());
            anotherSickLeave.setLakare(doctor2);
            sickLeaves = List.of(expectedSickLeave, anotherSickLeave);
            doReturn(true).when(textSearchFilterService).filter(any(), any());
        }

        @Test
        void shouldNotFilterOnDoctorIdsIfNull() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                null, null, null, TO_END_DATE, null, Collections.emptyList(), Collections.emptyList(), null);

            assertEquals(sickLeaves, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnDoctorIdsIfEmpty() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                null, null, null, TO_END_DATE, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

            assertEquals(sickLeaves, actualSickLeaveList);
        }

        @Test
        void shouldFilterDoctorIdsExcludedInFilter() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                null, null, null, TO_END_DATE, Collections.singletonList(doctor1.getId()), Collections.emptyList(),
                Collections.emptyList(), null);

            assertEquals(1, actualSickLeaveList.size());
            assertEquals(expectedSickLeave, actualSickLeaveList.get(0));
        }

        @Test
        void shouldNotFilterDoctorIdsIncludedInFilter() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                null, null, null, TO_END_DATE, List.of(doctor1.getId(), doctor2.getId()), Collections.emptyList(), Collections.emptyList(),
                null);

            assertEquals(sickLeaves, actualSickLeaveList);
        }
    }

    @Nested
    class FilterOnRekoStatus {

        List<SjukfallEnhet> sickLeaves;
        RekoStatusDTO rekoStatus1;
        RekoStatusDTO rekoStatus2;
        SjukfallEnhet expectedSickLeave;
        SjukfallEnhet anotherSickLeave;
        SjukfallEnhet sickLeaveWithNoRekoStatus;

        @BeforeEach
        void setup() {
            rekoStatus1 = new RekoStatusDTO();
            rekoStatus1.setStatus(new RekoStatusTypeDTO(RekoStatusType.REKO_3.toString(), "Reko"));
            rekoStatus2 = new RekoStatusDTO();
            rekoStatus2.setStatus(new RekoStatusTypeDTO(RekoStatusType.REKO_2.toString(), "Reko"));
            expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, Collections.emptyList());
            expectedSickLeave.setRekoStatus(rekoStatus1);
            anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12, ANOTHER_PATIENT_ID, Collections.emptyList());
            anotherSickLeave.setRekoStatus(rekoStatus2);
            sickLeaveWithNoRekoStatus = createSjukFallEnhet(DIAGNOSIS_CODE, 5, THIRD_PATIENT_ID, Collections.emptyList());
            sickLeaves = List.of(expectedSickLeave, anotherSickLeave, sickLeaveWithNoRekoStatus);
            doReturn(true).when(textSearchFilterService).filter(any(), any());
        }

        @Test
        void shouldNotFilterOnRekoStatusIfNull() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                null, null, null, TO_END_DATE, Collections.emptyList(), null, Collections.emptyList(), null);

            assertEquals(sickLeaves, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnRekoStatusIfEmpty() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                null, null, null, TO_END_DATE, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

            assertEquals(sickLeaves, actualSickLeaveList);
        }

        @Test
        void shouldFilterRekoStatusExcludedInFilter() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                null, null, null, TO_END_DATE, Collections.emptyList(),
                Collections.singletonList(RekoStatusType.REKO_3.toString()), Collections.emptyList(), null);

            assertEquals(1, actualSickLeaveList.size());
            assertEquals(expectedSickLeave, actualSickLeaveList.get(0));
        }

        @Test
        void shouldNotFilterRekoStatusIncludedInFilter() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                null, null, null, TO_END_DATE, Collections.emptyList(),
                List.of(RekoStatusType.REKO_1.toString(), RekoStatusType.REKO_2.toString(), RekoStatusType.REKO_3.toString()),
                Collections.emptyList(), null);

            assertEquals(sickLeaves, actualSickLeaveList);
        }

        @Test
        void shouldFilterSickLeaveWithNoRekoStatusAsRekoStatus1() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                null, null, null, TO_END_DATE, Collections.emptyList(),
                List.of(RekoStatusType.REKO_3.toString(), RekoStatusType.REKO_2.toString()), Collections.emptyList(), null);

            assertEquals(2, actualSickLeaveList.size());
            assertEquals(expectedSickLeave, actualSickLeaveList.get(0));
            assertEquals(anotherSickLeave, actualSickLeaveList.get(1));
        }

        @Test
        void shouldFilterAllSickLeavesButSickLeaveWithoutRekoStatus() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                null, null, null, TO_END_DATE, Collections.emptyList(),
                Collections.singletonList(RekoStatusType.REKO_1.toString()), Collections.emptyList(), null);

            assertEquals(1, actualSickLeaveList.size());
            assertEquals(sickLeaveWithNoRekoStatus, actualSickLeaveList.get(0));
        }
    }

    @Nested
    class FilterOnOccupationIds {

        List<SjukfallEnhet> sickLeaves;
        SjukfallEnhet expectedSickLeave;
        SjukfallEnhet anotherSickLeave;
        static final String OCCUPATION_NUVARANDE_ARBETE = "Nuvarande arbete";
        static final String OCCUPATION_FORALDRALEDIGHET = "Föräldraledighet för vård av barn";
        static final String OCCUPATION_STUDIER = "Studier";
        static final String OCCUPATION_ARBETSSOKANDE = "Arbetssökande";

        @BeforeEach
        void setUp() {
            doReturn(true).when(textSearchFilterService).filter(any(), any());
        }

        @Test
        void shouldNotFilterOnOccupationIdsIfNull() {
            sickLeaves = List.of(
                createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, List.of(OCCUPATION_STUDIER, OCCUPATION_FORALDRALEDIGHET)),
                createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, List.of(OCCUPATION_NUVARANDE_ARBETE, OCCUPATION_ARBETSSOKANDE)));
            final var filteredSickLeaves = filterSickLeaves.filter(sickLeaves, null, null, null, null, null, null, null, null, null,
                null);
            assertEquals(sickLeaves, filteredSickLeaves);
        }

        @Test
        void shouldNotFilterOnOccupationIdsIfEmpty() {
            sickLeaves = List.of(
                createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, List.of(OCCUPATION_STUDIER, OCCUPATION_FORALDRALEDIGHET)),
                createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, List.of(OCCUPATION_NUVARANDE_ARBETE, OCCUPATION_ARBETSSOKANDE)));
            final var filteredSickLeaves = filterSickLeaves.filter(sickLeaves, null, null, null, null, null, null, null, null,
                Collections.emptyList(), null);
            assertEquals(sickLeaves, filteredSickLeaves);
        }

        @Test
        void shouldFilterOnNuvarandeArbete() {
            expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID,
                List.of(OCCUPATION_STUDIER, OCCUPATION_FORALDRALEDIGHET, OCCUPATION_ARBETSSOKANDE));
            sickLeaves = List.of(
                expectedSickLeave,
                createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, List.of(OCCUPATION_NUVARANDE_ARBETE)));
            final var filteredSickLeaves = filterSickLeaves.filter(sickLeaves, null, null, null, null, null, null, null, null,
                List.of(OccupationType.NUVARANDE_ARBETE.toString()), null);
            assertEquals(List.of(expectedSickLeave), filteredSickLeaves);
        }

        @Test
        void shouldFilterOnArbetssokande() {
            expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID,
                List.of(OCCUPATION_STUDIER, OCCUPATION_FORALDRALEDIGHET, OCCUPATION_NUVARANDE_ARBETE));
            sickLeaves = List.of(
                expectedSickLeave,
                createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, List.of(OCCUPATION_ARBETSSOKANDE)));
            final var filteredSickLeaves = filterSickLeaves.filter(sickLeaves, null, null, null, null, null, null, null, null,
                List.of(OccupationType.ARBETSSOKANDE.toString()), null);
            assertEquals(List.of(expectedSickLeave), filteredSickLeaves);
        }

        @Test
        void shouldFilterOnForaldraledighet() {
            expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID,
                List.of(OCCUPATION_STUDIER, OCCUPATION_ARBETSSOKANDE, OCCUPATION_NUVARANDE_ARBETE));
            sickLeaves = List.of(
                expectedSickLeave,
                createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, List.of(OCCUPATION_FORALDRALEDIGHET)));
            final var filteredSickLeaves = filterSickLeaves.filter(sickLeaves, null, null, null, null, null, null, null, null,
                List.of(OccupationType.FORALDRALEDIG.toString()), null);
            assertEquals(List.of(expectedSickLeave), filteredSickLeaves);
        }

        @Test
        void shouldFilterOnStudier() {
            expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID,
                List.of(OCCUPATION_FORALDRALEDIGHET, OCCUPATION_ARBETSSOKANDE, OCCUPATION_NUVARANDE_ARBETE));
            sickLeaves = List.of(
                expectedSickLeave,
                createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID, List.of(OCCUPATION_STUDIER)));
            final var filteredSickLeaves = filterSickLeaves.filter(sickLeaves, null, null, null, null, null, null, null, null,
                List.of(OccupationType.STUDIER.toString()), null);
            assertEquals(List.of(expectedSickLeave), filteredSickLeaves);
        }

        @Test
        void shouldFilterOnMultipleValues() {
            expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID,
                List.of(OCCUPATION_FORALDRALEDIGHET));
            sickLeaves = List.of(
                expectedSickLeave,
                createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID,
                    List.of(OCCUPATION_STUDIER)));
            final var filteredSickLeaves = filterSickLeaves.filter(sickLeaves, null, null, null, null, null, null, null, null,
                List.of(
                    OccupationType.STUDIER.toString(),
                    OccupationType.NUVARANDE_ARBETE.toString(),
                    OccupationType.ARBETSSOKANDE.toString()),
                null);
            assertEquals(List.of(expectedSickLeave), filteredSickLeaves);
        }
    }

    @Nested
    class TextSearch {

        private static final String TEXT_SEARCH = "textSearch";


        @Test
        void shouldFilterOnTextSearch() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 36, PATIENT_ID, Collections.emptyList());
            final var expectedSickLeaveList = List.of(expectedSickLeave);
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 42, PATIENT_ID, Collections.emptyList());
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            doReturn(true).when(textSearchFilterService).filter(expectedSickLeave, TEXT_SEARCH);
            doReturn(false).when(textSearchFilterService).filter(anotherSickLeave, TEXT_SEARCH);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves,
                Collections.emptyList(), Collections.emptyList(), null, null, null, null, Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), TEXT_SEARCH);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnTextSearch() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 36, PATIENT_ID, Collections.emptyList());
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 42, PATIENT_ID, Collections.emptyList());
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            doReturn(true).when(textSearchFilterService).filter(expectedSickLeave, TEXT_SEARCH);
            doReturn(true).when(textSearchFilterService).filter(anotherSickLeave, TEXT_SEARCH);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves,
                Collections.emptyList(), Collections.emptyList(), null, null, null, null, Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), TEXT_SEARCH);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldCallTextSearchFilterService() {
            final var sickLeaveUnit = new SjukfallEnhet();
            final var sickLeaveUnits = List.of(sickLeaveUnit);
            filterSickLeaves.filter(sickLeaveUnits,
                Collections.emptyList(), Collections.emptyList(), null, null, null, null, Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), TEXT_SEARCH);
            verify(textSearchFilterService).filter(sickLeaveUnit, TEXT_SEARCH);
        }
    }


    private static SjukfallEnhet createSjukFallEnhet(String diagnosisCode, Integer dagar, String patientId, List<String> occupations) {
        SjukfallEnhet sickLeaveUnit = new SjukfallEnhet();
        Lakare lakare = Lakare.create(DOCTOR_ID, null);
        sickLeaveUnit.setLakare(lakare);
        sickLeaveUnit.setVardenhet(
            Vardenhet.create(UNIT_ID, null)
        );
        sickLeaveUnit.setDagar(dagar != null ? dagar : 0);
        sickLeaveUnit.setDiagnosKod(DiagnosKod.create(diagnosisCode));
        sickLeaveUnit.setPatient(Patient.create(patientId, null));
        sickLeaveUnit.setSlut(LocalDate.now());
        sickLeaveUnit.setSysselsattning(occupations);

        return sickLeaveUnit;
    }
}
