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
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.inera.intyg.intygstjanst.web.service.SickLeaveInformationService;

@ExtendWith(MockitoExtension.class)
class SickLeaveInformationServiceImplTest {

    private SickLeaveInformationService sickLeaveInformationService;

    @Mock
    private HsaService hsaService;

    private static final String DOCTOR_ID = "doctorId";
    private static final String ANOTHER_DOCTOR_ID = "anotherDoctorId";
    private static final String DOCTOR_NAME = "Arnold Johansson";
    private static final String ANOTHER_DOCTOR_NAME = "Ajla Doktor";

    @BeforeEach
    void setUp() {
        sickLeaveInformationService = new SickLeaveInformationServiceImpl(hsaService);
    }

    @Test
    void shouldUpdateEmployeeName() {
        final var sickLeaves = List.of(createSjukFallEnhet(DOCTOR_ID));
        final var expectedName = getExpectedName(null, DOCTOR_NAME);

        when(hsaService.getHsaEmployeeName(DOCTOR_ID)).thenReturn(expectedName);

        sickLeaveInformationService.updateAndDecorateDoctorName(sickLeaves);

        assertEquals(expectedName, sickLeaves.get(0).getLakare().getNamn());
    }

    @Test
    void shouldUpdateEmployeeNameWithHsaIdIfNull() {
        final var sickLeaves = List.of(createSjukFallEnhet(DOCTOR_ID));

        when(hsaService.getHsaEmployeeName(DOCTOR_ID)).thenReturn(null);

        sickLeaveInformationService.updateAndDecorateDoctorName(sickLeaves);

        assertEquals(DOCTOR_ID, sickLeaves.get(0).getLakare().getNamn());
    }

    @Test
    void shouldUpdateDuplicatedDoctorNamesWithHsaId() {
        final var sickLeaves = List.of(createSjukFallEnhet(DOCTOR_ID), createSjukFallEnhet(ANOTHER_DOCTOR_ID));
        final var expectedName = getExpectedName(DOCTOR_ID, DOCTOR_NAME);
        final var secondExpectedName = getExpectedName(ANOTHER_DOCTOR_ID, DOCTOR_NAME);

        when(hsaService.getHsaEmployeeName(DOCTOR_ID)).thenReturn(DOCTOR_NAME);
        when(hsaService.getHsaEmployeeName(ANOTHER_DOCTOR_ID)).thenReturn(DOCTOR_NAME);

        sickLeaveInformationService.updateAndDecorateDoctorName(sickLeaves);

        assertEquals(expectedName, sickLeaves.get(0).getLakare().getNamn());
        assertEquals(secondExpectedName, sickLeaves.get(1).getLakare().getNamn());
    }

    @Test
    void shouldNotUpdateDuplicatedDoctorNamesWithHsaId() {
        final var sickLeaves = List.of(createSjukFallEnhet(DOCTOR_ID), createSjukFallEnhet(ANOTHER_DOCTOR_ID));
        final var expectedName = getExpectedName(null, DOCTOR_NAME);
        final var secondExpectedName = getExpectedName(null, ANOTHER_DOCTOR_NAME);

        when(hsaService.getHsaEmployeeName(DOCTOR_ID)).thenReturn(DOCTOR_NAME);
        when(hsaService.getHsaEmployeeName(ANOTHER_DOCTOR_ID)).thenReturn(ANOTHER_DOCTOR_NAME);

        sickLeaveInformationService.updateAndDecorateDoctorName(sickLeaves);

        assertEquals(expectedName, sickLeaves.get(0).getLakare().getNamn());
        assertEquals(secondExpectedName, sickLeaves.get(1).getLakare().getNamn());
    }

    private String getExpectedName(String doctorId, String doctorName) {
        if (doctorId != null) {
            return doctorName + " (" + doctorId + ")";
        }
        return doctorName;
    }

    private static SjukfallEnhet createSjukFallEnhet(String doctorId) {
        SjukfallEnhet sickLeaveUnit = new SjukfallEnhet();
        Lakare lakare = Lakare.create(doctorId, DOCTOR_NAME);
        sickLeaveUnit.setLakare(lakare);
        return sickLeaveUnit;
    }
}
