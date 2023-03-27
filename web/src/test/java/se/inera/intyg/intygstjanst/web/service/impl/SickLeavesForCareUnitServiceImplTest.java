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
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.dto.Vardenhet;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.intygstjanst.web.service.ListActiveSickLeaveCertificateService;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveRequestDTO;

@ExtendWith(MockitoExtension.class)
class SickLeavesForCareUnitServiceImplTest {

    @Mock
    private SjukfallEngineService sjukfallEngine;
    @Mock
    private ListActiveSickLeaveCertificateService listActiveSickLeaveCertificateService;

    private SickLeavesForCareUnitServiceImpl sickLeavesForCareUnitService;

    private static final String DOCTOR_ID = "doctorId";
    private static final String ANOTHER_DOCTOR_ID = "anotherDoctorId";
    private static final String CARE_UNIT_ID = "careUnitId";
    private static final String ANOTHER_CARE_UNIT_ID = "anotherCareUnitId";
    private static final String UNIT_ID = "unitId";


    @BeforeEach
    void setUp() {
        sickLeavesForCareUnitService = new SickLeavesForCareUnitServiceImpl(sjukfallEngine, listActiveSickLeaveCertificateService);
    }

    @Test
    void shouldReturnSjukfallEnhetList() {
        final var sickLeaveRequestDTO = getSickLeaveRequestDTO(null);
        final var expectedSickLeave = List.of(createSjukFallEnhet(DOCTOR_ID, CARE_UNIT_ID));
        when(sjukfallEngine.beraknaSjukfallForEnhet(anyList(), any())).thenReturn(expectedSickLeave);
        final var result = sickLeavesForCareUnitService.getActiveSickLeavesForCareUnit(sickLeaveRequestDTO);
        assertIterableEquals(expectedSickLeave, result);
    }

    @Test
    void shouldReturnSjukfallEnhetListSortedOnDoctorId() {
        final var sickLeaveRequestDTO = getSickLeaveRequestDTO(null);
        final var expectedSickLeave = createSjukFallEnhet(DOCTOR_ID, CARE_UNIT_ID);
        final var sickLeaves = List.of(expectedSickLeave, createSjukFallEnhet(ANOTHER_DOCTOR_ID, CARE_UNIT_ID));
        when(sjukfallEngine.beraknaSjukfallForEnhet(anyList(), any())).thenReturn(sickLeaves);
        final var result = sickLeavesForCareUnitService.getActiveSickLeavesForCareUnit(sickLeaveRequestDTO);
        assertEquals(expectedSickLeave, result.get(0));
        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnSjukfallEnhetListSortedOnCareUnit() {
        final var sickLeaveRequestDTO = getSickLeaveRequestDTO(CARE_UNIT_ID);
        final var expectedSickLeave = createSjukFallEnhet(DOCTOR_ID, CARE_UNIT_ID);
        final var sickLeaves = List.of(expectedSickLeave, createSjukFallEnhet(DOCTOR_ID, ANOTHER_CARE_UNIT_ID));
        when(sjukfallEngine.beraknaSjukfallForEnhet(anyList(), any())).thenReturn(sickLeaves);
        final var result = sickLeavesForCareUnitService.getActiveSickLeavesForCareUnit(sickLeaveRequestDTO);
        assertEquals(expectedSickLeave, result.get(0));
        assertEquals(1, result.size());
    }

    private static SickLeaveRequestDTO getSickLeaveRequestDTO(String careUnitId) {
        final var sickLeaveRequestDTO = new SickLeaveRequestDTO();
        sickLeaveRequestDTO.setMaxCertificateGap(5);
        sickLeaveRequestDTO.setMaxDaysSinceSickLeaveCompleted(5);
        sickLeaveRequestDTO.setUnitId(UNIT_ID);
        sickLeaveRequestDTO.setDoctorId(DOCTOR_ID);
        sickLeaveRequestDTO.setCareUnitId(careUnitId);
        return sickLeaveRequestDTO;
    }

    private static SjukfallEnhet createSjukFallEnhet(String doctorId, String careUnitId) {
        SjukfallEnhet sickLeaveUnit = new SjukfallEnhet();
        Lakare lakare = new Lakare(doctorId, null);
        sickLeaveUnit.setLakare(lakare);
        sickLeaveUnit.setVardenhet(
            new Vardenhet(careUnitId, null)
        );

        return sickLeaveUnit;
    }
}
