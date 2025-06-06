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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKapitel;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKod;
import se.inera.intyg.infra.sjukfall.dto.Formaga;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.intygstjanst.web.service.DiagnosisChapterService;
import se.inera.intyg.intygstjanst.web.service.dto.GetSickLeaveFilterServiceResponse;
import se.inera.intyg.intygstjanst.web.service.dto.OccupationType;
import se.inera.intyg.intygstjanst.web.service.dto.OccupationTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusType;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusTypeDTO;

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
    private static final List<RekoStatusTypeDTO> REKO_LIST = Arrays.asList(
        new RekoStatusTypeDTO(RekoStatusType.REKO_1.toString(), RekoStatusType.REKO_1.getName()),
        new RekoStatusTypeDTO(RekoStatusType.REKO_2.toString(), RekoStatusType.REKO_2.getName()),
        new RekoStatusTypeDTO(RekoStatusType.REKO_3.toString(), RekoStatusType.REKO_3.getName()),
        new RekoStatusTypeDTO(RekoStatusType.REKO_4.toString(), RekoStatusType.REKO_4.getName()),
        new RekoStatusTypeDTO(RekoStatusType.REKO_5.toString(), RekoStatusType.REKO_5.getName()),
        new RekoStatusTypeDTO(RekoStatusType.REKO_6.toString(), RekoStatusType.REKO_6.getName()),
        new RekoStatusTypeDTO(RekoStatusType.REKO_7.toString(), RekoStatusType.REKO_7.getName()),
        new RekoStatusTypeDTO(RekoStatusType.REKO_8.toString(), RekoStatusType.REKO_8.getName())
    );

    private static final List<OccupationTypeDTO> OCCUPATION_TYPE_DTO_LIST = Arrays.asList(
        new OccupationTypeDTO(OccupationType.NUVARANDE_ARBETE.toString(), OccupationType.NUVARANDE_ARBETE.getName()),
        new OccupationTypeDTO(OccupationType.ARBETSSOKANDE.toString(), OccupationType.ARBETSSOKANDE.getName()),
        new OccupationTypeDTO(OccupationType.FORALDRALEDIG.toString(), OccupationType.FORALDRALEDIG.getName()),
        new OccupationTypeDTO(OccupationType.STUDIER.toString(), OccupationType.STUDIER.getName())
    );

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
                .nbrOfSickLeaves(2)
                .rekoStatusTypes(REKO_LIST)
                .occupationTypes(OCCUPATION_TYPE_DTO_LIST)
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
                .nbrOfSickLeaves(3)
                .rekoStatusTypes(REKO_LIST)
                .occupationTypes(OCCUPATION_TYPE_DTO_LIST)
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
                .nbrOfSickLeaves(2)
                .rekoStatusTypes(REKO_LIST)
                .occupationTypes(OCCUPATION_TYPE_DTO_LIST)
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
                .nbrOfSickLeaves(3)
                .rekoStatusTypes(REKO_LIST)
                .occupationTypes(OCCUPATION_TYPE_DTO_LIST)
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

    @Nested
    class RekoStatus {

        @Test
        void shallSetAllStatuses() {
            final var actualFilter = createSickLeaveFilter.create(Collections.singletonList(new IntygData()));
            assertEquals(8, actualFilter.getRekoStatusTypes().size());
        }

        @Test
        void shallSetFirstStatus() {
            final var actualFilter = createSickLeaveFilter.create(Collections.singletonList(new IntygData()));
            assertEquals(RekoStatusType.REKO_1.toString(), actualFilter.getRekoStatusTypes().getFirst().getId());
            assertEquals(RekoStatusType.REKO_1.getName(), actualFilter.getRekoStatusTypes().getFirst().getName());
        }

        @Test
        void shallSetSecondStatus() {
            final var actualFilter = createSickLeaveFilter.create(Collections.singletonList(new IntygData()));
            assertEquals(RekoStatusType.REKO_2.toString(), actualFilter.getRekoStatusTypes().get(1).getId());
            assertEquals(RekoStatusType.REKO_2.getName(), actualFilter.getRekoStatusTypes().get(1).getName());
        }

        @Test
        void shallSetThirdStatus() {
            final var actualFilter = createSickLeaveFilter.create(Collections.singletonList(new IntygData()));
            assertEquals(RekoStatusType.REKO_3.toString(), actualFilter.getRekoStatusTypes().get(2).getId());
            assertEquals(RekoStatusType.REKO_3.getName(), actualFilter.getRekoStatusTypes().get(2).getName());
        }

        @Test
        void shallSetFourthStatus() {
            final var actualFilter = createSickLeaveFilter.create(Collections.singletonList(new IntygData()));
            assertEquals(RekoStatusType.REKO_4.toString(), actualFilter.getRekoStatusTypes().get(3).getId());
            assertEquals(RekoStatusType.REKO_4.getName(), actualFilter.getRekoStatusTypes().get(3).getName());
        }

        @Test
        void shallSetFifthStatus() {
            final var actualFilter = createSickLeaveFilter.create(Collections.singletonList(new IntygData()));
            assertEquals(RekoStatusType.REKO_5.toString(), actualFilter.getRekoStatusTypes().get(4).getId());
            assertEquals(RekoStatusType.REKO_5.getName(), actualFilter.getRekoStatusTypes().get(4).getName());
        }

        @Test
        void shallSetSixthStatus() {
            final var actualFilter = createSickLeaveFilter.create(Collections.singletonList(new IntygData()));
            assertEquals(RekoStatusType.REKO_6.toString(), actualFilter.getRekoStatusTypes().get(5).getId());
            assertEquals(RekoStatusType.REKO_6.getName(), actualFilter.getRekoStatusTypes().get(5).getName());
        }
    }

    @Nested
    class OccupationTypes {

        @Test
        void shallContainOccupationNuvarandeArbete() {
            final var actualFilter = createSickLeaveFilter.create(Collections.singletonList(new IntygData()));
            assertEquals(actualFilter.getOccupationTypes().getFirst().getId(), OccupationType.NUVARANDE_ARBETE.toString());
            assertEquals(actualFilter.getOccupationTypes().getFirst().getName(), OccupationType.NUVARANDE_ARBETE.getName());
        }

        @Test
        void shallContainOccupationArbetssokande() {
            final var actualFilter = createSickLeaveFilter.create(Collections.singletonList(new IntygData()));
            assertEquals(actualFilter.getOccupationTypes().get(1).getId(), OccupationType.ARBETSSOKANDE.toString());
            assertEquals(actualFilter.getOccupationTypes().get(1).getName(), OccupationType.ARBETSSOKANDE.getName());
        }


        @Test
        void shallContainOccupationForaldraledig() {
            final var actualFilter = createSickLeaveFilter.create(Collections.singletonList(new IntygData()));
            assertEquals(actualFilter.getOccupationTypes().get(2).getId(), OccupationType.FORALDRALEDIG.toString());
            assertEquals(actualFilter.getOccupationTypes().get(2).getName(), OccupationType.FORALDRALEDIG.getName());
        }

        @Test
        void shallContainOccupationStudier() {
            final var actualFilter = createSickLeaveFilter.create(Collections.singletonList(new IntygData()));
            assertEquals(actualFilter.getOccupationTypes().get(3).getId(), OccupationType.STUDIER.toString());
            assertEquals(actualFilter.getOccupationTypes().get(3).getName(), OccupationType.STUDIER.getName());
        }
    }

    @Nested
    class OngoingSickLeaves {

        @Nested
        class HasOngoingSickLeaves {

            @Test
            void shallReturnTrueIfEndDateIsAfterToday() {
                final var intygData = new IntygData();
                intygData.setFormagor(List.of(getFormaga(10, -5, 25)));
                final var actualFilter = createSickLeaveFilter.create(Collections.singletonList(intygData));
                assertTrue(actualFilter.isHasOngoingSickLeaves());
            }

            @Test
            void shallReturnTrueIfEndDateIsEqualToToday() {
                final var intygData = new IntygData();
                intygData.setFormagor(List.of(getFormaga(10, 0, 25)));
                final var actualFilter = createSickLeaveFilter.create(Collections.singletonList(intygData));
                assertTrue(actualFilter.isHasOngoingSickLeaves());
            }

            @Test
            void shallReturnTrueIfContainingOneEndDateAfterToday() {
                final var intygData = new IntygData();
                intygData.setFormagor(List.of(
                    getFormaga(10, 7, 25),
                    getFormaga(7, 5, 50),
                    getFormaga(5, 2, 75),
                    getFormaga(2, -2, 100))
                );
                final var actualFilter = createSickLeaveFilter.create(Collections.singletonList(intygData));
                assertTrue(actualFilter.isHasOngoingSickLeaves());
            }
        }

        @Nested
        class HasNoOngoingSickLeaves {

            @Test
            void shallReturnFalseIfFormagaIsNull() {
                final var actualFilter = createSickLeaveFilter.create(Collections.singletonList(new IntygData()));
                assertFalse(actualFilter.isHasOngoingSickLeaves());
            }

            @Test
            void shallReturnFalseIfEmptyList() {
                final var actualFilter = createSickLeaveFilter.create(Collections.emptyList());
                assertFalse(actualFilter.isHasOngoingSickLeaves());
            }

            @Test
            void shallReturnFalseIfNoEndDateIsAfterToday() {
                final var intygData = new IntygData();
                intygData.setFormagor(List.of(getFormaga(10, 5, 25)));
                final var actualFilter = createSickLeaveFilter.create(Collections.singletonList(intygData));
                assertFalse(actualFilter.isHasOngoingSickLeaves());
            }

            @Test
            void shallReturnFalseIfContainingNoEndDateAfterToday() {
                final var intygData = new IntygData();
                intygData.setFormagor(List.of(
                    getFormaga(10, 7, 25),
                    getFormaga(7, 5, 50),
                    getFormaga(5, 2, 75),
                    getFormaga(2, 1, 100))
                );
                final var actualFilter = createSickLeaveFilter.create(Collections.singletonList(intygData));
                assertFalse(actualFilter.isHasOngoingSickLeaves());
            }
        }

        private Formaga getFormaga(int daysToSubtractStartDate, int daysToSubtractEndDate, int disability) {
            return new Formaga(
                LocalDate.now().minusDays(daysToSubtractStartDate),
                LocalDate.now().minusDays(daysToSubtractEndDate),
                disability
            );
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
            .rekoStatusTypes(REKO_LIST)
            .occupationTypes(OCCUPATION_TYPE_DTO_LIST)
            .nbrOfSickLeaves(2)
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
    void shallReturnPartialFilterIfNoIntygData() {
        final var expectedFilter =
            GetSickLeaveFilterServiceResponse.builder()
                .rekoStatusTypes(Arrays
                    .stream(RekoStatusType.values())
                    .map((status) -> new RekoStatusTypeDTO(status.toString(), status.getName()))
                    .collect(Collectors.toList()))
                .occupationTypes(Arrays
                    .stream(OccupationType.values())
                    .map(status -> new OccupationTypeDTO(status.toString(), status.getName()))
                    .collect(Collectors.toList()))
                .build();

        final var actualFilter = createSickLeaveFilter.create(Collections.emptyList());

        assertEquals(expectedFilter, actualFilter);
    }
}
