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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKapitel;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKod;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.intygstjanst.web.service.DiagnosisChapterService;
import se.inera.intyg.intygstjanst.web.service.dto.GetSickLeaveFilterServiceResponse;

@ExtendWith(MockitoExtension.class)
class CreateSickLeaveFilterImplTest {

    @Mock
    private DiagnosisChapterService diagnosisChapterService;

    @InjectMocks
    private CreateSickLeaveFilterImpl createSickLeaveFilter;

    private static final String DOCTOR_ID_ONE = "DoctorId1";
    private static final String DOCTOR_ID_TWO = "DoctorId2";
    private static final DiagnosKapitel DIAGNOSIS_CHAPTER_1 = new DiagnosKapitel("A00-B99Vissa infektionssjukdomar och parasitsjukdomar");
    private static final DiagnosKapitel DIAGNOSIS_CHAPTER_2 = new DiagnosKapitel("C00-D48Tumörer");
    private static final DiagnosKod DIAGNOSIS_A01 = DiagnosKod.create("A01");
    private static final DiagnosKod DIAGNOSIS_C01 = DiagnosKod.create("C01");

    @Nested
    class DoctorsTest {

        @Test
        void shallIncludeListOfDoctors() {
            final var expectedFilter = GetSickLeaveFilterServiceResponse.builder()
                .activeDoctors(
                    List.of(
                        Lakare.create(DOCTOR_ID_ONE, DOCTOR_ID_ONE),
                        Lakare.create(DOCTOR_ID_TWO, DOCTOR_ID_TWO)
                    )
                )
                .diagnosisChapters(
                    Collections.emptyList()
                )
                .build();

            final var intygDataOne = new IntygData();
            intygDataOne.setLakareId(DOCTOR_ID_ONE);
            final var intygDataTwo = new IntygData();
            intygDataTwo.setLakareId(DOCTOR_ID_TWO);
            final var intygDataList = List.of(intygDataOne, intygDataTwo);

            final var actualFilter = createSickLeaveFilter.create(intygDataList);

            assertEquals(expectedFilter, actualFilter);
        }

        @Test
        void shallOnlyIncludeUniqueDoctors() {
            final var expectedFilter = GetSickLeaveFilterServiceResponse.builder()
                .activeDoctors(
                    List.of(
                        Lakare.create(DOCTOR_ID_ONE, DOCTOR_ID_ONE),
                        Lakare.create(DOCTOR_ID_TWO, DOCTOR_ID_TWO)
                    )
                )
                .diagnosisChapters(
                    Collections.emptyList()
                )
                .build();

            final var intygDataOne = new IntygData();
            intygDataOne.setLakareId(DOCTOR_ID_ONE);
            final var intygDataTwo = new IntygData();
            intygDataTwo.setLakareId(DOCTOR_ID_TWO);
            final var intygDataThree = new IntygData();
            intygDataTwo.setLakareId(DOCTOR_ID_TWO);
            final var intygDataList = List.of(intygDataOne, intygDataTwo, intygDataThree);

            final var actualFilter = createSickLeaveFilter.create(intygDataList);

            assertEquals(expectedFilter, actualFilter);
        }
    }

    @Nested
    class DiagnosisChaptersTest {

        @BeforeEach
        void setUp() {
            doReturn(DIAGNOSIS_CHAPTER_1)
                .when(diagnosisChapterService)
                .getDiagnosisChapter(DIAGNOSIS_A01);

            doReturn(DIAGNOSIS_CHAPTER_2)
                .when(diagnosisChapterService)
                .getDiagnosisChapter(DIAGNOSIS_C01);
        }

        @Test
        void shallIncludeListOfDiagnosisChapters() {
            final var expectedFilter = GetSickLeaveFilterServiceResponse.builder()
                .activeDoctors(
                    Collections.emptyList()
                )
                .diagnosisChapters(
                    List.of(
                        DIAGNOSIS_CHAPTER_1,
                        DIAGNOSIS_CHAPTER_2
                    )
                )
                .build();

            final var intygDataOne = new IntygData();
            intygDataOne.setDiagnosKod(DIAGNOSIS_A01);
            final var intygDataTwo = new IntygData();
            intygDataTwo.setDiagnosKod(DIAGNOSIS_C01);
            final var intygDataList = List.of(intygDataOne, intygDataTwo);

            final var actualFilter = createSickLeaveFilter.create(intygDataList);

            assertEquals(expectedFilter, actualFilter);
        }

        @Test
        void shallOnlyIncludeUniqueDiagnosisChapters() {
            final var expectedFilter = GetSickLeaveFilterServiceResponse.builder()
                .activeDoctors(
                    Collections.emptyList()
                )
                .diagnosisChapters(
                    List.of(
                        DIAGNOSIS_CHAPTER_1,
                        DIAGNOSIS_CHAPTER_2
                    )
                )
                .build();

            final var intygDataOne = new IntygData();
            intygDataOne.setDiagnosKod(DIAGNOSIS_A01);
            final var intygDataTwo = new IntygData();
            intygDataTwo.setDiagnosKod(DIAGNOSIS_C01);
            final var intygDataThree = new IntygData();
            intygDataThree.setDiagnosKod(DIAGNOSIS_C01);
            final var intygDataList = List.of(intygDataOne, intygDataTwo, intygDataThree);

            final var actualFilter = createSickLeaveFilter.create(intygDataList);

            assertEquals(expectedFilter, actualFilter);
        }
    }

    @Test
    void shallReturnFilterWithBothDoctorsAndDiagnosisChapters() {
        final var expectedFilter = GetSickLeaveFilterServiceResponse.builder()
            .activeDoctors(
                List.of(
                    Lakare.create(DOCTOR_ID_ONE, DOCTOR_ID_ONE),
                    Lakare.create(DOCTOR_ID_TWO, DOCTOR_ID_TWO)
                )
            )
            .diagnosisChapters(
                List.of(
                    DIAGNOSIS_CHAPTER_1,
                    DIAGNOSIS_CHAPTER_2
                )
            )
            .build();

        doReturn(DIAGNOSIS_CHAPTER_1)
            .when(diagnosisChapterService)
            .getDiagnosisChapter(DIAGNOSIS_A01);

        doReturn(DIAGNOSIS_CHAPTER_2)
            .when(diagnosisChapterService)
            .getDiagnosisChapter(DIAGNOSIS_C01);

        final var intygDataOne = new IntygData();
        intygDataOne.setLakareId(DOCTOR_ID_ONE);
        intygDataOne.setDiagnosKod(DIAGNOSIS_A01);
        final var intygDataTwo = new IntygData();
        intygDataTwo.setLakareId(DOCTOR_ID_TWO);
        intygDataTwo.setDiagnosKod(DIAGNOSIS_C01);
        final var intygDataList = List.of(intygDataOne, intygDataTwo);

        final var actualFilter = createSickLeaveFilter.create(intygDataList);

        assertEquals(expectedFilter, actualFilter);
    }

    @Test
    void shallReturnEmptyFilterIfNoIntygData() {
        final var expectedFilter = GetSickLeaveFilterServiceResponse.builder().build();

        final var actualFilter = createSickLeaveFilter.create(Collections.emptyList());

        assertEquals(expectedFilter, actualFilter);
    }
}