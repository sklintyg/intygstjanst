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

package se.inera.intyg.intygstjanst.web.integration.sickleave;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.web.service.SickLeavesForCareUnitService;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveRequestResponseDTO;

@ExtendWith(MockitoExtension.class)
class ListActiveSickLeaveControllerTest {

    @Mock
    private SickLeavesForCareUnitService sickLeavesForCareUnitService;
    private ListActiveSickLeaveController listActiveSickLeaveController;

    @BeforeEach
    void setUp() {
        listActiveSickLeaveController = new ListActiveSickLeaveController(sickLeavesForCareUnitService);
    }

    @Test
    void shouldCallGetActiveSickLeavesService() {
        final var sickLeaveRequestDTO = new SickLeaveRequestDTO();
        listActiveSickLeaveController.getActiveSickLeaves(sickLeaveRequestDTO);
        verify(sickLeavesForCareUnitService).getActiveSickLeavesForCareUnit(sickLeaveRequestDTO);
    }

    @Test
    void shouldReturnSickLeaveRequestResponseDTO() {
        final var sickLeaveRequestDTO = new SickLeaveRequestDTO();
        final var sjukfallEnhet = new SjukfallEnhet();
        final var expectedSickLeaveRequestResponseDTO = new ResponseEntity<>(new SickLeaveRequestResponseDTO(List.of(sjukfallEnhet)),
            HttpStatus.OK);
        when(sickLeavesForCareUnitService.getActiveSickLeavesForCareUnit(sickLeaveRequestDTO)).thenReturn(List.of(sjukfallEnhet));
        final var result = listActiveSickLeaveController.getActiveSickLeaves(sickLeaveRequestDTO);
        assertEquals(expectedSickLeaveRequestResponseDTO, result);
    }
}