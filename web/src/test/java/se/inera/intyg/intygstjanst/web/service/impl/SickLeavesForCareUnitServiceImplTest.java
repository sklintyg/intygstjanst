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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.dto.Vardenhet;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.intygstjanst.web.service.IntygDataService;
import se.inera.intyg.intygstjanst.web.service.SickLeaveInformationService;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveRequestDTO;

@ExtendWith(MockitoExtension.class)
class SickLeavesForCareUnitServiceImplTest {

    @Mock
    private SjukfallEngineService sjukfallEngine;
    @Mock
    private IntygDataService intygDataService;
    @Mock
    private SickLeaveInformationService sickLeaveInformationService;
    private SickLeavesForCareUnitServiceImpl sickLeavesForCareUnitService;

    private static final String DOCTOR_ID = "doctorId";
    private static final String ANOTHER_DOCTOR_ID = "anotherDoctorId";
    private static final String CARE_UNIT_ID = "careUnitId";
    private static final String ANOTHER_UNIT_ID = "anotherUnitId";
    private static final String UNIT_ID = "unitId";
    private List<IntygData> intygData;


    @BeforeEach
    void setUp() {
        sickLeavesForCareUnitService = new SickLeavesForCareUnitServiceImpl(sjukfallEngine, intygDataService,
            sickLeaveInformationService);
        intygData = List.of(new IntygData(), new IntygData());
    }

    @Test
    void shouldReturnSjukfallEnhetList() {
        final var sickLeaveRequestDTO = getSickLeaveRequestDTO(null, null, CARE_UNIT_ID);
        final var expectedSickLeave = List.of(createSjukFallEnhet(null, null));
        when(intygDataService.getIntygData(sickLeaveRequestDTO.getCareUnitId(),
            sickLeaveRequestDTO.getMaxDaysSinceSickLeaveCompleted())).thenReturn(intygData);
        when(sjukfallEngine.beraknaSjukfallForEnhet(eq(intygData), any())).thenReturn(expectedSickLeave);
        final var result = sickLeavesForCareUnitService.getActiveSickLeavesForCareUnit(sickLeaveRequestDTO);
        verify(sickLeaveInformationService).updateAndDecorateDoctorName(expectedSickLeave);
        assertIterableEquals(expectedSickLeave, result);
    }

    @Test
    void shouldReturnSjukfallEnhetListFilteredOnDoctorId() {
        final var sickLeaveRequestDTO = getSickLeaveRequestDTO(null, DOCTOR_ID, CARE_UNIT_ID);
        final var expectedSickLeave = createSjukFallEnhet(DOCTOR_ID, null);
        final var sickLeaves = List.of(expectedSickLeave, createSjukFallEnhet(ANOTHER_DOCTOR_ID, UNIT_ID));
        when(intygDataService.getIntygData(sickLeaveRequestDTO.getCareUnitId(),
            sickLeaveRequestDTO.getMaxDaysSinceSickLeaveCompleted())).thenReturn(intygData);
        when(sjukfallEngine.beraknaSjukfallForEnhet(eq(intygData), any())).thenReturn(sickLeaves);
        final var result = sickLeavesForCareUnitService.getActiveSickLeavesForCareUnit(sickLeaveRequestDTO);
        verify(sickLeaveInformationService).updateAndDecorateDoctorName(anyList());
        assertEquals(1, result.size());
        assertEquals(expectedSickLeave, result.get(0));
    }

    @Test
    void shouldReturnSjukfallEnhetListFiltereddOnCareUnit() {
        final var sickLeaveRequestDTO = getSickLeaveRequestDTO(UNIT_ID, null, CARE_UNIT_ID);
        final var expectedSickLeave = createSjukFallEnhet(null, UNIT_ID);
        final var sickLeaves = List.of(expectedSickLeave, createSjukFallEnhet(DOCTOR_ID, ANOTHER_UNIT_ID));
        when(intygDataService.getIntygData(sickLeaveRequestDTO.getCareUnitId(),
            sickLeaveRequestDTO.getMaxDaysSinceSickLeaveCompleted())).thenReturn(intygData);
        when(sjukfallEngine.beraknaSjukfallForEnhet(eq(intygData), any())).thenReturn(sickLeaves);
        final var result = sickLeavesForCareUnitService.getActiveSickLeavesForCareUnit(sickLeaveRequestDTO);
        verify(sickLeaveInformationService).updateAndDecorateDoctorName(anyList());
        assertEquals(1, result.size());
        assertEquals(expectedSickLeave, result.get(0));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionIfNoCareUnit() {
        final var sickLeaveRequestDTO = getSickLeaveRequestDTO(null, null, null);
        assertThrows(IllegalArgumentException.class,
            () -> sickLeavesForCareUnitService.getActiveSickLeavesForCareUnit(sickLeaveRequestDTO));
    }

    private static SickLeaveRequestDTO getSickLeaveRequestDTO(String unitId, String doctorId, String careUnitId) {
        final var sickLeaveRequestDTO = new SickLeaveRequestDTO();
        sickLeaveRequestDTO.setMaxCertificateGap(5);
        sickLeaveRequestDTO.setMaxDaysSinceSickLeaveCompleted(5);
        sickLeaveRequestDTO.setUnitId(unitId);
        sickLeaveRequestDTO.setDoctorId(doctorId);
        sickLeaveRequestDTO.setCareUnitId(careUnitId);
        return sickLeaveRequestDTO;
    }

    private static SjukfallEnhet createSjukFallEnhet(String doctorId, String unitId) {
        SjukfallEnhet sickLeaveUnit = new SjukfallEnhet();
        Lakare lakare = Lakare.create(doctorId, null);
        sickLeaveUnit.setLakare(lakare);
        sickLeaveUnit.setVardenhet(
            Vardenhet.create(unitId, null)
        );

        return sickLeaveUnit;
    }
}
