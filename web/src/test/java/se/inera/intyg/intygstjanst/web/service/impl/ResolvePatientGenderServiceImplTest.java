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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResolvePatientGenderServiceImplTest {

    @InjectMocks
    private ResolvePatientGenderServiceImpl calculatePatientGenderService;
    private static final String MALE = "Man";
    private static final String MALE_PATIENT_ID = "191212121212";
    private static final String FEMALE = "Kvinna";
    private static final String UKNOWN = "Okänd";
    private static final String FEMALE_PATIENT_ID = "194404044444";
    private static final String UKNOWN_PATIENT_ID_EMPTY = "";
    private static final String UKNOWN_PATIENT_ID_NULL = null;
    private static final String INVALID_PATIENT_ID = "invalidPatientId";

    @Test
    void shouldReturnMale() {
        final var result = calculatePatientGenderService.get(MALE_PATIENT_ID);
        assertEquals(MALE, result);
    }

    @Test
    void shouldReturnFemale() {
        final var result = calculatePatientGenderService.get(FEMALE_PATIENT_ID);
        assertEquals(FEMALE, result);
    }

    @Test
    void shouldReturnUnknown() {
        final var result = calculatePatientGenderService.get(INVALID_PATIENT_ID);
        assertEquals(UKNOWN, result);
    }

    @Test
    void shouldReturnUknownIfPatientIdIsEmpty() {
        final var result = calculatePatientGenderService.get(UKNOWN_PATIENT_ID_EMPTY);
        assertEquals(UKNOWN, result);
    }

    @Test
    void shouldReturnUknownIfPatientIdIsNull() {
        final var result = calculatePatientGenderService.get(UKNOWN_PATIENT_ID_NULL);
        assertEquals(UKNOWN, result);
    }
}
