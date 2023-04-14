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
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKapitel;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.web.service.PopulateFilterService;
import se.inera.intyg.intygstjanst.web.service.SickLeavesForCareUnitService;
import se.inera.intyg.intygstjanst.web.service.dto.PopulateFiltersRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.PopulateFiltersResponseDTO;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveResponseDTO;

@ExtendWith(MockitoExtension.class)
class SickLeaveControllerTest {

    @Mock
    private SickLeavesForCareUnitService sickLeavesForCareUnitService;
    @Mock
    private PopulateFilterService populateFilterService;
    private SickLeaveController sickLeaveController;

    private static final String DOCTOR_ID = "doctorID";
    private static final String DOCTOR_NAME = "doctorName";
    private static final String DIAGNOSIS_CHAPTER = "C00-D48Tumörer";

    @BeforeEach
    void setUp() {
        sickLeaveController = new SickLeaveController(sickLeavesForCareUnitService, populateFilterService);
    }

    @Test
    void shouldCallGetActiveSickLeavesService() {
        final var sickLeaveRequestDTO = new SickLeaveRequestDTO();
        sickLeaveController.getActiveSickLeavesForCareUnit(sickLeaveRequestDTO);
        verify(sickLeavesForCareUnitService).getActiveSickLeavesForCareUnit(sickLeaveRequestDTO);
    }

    @Test
    void shouldCallPopulateFilterSerivceService() {
        final var populateFiltersRequestDTO = new PopulateFiltersRequestDTO();
        sickLeaveController.populateFilters(populateFiltersRequestDTO);
        verify(populateFilterService).populateFilters(populateFiltersRequestDTO);
    }

    @Test
    void shouldReturnSickLeaveRequestResponseDTO() {
        final var sickLeaveRequestDTO = new SickLeaveRequestDTO();
        final var sjukfallEnhet = new SjukfallEnhet();
        final var expectedResponse = Response.ok(new SickLeaveResponseDTO(List.of(sjukfallEnhet))).build();
        when(sickLeavesForCareUnitService.getActiveSickLeavesForCareUnit(sickLeaveRequestDTO)).thenReturn(List.of(sjukfallEnhet));
        final var result = sickLeaveController.getActiveSickLeavesForCareUnit(sickLeaveRequestDTO);
        assertEquals(expectedResponse.getEntity(), result.getEntity());
    }

    @Test
    void shouldReturnPopulateFiltersResonseDTO() {
        final var populateFiltersRequestDTO = new PopulateFiltersRequestDTO();
        final var doctor = Lakare.create(DOCTOR_ID, DOCTOR_NAME);
        final var diagnosisChapter = new DiagnosKapitel(DIAGNOSIS_CHAPTER);
        final var expectedResponse = Response.ok(new PopulateFiltersResponseDTO(List.of(doctor), List.of(diagnosisChapter))).build();

        when(populateFilterService.populateFilters(populateFiltersRequestDTO)).thenReturn(
            new PopulateFiltersResponseDTO(List.of(doctor), List.of(diagnosisChapter)));

        final var result = sickLeaveController.populateFilters(populateFiltersRequestDTO);

        assertEquals(expectedResponse.getEntity(), result.getEntity());
    }
}
