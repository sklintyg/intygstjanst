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

import java.util.Collections;
import java.util.List;
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
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.dto.Vardenhet;
import se.inera.intyg.intygstjanst.web.service.CalculatePatientAgeService;
import se.inera.intyg.intygstjanst.web.service.DiagnosisChapterService;
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
                Collections.emptyList(), null, null);

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
                Collections.emptyList(), null, null);

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
                Collections.emptyList(), null, null);

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
                Collections.emptyList(), null, null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnSickLeaveLengthIfNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID);
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, Collections.emptyList(), null, null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnSickLeaveLengthIfEmpty() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID);
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5, PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, Collections.emptyList(), Collections.emptyList(), null,
                null);

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
                null, null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnDiagnosisChapterIfNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID);
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 5, PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, null, null, null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnDiagnosisChapterIfEmpty() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID);
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 5, PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, null, null, null);

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
                30, 60);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnPatientAgeIfFromIsNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID);
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 5, ANOTHER_PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, null, null, 50);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnPatientAgeIfToIsNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12, PATIENT_ID);
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 5, ANOTHER_PATIENT_ID);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, null, 50, null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
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

        return sickLeaveUnit;
    }
}