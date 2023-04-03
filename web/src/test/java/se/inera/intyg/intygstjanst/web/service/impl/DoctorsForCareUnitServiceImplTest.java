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
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.service.DoctorsForCareUnitService;
import se.inera.intyg.intygstjanst.web.service.HsaServiceProvider;
import se.inera.intyg.intygstjanst.web.service.SickLeaveInformationService;
import se.inera.intyg.intygstjanst.web.service.dto.PopulateFiltersRequestDTO;

@ExtendWith(MockitoExtension.class)
class DoctorsForCareUnitServiceImplTest {

    @Mock
    private SickLeaveInformationService sickLeaveInformationService;
    @Mock
    private SjukfallCertificateDao sjukfallCertificateDao;
    @Mock
    private HsaServiceProvider hsaServiceProvider;

    private DoctorsForCareUnitService doctorsForCareUnitService;
    private static final String CARE_UNIT_ID = "careUnitId";
    private static final String DOCTOR_ID = "doctorId";
    private static final String DOCTOR_NAME = "Arnold";
    private static final String ANOTHER_DOCTOR_NAME = "Ajla";
    private static final String ANOTHER_DOCTOR_ID = "anotherDoctorId";
    private static final String ANOTHER_CARE_UNIT_ID = "anotherCareUnitId";
    private static final String CARE_GIVER_HSA_ID = "careGiverHsaId";
    private static final int MAX_DAYS_SINCE_SICK_LEAVE_COMPLEDTED = 5;

    @BeforeEach
    void setUp() {
        doctorsForCareUnitService = new DoctorsForCareUnitServiceImpl(sickLeaveInformationService, sjukfallCertificateDao,
            hsaServiceProvider);
    }

    @Test
    void shouldReturnEmptyList() {
        final var populateFiltersRequestDTO = new PopulateFiltersRequestDTO();
        final var result = doctorsForCareUnitService.getActiveDoctorsForCareUnit(populateFiltersRequestDTO);
        assertEquals(0, result.size());
    }

    @Test
    void shouldReturnSortedListOfDoctorsWithActiveSickLeaves() {
        final var populateFiltersRequestDTO = new PopulateFiltersRequestDTO();
        populateFiltersRequestDTO.setCareUnitId(CARE_UNIT_ID);
        populateFiltersRequestDTO.setMaxDaysSinceSickLeaveCompleted(MAX_DAYS_SINCE_SICK_LEAVE_COMPLEDTED);

        when(hsaServiceProvider.getCareGiverHsaId(CARE_UNIT_ID)).thenReturn(CARE_GIVER_HSA_ID);
        when(hsaServiceProvider.getUnitAndRelatedSubUnits(CARE_UNIT_ID)).thenReturn(List.of(CARE_UNIT_ID, ANOTHER_CARE_UNIT_ID));
        when(sjukfallCertificateDao.findDoctorsWithActiveSickLeavesForCareUnits(CARE_GIVER_HSA_ID,
            List.of(CARE_UNIT_ID, ANOTHER_CARE_UNIT_ID), MAX_DAYS_SINCE_SICK_LEAVE_COMPLEDTED))
            .thenReturn(List.of(DOCTOR_ID, ANOTHER_DOCTOR_ID));
        when(sickLeaveInformationService.getEmployee(DOCTOR_ID)).thenReturn(Lakare.create(DOCTOR_ID, DOCTOR_NAME));
        when(sickLeaveInformationService.getEmployee(ANOTHER_DOCTOR_ID)).thenReturn(Lakare.create(ANOTHER_DOCTOR_ID, ANOTHER_DOCTOR_NAME));

        final var expectedResult = List.of(Lakare.create(ANOTHER_DOCTOR_ID, ANOTHER_DOCTOR_NAME), Lakare.create(DOCTOR_ID, DOCTOR_NAME));

        final var result = doctorsForCareUnitService.getActiveDoctorsForCareUnit(populateFiltersRequestDTO);

        assertEquals(expectedResult, result);
    }

    @Test
    void shouldDecorateWithHsaId() {
        final var populateFiltersRequestDTO = new PopulateFiltersRequestDTO();
        populateFiltersRequestDTO.setCareUnitId(CARE_UNIT_ID);
        populateFiltersRequestDTO.setMaxDaysSinceSickLeaveCompleted(MAX_DAYS_SINCE_SICK_LEAVE_COMPLEDTED);

        when(hsaServiceProvider.getCareGiverHsaId(CARE_UNIT_ID)).thenReturn(CARE_GIVER_HSA_ID);
        when(hsaServiceProvider.getUnitAndRelatedSubUnits(CARE_UNIT_ID)).thenReturn(List.of(CARE_UNIT_ID, ANOTHER_CARE_UNIT_ID));
        when(sjukfallCertificateDao.findDoctorsWithActiveSickLeavesForCareUnits(CARE_GIVER_HSA_ID,
            List.of(CARE_UNIT_ID, ANOTHER_CARE_UNIT_ID), MAX_DAYS_SINCE_SICK_LEAVE_COMPLEDTED))
            .thenReturn(List.of(DOCTOR_ID, ANOTHER_DOCTOR_ID));
        when(sickLeaveInformationService.getEmployee(DOCTOR_ID)).thenReturn(Lakare.create(DOCTOR_ID, DOCTOR_NAME));
        when(sickLeaveInformationService.getEmployee(ANOTHER_DOCTOR_ID)).thenReturn(Lakare.create(ANOTHER_DOCTOR_ID, DOCTOR_NAME));

        final var expectedResult = List.of(
            Lakare.create(ANOTHER_DOCTOR_ID, DOCTOR_NAME + " (" + ANOTHER_DOCTOR_ID + ")"),
            Lakare.create(DOCTOR_ID, DOCTOR_NAME + " (" + DOCTOR_ID + ")"));

        final var result = doctorsForCareUnitService.getActiveDoctorsForCareUnit(populateFiltersRequestDTO);

        assertEquals(expectedResult, result);
    }
}