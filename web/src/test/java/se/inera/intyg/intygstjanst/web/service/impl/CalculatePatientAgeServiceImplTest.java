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

import java.time.LocalDate;
import java.time.Period;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalculatePatientAgeServiceImplTest {

    private static final int[] YEAR_SEPARATOR = {0, 4};
    private static final int[] MONTH_SEPARATOR = {4, 6};
    private static final int[] DAY_SEPARATOR = {6, 8};

    @InjectMocks
    private CalculatePatientAgeServiceImpl calculatePatientAgeService;

    @Test
    void shouldCalculateAgeForStandardPatientId() {
        final var patientId = "19121212-1212";
        final var expectedAge = getExpectedAge(patientId);
        assertEquals(expectedAge.getYears(), calculatePatientAgeService.get(patientId));
    }

    @Test
    void shouldCalculateAgeForStandardPatientIdWithoutSeparator() {
        final var patientId = "19121212-1212";
        final var expectedAge = getExpectedAge(patientId);
        final var patientIdWithoutSeparator = "191212121212";
        assertEquals(expectedAge.getYears(), calculatePatientAgeService.get(patientIdWithoutSeparator));
    }

    @Test
    void shouldCalculateAgeForStandardPatientIdWithPlusSeparator() {
        final var patientId = "19121212-1212";
        final var expectedAge = getExpectedAge(patientId);
        final var patientIdWithPlusSeparator = "19121212+1212";
        assertEquals(expectedAge.getYears(), calculatePatientAgeService.get(patientIdWithPlusSeparator));
    }

    @Test
    void shouldCalculateAgeForSamordningsnummer() {
        final var patientId = "19121212-1212";
        final var expectedAge = getExpectedAge(patientId);
        final var samordningsnummer = "19121272-1212";
        assertEquals(expectedAge.getYears(), calculatePatientAgeService.get(samordningsnummer));
    }

    @Test
    void shouldCalculateAgeForSamordningsnummerWithSingleDigits() {
        final var patientId = "19121201-1212";
        final var expectedAge = getExpectedAge(patientId);
        final var samordningsnummer = "19121261-1212";
        assertEquals(expectedAge.getYears(), calculatePatientAgeService.get(samordningsnummer));
    }

    @Test
    void shouldCalculateAgeFor10DigitPatientId() {
        final var patientId = "19991212-1212";
        final var expectedAge = getExpectedAge(patientId);
        final var tenDigitPatientId = "991212-1212";
        assertEquals(expectedAge.getYears(), calculatePatientAgeService.get(tenDigitPatientId));
    }

    @Test
    void shouldCalculateAgeFor10DigitPatientIdWithoutSeparator() {
        final var patientId = "19991212-1212";
        final var expectedAge = getExpectedAge(patientId);
        final var tenDigitPatientIdWithoutSeparator = "9912121212";
        assertEquals(expectedAge.getYears(), calculatePatientAgeService.get(tenDigitPatientIdWithoutSeparator));
    }

    @Test
    void shouldCalculateAgeFor10DigitPatientIdWithPlusSeparator() {
        final var patientId = "19121212-1212";
        final var expectedAge = getExpectedAge(patientId);
        final var tenDigitPatientIdWithoutSeparator = "121212+1212";
        assertEquals(expectedAge.getYears(), calculatePatientAgeService.get(tenDigitPatientIdWithoutSeparator));
    }

    private static Period getExpectedAge(String patientId) {
        return Period.between(
            LocalDate.of(
                Integer.parseInt(patientId.substring(YEAR_SEPARATOR[0], YEAR_SEPARATOR[1])),
                Integer.parseInt(patientId.substring(MONTH_SEPARATOR[0], MONTH_SEPARATOR[1])),
                Integer.parseInt(patientId.substring(DAY_SEPARATOR[0], DAY_SEPARATOR[1]))),
            LocalDate.now());
    }
}
