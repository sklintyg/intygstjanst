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
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.model.PersonInformation;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaEmployeeServiceImpl;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.web.service.DecorateSickLeaveInformationService;

@ExtendWith(MockitoExtension.class)
class DecorateSickLeaveInformationServiceImplTest {

    private DecorateSickLeaveInformationService decorateSickLeaveInformationService;

    @Mock
    private HsaEmployeeServiceImpl hsaEmployeeService;

    private static final String DOCTOR_ID = "doctorId";
    private static final String ANOTHER_DOCTOR_ID = "anotherDoctorId";
    private static final String DOCTOR_NAME = "doctorName";

    @BeforeEach
    void setUp() {
        decorateSickLeaveInformationService = new DecorateSickLeaveInformationServiceImpl(hsaEmployeeService);
    }

    @Test
    void shouldUpdateEmployeeName() {
        final var sickLeaves = List.of(createSjukFallEnhet(DOCTOR_ID, DOCTOR_NAME));
        final var expectedName = getExpectedName(null);
        final var personInformation = getPersonInformation();

        when(hsaEmployeeService.getEmployee(DOCTOR_ID, null, null)).thenReturn(List.of(personInformation));

        decorateSickLeaveInformationService.decorate(sickLeaves);

        assertEquals(expectedName, sickLeaves.get(0).getLakare().getNamn());
    }

    @Test
    void shouldUpdateEmployeeNameWithHsaId() {
        final var sickLeaves = List.of(createSjukFallEnhet(DOCTOR_ID, DOCTOR_NAME));

        when(hsaEmployeeService.getEmployee(DOCTOR_ID, null, null)).thenReturn(null);

        decorateSickLeaveInformationService.decorate(sickLeaves);

        assertEquals(DOCTOR_ID, sickLeaves.get(0).getLakare().getNamn());
    }

    @Test
    void shouldUpdateDuplicatedDoctorNamesWithHsaId() {
        final var sickLeaves = List.of(createSjukFallEnhet(DOCTOR_ID, DOCTOR_NAME), createSjukFallEnhet(ANOTHER_DOCTOR_ID, DOCTOR_NAME));
        final var expectedName = getExpectedName(DOCTOR_ID);
        final var secondExpectedName = getExpectedName(ANOTHER_DOCTOR_ID);
        final var personInformation = getPersonInformation();

        when(hsaEmployeeService.getEmployee(DOCTOR_ID, null, null)).thenReturn(List.of(personInformation));
        when(hsaEmployeeService.getEmployee(ANOTHER_DOCTOR_ID, null, null)).thenReturn(List.of(personInformation));

        decorateSickLeaveInformationService.decorate(sickLeaves);

        assertEquals(expectedName, sickLeaves.get(0).getLakare().getNamn());
        assertEquals(secondExpectedName, sickLeaves.get(1).getLakare().getNamn());
    }

    @Test
    void shouldNotUpdateDuplicatedDoctorNamesWithHsaId() {
        final var sickLeaves = List.of(createSjukFallEnhet(DOCTOR_ID, DOCTOR_NAME), createSjukFallEnhet(DOCTOR_ID, DOCTOR_NAME));
        final var expectedName = getExpectedName(null);
        final var secondExpectedName = getExpectedName(null);
        final var personInformation = getPersonInformation();

        when(hsaEmployeeService.getEmployee(DOCTOR_ID, null, null)).thenReturn(List.of(personInformation));

        decorateSickLeaveInformationService.decorate(sickLeaves);

        assertEquals(expectedName, sickLeaves.get(0).getLakare().getNamn());
        assertEquals(secondExpectedName, sickLeaves.get(1).getLakare().getNamn());
    }

    private static PersonInformation getPersonInformation() {
        final var personInformation = new PersonInformation();
        personInformation.setGivenName("test");
        personInformation.setMiddleAndSurName("testsson");
        return personInformation;
    }

    private String getExpectedName(String doctorId) {
        if (doctorId != null) {
            return "test testsson (" + doctorId + ")";
        }
        return "test testsson";
    }

    private static SjukfallEnhet createSjukFallEnhet(String doctorId, String doctorName) {
        SjukfallEnhet sickLeaveUnit = new SjukfallEnhet();
        Lakare lakare = Lakare.create(doctorId, doctorName);
        sickLeaveUnit.setLakare(lakare);
        return sickLeaveUnit;
    }
}
