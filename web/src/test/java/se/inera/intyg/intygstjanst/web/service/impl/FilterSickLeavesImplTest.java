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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

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
import se.inera.intyg.infra.sjukfall.dto.*;
import se.inera.intyg.intygstjanst.web.service.CalculatePatientAgeService;
import se.inera.intyg.intygstjanst.web.service.DiagnosisChapterService;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusType;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveLengthInterval;

@ExtendWith(MockitoExtension.class)
class FilterSickLeavesImplTest {

    @Mock
    private DiagnosisChapterService diagnosisChapterService;
    @Mock
    private CalculatePatientAgeService calculatePatientAgeService;

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
    private static final LocalDate FROM_END_DATE = LocalDate.now();
    private static final LocalDate TO_END_DATE = FROM_END_DATE.plusDays(10);

    @Nested
    class FilterOnSickLeaveLength {

        @Test
        void shouldFilterOnSickLeaveLengthIfSpecified() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 36, PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave);
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 42, PATIENT_ID);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves,
                List.of(new SickLeaveLengthInterval(6, 12), new SickLeaveLengthInterval(12, 40), new SickLeaveLengthInterval(50, 100)),
                Collections.emptyList(), null, null, null, null, Collections.emptyList(), Collections.emptyList());

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldFilterOnSickLeaveLengthIfFromIsNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 36, PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave);
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 42, PATIENT_ID);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves,
                List.of(new SickLeaveLengthInterval(6, 12), new SickLeaveLengthInterval(null, 40), new SickLeaveLengthInterval(50, 100)),
                Collections.emptyList(), null, null, null, null, Collections.emptyList(), Collections.emptyList());

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldFilterOnSickLeaveLengthIfToIsNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 36, PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave);
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 26, PATIENT_ID);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves,
                List.of(new SickLeaveLengthInterval(6, 12), new SickLeaveLengthInterval(35, null), new SickLeaveLengthInterval(50, 100)),
                Collections.emptyList(), null, null, null, null, Collections.emptyList(), Collections.emptyList());

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnSickLeaveLengthIfFromAndToIsNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 36, PATIENT_ID);
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 26, PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves,
                List.of(new SickLeaveLengthInterval(6, 12), new SickLeaveLengthInterval(null, null), new SickLeaveLengthInterval(50, 100)),
                Collections.emptyList(), null, null, null, null, Collections.emptyList(), Collections.emptyList());

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnSickLeaveLengthIfNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID);
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null,
                    Collections.emptyList(), null, null, null, null, Collections.emptyList(), Collections.emptyList());

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnSickLeaveLengthIfEmpty() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID);
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, Collections.emptyList(),
                    Collections.emptyList(), null, null, null, null, Collections.emptyList(), Collections.emptyList());

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }
    }

    @Nested
    class FilterOnDiagnosisChapter {

        @Test
        void shouldFilterOnDiagnosisChapterIfSpecified() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave);

            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12, PATIENT_ID);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            doReturn(new DiagnosKapitel(DIAGNOSIS_CHAPTER))
                .when(diagnosisChapterService)
                .getDiagnosisChaptersFromSickLeave(expectedSickLeave);

            doReturn(new DiagnosKapitel(ANOTHER_DIAGNOSIS_CHAPTER))
                .when(diagnosisChapterService)
                .getDiagnosisChaptersFromSickLeave(anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null,
                List.of(new DiagnosKapitel(DIAGNOSIS_CHAPTER)),
                null, null, null, null, Collections.emptyList(), Collections.emptyList());

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnDiagnosisChapterIfNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID);
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 5, PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null,
                    null, null, null, null, null, Collections.emptyList(), Collections.emptyList());

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnDiagnosisChapterIfEmpty() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID);
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 5, PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null,
                    null, null, null, null, null, Collections.emptyList(), Collections.emptyList());

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }
    }

    @Nested
    class FilterOnPatientAge {

        @Test
        void shouldFilterOnPatientAgeIfSpecified() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave);

            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12, ANOTHER_PATIENT_ID);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            doReturn(50)
                .when(calculatePatientAgeService)
                .get(expectedSickLeave.getPatient().getId());

            doReturn(20)
                .when(calculatePatientAgeService)
                .get(anotherSickLeave.getPatient().getId());

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                30, 60, null, null, Collections.emptyList(), Collections.emptyList());

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnPatientAgeIfFromIsNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID);
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 5, ANOTHER_PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, null,
                    null, 50, null, null, Collections.emptyList(), Collections.emptyList());

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnPatientAgeIfToIsNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID);
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 5, ANOTHER_PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, null,
                    50, null, null, null, Collections.emptyList(), Collections.emptyList());

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }
    }

    @Nested
    class FilterOnSickLeaveEndDate {
        @Test
        void shouldNotFilterOnFromDateIfNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID);
            expectedSickLeave.setSlut(FROM_END_DATE.minusDays(1));
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12, ANOTHER_PATIENT_ID);
            anotherSickLeave.setSlut(FROM_END_DATE.minusDays(5));
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                    null, null, null, TO_END_DATE, Collections.emptyList(), Collections.emptyList());

            assertEquals(sickLeaves, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnToDateIfNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID);
            expectedSickLeave.setSlut(TO_END_DATE.plusDays(1));
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12, ANOTHER_PATIENT_ID);
            anotherSickLeave.setSlut(TO_END_DATE.plusDays(5));
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                    null, null, FROM_END_DATE, null, Collections.emptyList(), Collections.emptyList());

            assertEquals(sickLeaves, actualSickLeaveList);
        }

        @Test
        void shouldFilterOnFromDateIfValue() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID);
            expectedSickLeave.setSlut(FROM_END_DATE.minusDays(1));
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12, ANOTHER_PATIENT_ID);
            anotherSickLeave.setSlut(FROM_END_DATE.minusDays(5));
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                    null, null, FROM_END_DATE, null, Collections.emptyList(), Collections.emptyList());

            assertEquals(0, actualSickLeaveList.size());
        }

        @Test
        void shouldFilterOnToDateIfValue() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID);
            expectedSickLeave.setSlut(TO_END_DATE.plusDays(1));
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12, ANOTHER_PATIENT_ID);
            anotherSickLeave.setSlut(TO_END_DATE.plusDays(5));
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                    null, null, null, TO_END_DATE, Collections.emptyList(), Collections.emptyList());

            assertEquals(0, actualSickLeaveList.size());
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

            expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID);
            expectedSickLeave.setLakare(doctor1);
            anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12, ANOTHER_PATIENT_ID);
            anotherSickLeave.setLakare(doctor2);
            sickLeaves = List.of(expectedSickLeave, anotherSickLeave);
        }

        @Test
        void shouldNotFilterOnDoctorIdsIfNull() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                    null, null, null, TO_END_DATE, null, Collections.emptyList());

            assertEquals(sickLeaves, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnDoctorIdsIfEmpty() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                    null, null, null, TO_END_DATE, Collections.emptyList(), Collections.emptyList());

            assertEquals(sickLeaves, actualSickLeaveList);
        }

        @Test
        void shouldFilterDoctorIdsExcludedInFilter() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                    null, null, null, TO_END_DATE, Collections.singletonList(doctor1.getId()), Collections.emptyList());

            assertEquals(1, actualSickLeaveList.size());
            assertEquals(expectedSickLeave, actualSickLeaveList.get(0));
        }

        @Test
        void shouldNotFilterDoctorIdsIncludedInFilter() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                    null, null, null, TO_END_DATE, List.of(doctor1.getId(), doctor2.getId()), Collections.emptyList());

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
            expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID);
            expectedSickLeave.setRekoStatus(rekoStatus1);
            anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12, ANOTHER_PATIENT_ID);
            anotherSickLeave.setRekoStatus(rekoStatus2);
            sickLeaveWithNoRekoStatus = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID);
            sickLeaves = List.of(expectedSickLeave, anotherSickLeave, sickLeaveWithNoRekoStatus);
        }

        @Test
        void shouldNotFilterOnRekoStatusIfNull() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                    null, null, null, TO_END_DATE, Collections.emptyList(), null);

            assertEquals(sickLeaves, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnRekoStatusIfEmpty() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                    null, null, null, TO_END_DATE, Collections.emptyList(), Collections.emptyList());

            assertEquals(sickLeaves, actualSickLeaveList);
        }

        @Test
        void shouldFilterRekoStatusExcludedInFilter() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                    null, null, null, TO_END_DATE, Collections.emptyList(),
                    Collections.singletonList(RekoStatusType.REKO_1.toString()));

            assertEquals(1, actualSickLeaveList.size());
            assertEquals(expectedSickLeave, actualSickLeaveList.get(0));
        }

        @Test
        void shouldNotFilterRekoStatusIncludedInFilter() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                    null, null, null, TO_END_DATE, Collections.emptyList(),
                    List.of(RekoStatusType.REKO_1.toString(), RekoStatusType.REKO_2.toString(), RekoStatusType.REKO_3.toString()));

            assertEquals(sickLeaves, actualSickLeaveList);
            assertEquals(expectedSickLeave, actualSickLeaveList.get(0));
            assertEquals(anotherSickLeave, actualSickLeaveList.get(1));
        }

        @Test
        void shouldFilterSickLeaveWithNoRekoStatusAsRekoStatus1() {
            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(),
                    null, null, null, TO_END_DATE, Collections.emptyList(),
                    List.of(RekoStatusType.REKO_3.toString(), RekoStatusType.REKO_2.toString()));

            assertEquals(2, actualSickLeaveList.size());
        }
    }

    private static SjukfallEnhet createSjukFallEnhet(String diagnosisCode, Integer dagar, String patientId) {
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

        return sickLeaveUnit;
    }
}