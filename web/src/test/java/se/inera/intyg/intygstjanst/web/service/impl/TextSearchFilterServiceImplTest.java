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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKapitel;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKategori;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKod;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.infra.sjukfall.dto.Patient;
import se.inera.intyg.infra.sjukfall.dto.RekoStatusDTO;
import se.inera.intyg.infra.sjukfall.dto.RekoStatusTypeDTO;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.web.service.CalculatePatientAgeService;
import se.inera.intyg.intygstjanst.web.service.DiagnosisDescriptionService;
import se.inera.intyg.intygstjanst.web.service.ResolvePatientGenderService;

@ExtendWith(MockitoExtension.class)
class TextSearchFilterServiceImplTest {

    @Mock
    private CalculatePatientAgeService calculatePatientAgeService;
    @Mock
    private ResolvePatientGenderService resolvePatientGenderService;
    @Mock
    private DiagnosisDescriptionService diagnosisDescriptionService;

    @InjectMocks
    private TextSearchFilterServiceImpl textSearchFilterService;

    private static final String PATIENT_ARNOLD_ID = "191212121212";
    private static final String PATIENT_ARNOLD_NAME = "Arnold Johansson";
    private static final String DOKTOR_AJLA = "Doktor Ajla";
    private static final String ANNIKA_LARSSON = "Annika Larsson";
    private static final String VARDADMIN_ALVA = "Vardadministratör Alva";
    private static final String PATIENT_ATTILA_ID = "194012019149";
    private static final String PATIENT_ATTILA_NAME = "Attila Person";
    private static final String DIAGNOSIS_CODE_N41 = "N41";
    private static final String DIAGNOSIS_CODE_F23 = "F23";
    private static final String NO_MATCHING_RESULT = "noMatchingResult";
    private static final String MALE = "Man";
    private static final String FEMMALE = "Kvinna";
    private static final String HSA_ID = "hsaId";
    private static final String NUVARANDE_ARBETE = "Nuvarande arbete";
    private static final String STUDIER = "Studier";
    private static final String ARBETSSOKANDE = "Arbetssökande";
    private static final String FORALDRALEDIGHET = "Föräldraledighet";
    private static final String DIAGNOSIS_DESCRIPTION_AKUT = "Akut stressreaktion";
    private static final String DIAGNOSIS_DESCRIPTION_PTSD = "Posttraumatisk";
    private static final String REKO_ID = "id";
    private static final String REKO_INGEN = "Ingen";
    private static final String REKO_AVBOJD = "Avböjd";
    private static final String REKO_KONTAKTAD = "Kontaktad";

    @Nested
    class TextSearch {

        @Test
        void shouldReturnTrueIfTextSearchIsEmpty() {
            final var filterResult = textSearchFilterService.filter(new SjukfallEnhet(), "");
            assertTrue(filterResult);
        }

        @Test
        void shouldReturnTrueIfTextSearchIsNull() {
            final var filterResult = textSearchFilterService.filter(new SjukfallEnhet(), null);
            assertTrue(filterResult);
        }

        @Test
        void shouldReturnSickLeaveIfTextSearchIsEmpty() {
            final var expectedResult = List.of(new SjukfallEnhet());
            final var filterResult = textSearchFilterService.filterList(expectedResult, "");
            assertEquals(expectedResult, filterResult);
        }

        @Test
        void shouldReturnSickLeaveIfTextSearchIsNull() {
            final var expectedResult = List.of(new SjukfallEnhet());
            final var filterResult = textSearchFilterService.filterList(expectedResult, null);
            assertEquals(expectedResult, filterResult);
        }

    }

    @Nested
    class FilterList {

        @Nested
        class PatientAge {


