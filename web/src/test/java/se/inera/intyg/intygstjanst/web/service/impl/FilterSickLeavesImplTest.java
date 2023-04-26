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
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.dto.Vardenhet;
import se.inera.intyg.intygstjanst.web.service.DiagnosisChapterService;

@ExtendWith(MockitoExtension.class)
class FilterSickLeavesImplTest {

    @Mock
    private DiagnosisChapterService diagnosisChapterService;

    @InjectMocks
    private FilterSickLeavesImpl filterSickLeaves;

    private static final String DOCTOR_ID = "doctorId";
    private static final String UNIT_ID = "unitId";
    private static final String DIAGNOSIS_CODE = "A01";
    private static final String ANOTHER_DIAGNOSIS_CODE = "C10";
    private static final String DIAGNOSIS_CHAPTER = "A00-B99Vissa infektionssjukdomar och parasitsjukdomar";
    private static final String ANOTHER_DIAGNOSIS_CHAPTER = "C00-D48Tumörer";

    @Nested
    class FilterOnSickLeaveLength {

        @Test
        void shouldFilterOnSickLeaveLengthIfSpecified() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12);
            final var expectedSickLeaveList = List.of(expectedSickLeave);
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, 10, 15, Collections.emptyList());

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnSickLeaveLengthIfFromAndToIsNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12);
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, null, Collections.emptyList());

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnSickLeaveLengthIfFromIsNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12);
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, null, 5, Collections.emptyList());

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnSickLeaveLengthIfToIsNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12);
            final var anotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, 1, null, Collections.emptyList());

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }
    }

    @Nested
    class FilterOnDiagnosisChapter {

        @Test
        void shouldFilterOnDiagnosisChapterIfSpecified() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5);
            final var expectedSickLeaveList = List.of(expectedSickLeave);

            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 12);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            doReturn(new DiagnosKapitel(DIAGNOSIS_CHAPTER))
                .when(diagnosisChapterService)
                .getDiagnosisChaptersFromSickLeave(expectedSickLeave);

            doReturn(new DiagnosKapitel(ANOTHER_DIAGNOSIS_CHAPTER))
                .when(diagnosisChapterService)
                .getDiagnosisChaptersFromSickLeave(anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, 1, 365, List.of(new DiagnosKapitel(DIAGNOSIS_CHAPTER)));

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnDiagnosisChapterIfNull() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12);
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 5);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, 1, 365, null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }

        @Test
        void shouldNotFilterOnDiagnosisChapterIfEmpty() {
            final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12);
            final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 5);
            final var expectedSickLeaveList = List.of(expectedSickLeave, anotherSickLeave);
            final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave);

            final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, 1, 365, null);

            assertEquals(expectedSickLeaveList, actualSickLeaveList);
        }
    }

    @Test
    void shouldFilterOnDiagnosisChapterIfSpecified() {
        final var expectedSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 5);
        final var expectedSickLeaveList = List.of(expectedSickLeave);

        final var anotherSickLeave = createSjukFallEnhet(ANOTHER_DIAGNOSIS_CODE, 5);
        final var andAnotherSickLeave = createSjukFallEnhet(DIAGNOSIS_CODE, 12);
        final var sickLeaves = List.of(expectedSickLeave, anotherSickLeave, andAnotherSickLeave);

        doReturn(new DiagnosKapitel(DIAGNOSIS_CHAPTER))
            .when(diagnosisChapterService)
            .getDiagnosisChaptersFromSickLeave(expectedSickLeave);

        doReturn(new DiagnosKapitel(ANOTHER_DIAGNOSIS_CHAPTER))
            .when(diagnosisChapterService)
            .getDiagnosisChaptersFromSickLeave(anotherSickLeave);

        final var actualSickLeaveList = filterSickLeaves.filter(sickLeaves, 1, 8, List.of(new DiagnosKapitel(DIAGNOSIS_CHAPTER)));

        assertEquals(expectedSickLeaveList, actualSickLeaveList);
    }

    private static SjukfallEnhet createSjukFallEnhet(String diagnosisCode, Integer dagar) {
        SjukfallEnhet sickLeaveUnit = new SjukfallEnhet();
        Lakare lakare = Lakare.create(DOCTOR_ID, null);
        sickLeaveUnit.setLakare(lakare);
        sickLeaveUnit.setVardenhet(
            Vardenhet.create(UNIT_ID, null)
        );
        sickLeaveUnit.setDagar(dagar != null ? dagar : 0);
        sickLeaveUnit.setDiagnosKod(DiagnosKod.create(diagnosisCode));

        return sickLeaveUnit;
    }
}