            @Test
            void shouldFilterOnMatchingPatientAge() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                doReturn(100).when(calculatePatientAgeService).get(PATIENT_ARNOLD_ID);
                doReturn(200).when(calculatePatientAgeService).get(PATIENT_ATTILA_ID);

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), "100");
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnPartialPatientAge() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                doReturn(100).when(calculatePatientAgeService).get(PATIENT_ARNOLD_ID);
                doReturn(10).when(calculatePatientAgeService).get(PATIENT_ATTILA_ID);

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), "10");
                assertEquals(List.of(expectedResult, sickLeaveUnit), filterResult);
            }

            @Test
            void shouldFilterOnPatientAge() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                doReturn(100).when(calculatePatientAgeService).get(PATIENT_ARNOLD_ID);
                doReturn(200).when(calculatePatientAgeService).get(PATIENT_ATTILA_ID);

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), NO_MATCHING_RESULT);
                assertEquals(0, filterResult.size());
            }
        }

        @Nested
        class PatientName {

            @Test
            void shouldFilterOnMatchingPatientName() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), "Arnold");
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnPartialPatientName() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit),
                    PATIENT_ARNOLD_NAME.substring(0, 5));
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnPatientName() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), NO_MATCHING_RESULT);
                assertEquals(0, filterResult.size());
            }
        }

        @Nested
        class Gender {

            @Test
            void shouldFilterOnGenderMale() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                doReturn(MALE).when(resolvePatientGenderService).get(PATIENT_ARNOLD_ID);
                doReturn(FEMMALE).when(resolvePatientGenderService).get(PATIENT_ATTILA_ID);

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), MALE);
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnGenderFemale() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var expectedResult = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                doReturn(MALE).when(resolvePatientGenderService).get(PATIENT_ARNOLD_ID);
                doReturn(FEMMALE).when(resolvePatientGenderService).get(PATIENT_ATTILA_ID);

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), FEMMALE);
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnGender() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var expectedResult = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                doReturn(MALE).when(resolvePatientGenderService).get(PATIENT_ARNOLD_ID);
                doReturn(FEMMALE).when(resolvePatientGenderService).get(PATIENT_ATTILA_ID);

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), NO_MATCHING_RESULT);
                assertEquals(0, filterResult.size());
            }

        }

        @Nested
        class Diagnosis {

            @Test
            void shouldFilterOnMatchingDiagnosisCode() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), DIAGNOSIS_CODE_N41);
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnPartialDiagnosisCode() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit),
                    DIAGNOSIS_CODE_N41.substring(0, 2));
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnMatchingDiagnosisDescription() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                doReturn(DIAGNOSIS_DESCRIPTION_AKUT).when(diagnosisDescriptionService)
                    .getDiagnosisDescriptionFromSickLeave(DIAGNOSIS_CODE_N41);
                doReturn(DIAGNOSIS_DESCRIPTION_PTSD).when(diagnosisDescriptionService)
                    .getDiagnosisDescriptionFromSickLeave(DIAGNOSIS_CODE_F23);

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit),
                    DIAGNOSIS_DESCRIPTION_AKUT);
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnPartialDiagnosisDescription() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                doReturn(DIAGNOSIS_DESCRIPTION_AKUT).when(diagnosisDescriptionService)
                    .getDiagnosisDescriptionFromSickLeave(DIAGNOSIS_CODE_N41);
                doReturn(DIAGNOSIS_DESCRIPTION_PTSD).when(diagnosisDescriptionService)
                    .getDiagnosisDescriptionFromSickLeave(DIAGNOSIS_CODE_F23);

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit),
                    DIAGNOSIS_DESCRIPTION_AKUT.substring(0, 2));
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnMatchingBiDiagnosisCode() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    List.of(DIAGNOSIS_CODE_N41, DIAGNOSIS_CODE_N41));
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), DIAGNOSIS_CODE_N41);
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnPartialBiDiagnosisCode() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    List.of(DIAGNOSIS_CODE_N41, DIAGNOSIS_CODE_N41));
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit),
                    DIAGNOSIS_CODE_N41.substring(0, 2));
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnDiagnosis() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), NO_MATCHING_RESULT);
                assertEquals(0, filterResult.size());
            }

            private DiagnosKapitel getDiagnosKapitel(String diagnosisCodeN41) {
                return new DiagnosKapitel(new DiagnosKategori('a', 0), new DiagnosKategori('a', 0), diagnosisCodeN41);
            }
        }

        @Nested
        class Period {

            @Test
            void shouldFilterOnStartPeriod() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                expectedResult.setStart(LocalDate.now().minusDays(5));
                expectedResult.setSlut(LocalDate.now().plusDays(5));
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setStart(LocalDate.now().minusDays(0));
                sickLeaveUnit.setSlut(LocalDate.now().plusDays(10));

                final var textSearch = LocalDate.now().minusDays(5).toString();

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), textSearch);
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnPartialStartPeriod() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                expectedResult.setStart(LocalDate.now().minusDays(5));
                expectedResult.setSlut(LocalDate.now().plusDays(5));
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setStart(LocalDate.now().minusYears(10));
                sickLeaveUnit.setSlut(LocalDate.now().minusYears(9));

                final var textSearch = LocalDate.now().minusDays(5).toString().substring(0, 3);

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), textSearch);
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnEndPeriod() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                expectedResult.setStart(LocalDate.now().minusDays(5));
                expectedResult.setSlut(LocalDate.now().plusDays(5));
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setStart(LocalDate.now().minusDays(0));
                sickLeaveUnit.setSlut(LocalDate.now().plusDays(10));

                final var textSearch = LocalDate.now().plusDays(5).toString();

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), textSearch);
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnPartialEndPeriod() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                expectedResult.setStart(LocalDate.now().minusDays(5));
                expectedResult.setSlut(LocalDate.now().plusDays(5));
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setStart(LocalDate.now().minusYears(10));
                sickLeaveUnit.setSlut(LocalDate.now().minusYears(9));

                final var textSearch = LocalDate.now().plusDays(10).toString().substring(0, 3);

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), textSearch);
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnPeriod() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), NO_MATCHING_RESULT);
                assertEquals(0, filterResult.size());
            }
        }

        @Nested
        class Length {

            @Test
            void shouldFilterOnMatchingLength() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                expectedResult.setDagar(555);
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setDagar(2);

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), "555");
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnMatchingLengthWithDays() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                expectedResult.setDagar(5);
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setDagar(2);

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), "5 dagar");
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnLength() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                expectedResult.setDagar(5);
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setDagar(2);

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), NO_MATCHING_RESULT);
                assertEquals(0, filterResult.size());
            }
        }

        @Nested
        class NumberOfCertificate {

            @Test
            void shouldFilterOnMatchingNumberOfCertificate() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                expectedResult.setIntygLista(List.of("", "", "", "", ""));

                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setIntygLista(List.of(""));

                correctDateForSickLeave(expectedResult);
                correctDateForSickLeave(sickLeaveUnit);

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), "5");
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnNumberOfCertificate() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                expectedResult.setIntygLista(List.of("", "", "", "", ""));
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setIntygLista(List.of("", ""));

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), NO_MATCHING_RESULT);
                assertEquals(0, filterResult.size());
            }
        }

        @Nested
        class Degree {

            @Test
            void shouldFilterOnMatchingDegree() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                expectedResult.setAktivGrad(25);
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setAktivGrad(50);

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), "25%");
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnPartialDegree() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                expectedResult.setAktivGrad(33333);
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setAktivGrad(50);

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), "333");
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnDegree() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                expectedResult.setAktivGrad(25);
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setAktivGrad(50);

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), NO_MATCHING_RESULT);
                assertEquals(0, filterResult.size());
            }
        }

        @Nested
        class DoctorName {

            @Test
            void shouldFilterOnMatchingDoctorName() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, ANNIKA_LARSSON,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), DOKTOR_AJLA);
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnPartialDoctorName() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, ANNIKA_LARSSON,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit),
                    DOKTOR_AJLA.substring(0, 3));
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnDoctorName() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, ANNIKA_LARSSON,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), NO_MATCHING_RESULT);
                assertEquals(0, filterResult.size());
            }
        }

        @Nested
        class PatientId {

            @Test
            void shouldFilterOnMatchingPatientId() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, ANNIKA_LARSSON,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), PATIENT_ARNOLD_ID);
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnPartialPatientId() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, ANNIKA_LARSSON,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit),
                    PATIENT_ARNOLD_ID.substring(0, 5));
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnPatientId() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, ANNIKA_LARSSON,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), NO_MATCHING_RESULT);
                assertEquals(0, filterResult.size());
            }
        }

        @Nested
        class Occupation {

            @Test
            void shouldFilterOnMatchingOccupation() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                expectedResult.setSysselsattning(List.of(NUVARANDE_ARBETE));
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, ANNIKA_LARSSON,
                    Collections.emptyList());
                sickLeaveUnit.setSysselsattning(List.of(STUDIER));

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), NUVARANDE_ARBETE);
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnPartialOccupation() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                expectedResult.setSysselsattning(List.of(NUVARANDE_ARBETE));
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, ANNIKA_LARSSON,
                    Collections.emptyList());
                sickLeaveUnit.setSysselsattning(List.of(STUDIER));

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit),
                    NUVARANDE_ARBETE.substring(0, 3));
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnMultipleOccupation() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                expectedResult.setSysselsattning(List.of(NUVARANDE_ARBETE));
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, ANNIKA_LARSSON,
                    Collections.emptyList());
                sickLeaveUnit.setSysselsattning(List.of(STUDIER, ARBETSSOKANDE, FORALDRALEDIGHET));

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), NUVARANDE_ARBETE);
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnOccupation() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                expectedResult.setSysselsattning(List.of(STUDIER));
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, ANNIKA_LARSSON,
                    Collections.emptyList());
                sickLeaveUnit.setSysselsattning(List.of(STUDIER, ARBETSSOKANDE, FORALDRALEDIGHET));

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), NUVARANDE_ARBETE);
                assertEquals(0, filterResult.size());
            }
        }

        @Nested
        class RekoStatus {

            @Test
            void shouldFilterOnMatchingRekoStatus() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                expectedResult.setRekoStatus(getRekoStatusDTO(REKO_AVBOJD));
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, ANNIKA_LARSSON,
                    Collections.emptyList());
                sickLeaveUnit.setRekoStatus(getRekoStatusDTO(REKO_KONTAKTAD));

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), REKO_AVBOJD);
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnPartialRekoStatus() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                expectedResult.setRekoStatus(getRekoStatusDTO(REKO_AVBOJD));
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, ANNIKA_LARSSON,
                    Collections.emptyList());
                sickLeaveUnit.setRekoStatus(getRekoStatusDTO(REKO_KONTAKTAD));

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit),
                    REKO_AVBOJD.substring(0, 3));
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnEmptyRekoStatus() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, ANNIKA_LARSSON,
                    Collections.emptyList());
                sickLeaveUnit.setRekoStatus(getRekoStatusDTO(REKO_KONTAKTAD));

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), REKO_INGEN);
                assertEquals(List.of(expectedResult), filterResult);
            }

            @Test
            void shouldFilterOnRekoStatus() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                expectedResult.setRekoStatus(getRekoStatusDTO(REKO_AVBOJD));
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, ANNIKA_LARSSON,
                    Collections.emptyList());
                sickLeaveUnit.setRekoStatus(getRekoStatusDTO(REKO_KONTAKTAD));

                final var filterResult = textSearchFilterService.filterList(List.of(expectedResult, sickLeaveUnit), REKO_INGEN);
                assertEquals(0, filterResult.size());
            }
        }
    }

    @Nested
    class Filter {

        @Nested
        class PatientAge {


            @Test
            void shouldFilterOnMatchingPatientAge() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());

                doReturn(100).when(calculatePatientAgeService).get(PATIENT_ARNOLD_ID);

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, "100");
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnPartialPatientAge() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());

                doReturn(100).when(calculatePatientAgeService).get(PATIENT_ARNOLD_ID);

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, "10");
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnPatientAge() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());

                doReturn(100).when(calculatePatientAgeService).get(PATIENT_ARNOLD_ID);

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, NO_MATCHING_RESULT);
                assertFalse(filterResult);
            }
        }

        @Nested
        class PatientName {

            @Test
            void shouldFilterOnMatchingPatientName() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, "Arnold");
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnPartialPatientName() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, PATIENT_ARNOLD_NAME.substring(0, 5));
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnPatientName() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, NO_MATCHING_RESULT);
                assertFalse(filterResult);
            }
        }

        @Nested
        class Gender {

            @Test
            void shouldFilterOnGenderMale() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());

                doReturn(MALE).when(resolvePatientGenderService).get(PATIENT_ARNOLD_ID);

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, MALE);
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnGenderFemale() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());

                doReturn(FEMMALE).when(resolvePatientGenderService).get(PATIENT_ATTILA_ID);

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, FEMMALE);
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnGender() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ATTILA_ID, PATIENT_ATTILA_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, NO_MATCHING_RESULT);
                assertFalse(filterResult);
            }
        }

        @Nested
        class Diagnosis {

            @Test
            void shouldFilterOnMatchingDiagnosisCode() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, DIAGNOSIS_CODE_N41);
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnPartialDiagnosisCode() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, DIAGNOSIS_CODE_N41.substring(0, 2));
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnMatchingDiagnosisDescription() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());

                doReturn(DIAGNOSIS_DESCRIPTION_AKUT).when(diagnosisDescriptionService)
                    .getDiagnosisDescriptionFromSickLeave(DIAGNOSIS_CODE_N41);

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, DIAGNOSIS_DESCRIPTION_AKUT);
                assertTrue(filterResult);
            }


            @Test
            void shouldFilterOnPartialDiagnosisDescription() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());

                doReturn(DIAGNOSIS_DESCRIPTION_AKUT).when(diagnosisDescriptionService)
                    .getDiagnosisDescriptionFromSickLeave(DIAGNOSIS_CODE_N41);

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, DIAGNOSIS_DESCRIPTION_AKUT.substring(0, 2));
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnMatchingBiDiagnosisCode() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    List.of(DIAGNOSIS_CODE_N41, DIAGNOSIS_CODE_N41));

                final var filterResult = textSearchFilterService.filter(expectedResult, DIAGNOSIS_CODE_N41);
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnPartialBiDiagnosisCode() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_F23, VARDADMIN_ALVA,
                    List.of(DIAGNOSIS_CODE_N41, DIAGNOSIS_CODE_N41));
                final var filterResult = textSearchFilterService.filter(expectedResult, DIAGNOSIS_CODE_N41.substring(0, 2));
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnDiagnosis() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, NO_MATCHING_RESULT);
                assertFalse(filterResult);
            }

            private DiagnosKapitel getDiagnosKapitel(String diagnosisCodeN41) {
                return new DiagnosKapitel(new DiagnosKategori('a', 0), new DiagnosKategori('a', 0), diagnosisCodeN41);
            }
        }

        @Nested
        class Period {

            @Test
            void shouldFilterOnStartPeriod() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setStart(LocalDate.now().minusDays(5));
                sickLeaveUnit.setSlut(LocalDate.now().plusDays(5));

                final var textSearch = LocalDate.now().minusDays(5).toString();

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, textSearch);
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnPartialStartPeriod() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setStart(LocalDate.now().minusDays(5));
                sickLeaveUnit.setSlut(LocalDate.now().plusDays(5));

                final var textSearch = LocalDate.now().minusDays(5).toString().substring(0, 3);

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, textSearch);
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnEndPeriod() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setStart(LocalDate.now().minusDays(5));
                sickLeaveUnit.setSlut(LocalDate.now().plusDays(5));

                final var textSearch = LocalDate.now().plusDays(5).toString();

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, textSearch);
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnPartialEndPeriod() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setStart(LocalDate.now().minusDays(5));
                sickLeaveUnit.setSlut(LocalDate.now().plusDays(5));

                final var textSearch = LocalDate.now().plusDays(10).toString().substring(0, 3);

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, textSearch);
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnPeriod() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, NO_MATCHING_RESULT);
                assertFalse(filterResult);
            }
        }

        @Nested
        class Length {

            @Test
            void shouldFilterOnMatchingLength() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setDagar(5);

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, "5");
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnMatchingLengthWithDays() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setDagar(5);

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, "5 dagar");
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnLength() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setDagar(5);
                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, NO_MATCHING_RESULT);
                assertFalse(filterResult);
            }
        }

        @Nested
        class NumberOfCertificate {

            @Test
            void shouldFilterOnMatchingNumberOfCertificate() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setIntygLista(List.of("", "", "", "", ""));

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, "5");
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnNumberOfCertificate() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setIntygLista(List.of("", "", "", "", ""));
                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, NO_MATCHING_RESULT);
                assertFalse(filterResult);
            }
        }

        @Nested
        class Degree {

            @Test
            void shouldFilterOnMatchingDegree() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setAktivGrad(25);
                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, "25%");
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnPartialDegree() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setAktivGrad(33333);

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, "333");
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnDegree() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, VARDADMIN_ALVA,
                    Collections.emptyList());
                sickLeaveUnit.setAktivGrad(25);

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, NO_MATCHING_RESULT);
                assertFalse(filterResult);
            }
        }

        @Nested
        class DoctorName {

            @Test
            void shouldFilterOnMatchingDoctorName() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, DOKTOR_AJLA);
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnPartialDoctorName() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, DOKTOR_AJLA.substring(0, 3));
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnDoctorName() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, NO_MATCHING_RESULT);
                assertFalse(filterResult);
            }
        }

        @Nested
        class PatientId {

            @Test
            void shouldFilterOnMatchingPatientId() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, PATIENT_ARNOLD_ID);
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnPartialPatientId() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, PATIENT_ARNOLD_ID.substring(0, 5));
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnPatientId() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, NO_MATCHING_RESULT);
                assertFalse(filterResult);
            }
        }

        @Nested
        class Occupation {

            @Test
            void shouldFilterOnMatchingOccupation() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                expectedResult.setSysselsattning(List.of(NUVARANDE_ARBETE));

                final var filterResult = textSearchFilterService.filter(expectedResult, NUVARANDE_ARBETE);
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnPartialOccupation() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                expectedResult.setSysselsattning(List.of(NUVARANDE_ARBETE));

                final var filterResult = textSearchFilterService.filter(expectedResult, NUVARANDE_ARBETE.substring(0, 3));
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnMultipleOccupation() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                expectedResult.setSysselsattning(List.of(NUVARANDE_ARBETE, STUDIER, ARBETSSOKANDE));

                final var filterResult = textSearchFilterService.filter(expectedResult, STUDIER);
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnOccupation() {
                final var expectedResult = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                expectedResult.setSysselsattning(List.of(NUVARANDE_ARBETE, STUDIER, ARBETSSOKANDE));

                final var filterResult = textSearchFilterService.filter(expectedResult, FORALDRALEDIGHET);
                assertFalse(filterResult);
            }
        }

        @Nested
        class RekoStatus {

            @Test
            void shouldFilterOnMatchingRekoStatus() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                sickLeaveUnit.setRekoStatus(getRekoStatusDTO(REKO_AVBOJD));

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, REKO_AVBOJD);
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnPartialRekoStatus() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                sickLeaveUnit.setRekoStatus(getRekoStatusDTO(REKO_AVBOJD));

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, REKO_AVBOJD.substring(0, 3));
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnEmptyRekoStatus() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());

                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, REKO_INGEN);
                assertTrue(filterResult);
            }

            @Test
            void shouldFilterOnRekoStatus() {
                final var sickLeaveUnit = createSickLeave(PATIENT_ARNOLD_ID, PATIENT_ARNOLD_NAME, DIAGNOSIS_CODE_N41, DOKTOR_AJLA,
                    Collections.emptyList());
                sickLeaveUnit.setRekoStatus(getRekoStatusDTO(REKO_AVBOJD));
                final var filterResult = textSearchFilterService.filter(sickLeaveUnit, REKO_INGEN);
                assertFalse(filterResult);
            }
        }
    }


    private SjukfallEnhet createSickLeave(String patientId, String patientName, String diagnosis, String doctorName,
        List<String> biDiagnosisCodes) {
        final var sickLeaveUnit = new SjukfallEnhet();
        final var biDiagnosi = new ArrayList<DiagnosKod>();
        sickLeaveUnit.setPatient(Patient.create(patientId, patientName));
        sickLeaveUnit.setDiagnosKod(DiagnosKod.create(diagnosis));
        sickLeaveUnit.setStart(LocalDate.now());
        sickLeaveUnit.setSlut(LocalDate.now());
        sickLeaveUnit.setDagar(1);
        sickLeaveUnit.setIntygLista(Collections.emptyList());
        sickLeaveUnit.setAktivGrad(1);
        sickLeaveUnit.setLakare(Lakare.create(HSA_ID, doctorName));
        biDiagnosisCodes.forEach(biDiagnosisCode -> biDiagnosi.add(DiagnosKod.create(biDiagnosisCode)));
        sickLeaveUnit.setBiDiagnoser(biDiagnosi);
        sickLeaveUnit.setSysselsattning(Collections.emptyList());
        return sickLeaveUnit;
    }

    private static RekoStatusDTO getRekoStatusDTO(String status) {
        final var rekoStatusDTO = new RekoStatusDTO();
        rekoStatusDTO.setStatus(new RekoStatusTypeDTO(REKO_ID, status));
        return rekoStatusDTO;
    }

    private static void correctDateForSickLeave(SjukfallEnhet sickLeaveUnit) {
        if (String.valueOf(sickLeaveUnit.getStart().getYear()).contains("5")) {
            sickLeaveUnit.setStart(sickLeaveUnit.getStart().plusYears(1));
            sickLeaveUnit.setSlut(sickLeaveUnit.getSlut().plusYears(1));
        }
        if (String.valueOf(sickLeaveUnit.getStart().getMonthValue()).contains("5")) {
            sickLeaveUnit.setStart(sickLeaveUnit.getStart().plusMonths(1));
            sickLeaveUnit.setSlut(sickLeaveUnit.getSlut().plusMonths(1));
        }
        if (String.valueOf(sickLeaveUnit.getStart().getDayOfMonth()).contains("5")) {
            sickLeaveUnit.setStart(sickLeaveUnit.getStart().plusDays(1));
            sickLeaveUnit.setSlut(sickLeaveUnit.getSlut().plusDays(1));
        }
    }
}
