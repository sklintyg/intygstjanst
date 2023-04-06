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
import se.inera.intyg.infra.sjukfall.dto.DiagnosKod;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.service.DoctorsForCareUnitComponent;
import se.inera.intyg.intygstjanst.web.service.HsaServiceProvider;
import se.inera.intyg.intygstjanst.web.service.PopulateFilterService;
import se.inera.intyg.intygstjanst.web.service.dto.PopulateFiltersRequestDTO;

@ExtendWith(MockitoExtension.class)
class PopulateFilterServiceImplTest {

    private static final String CARE_UNIT_ID = "careUnitId";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String ANOTHER_CARE_UNIT_ID = "anotherCareUnitId";
    private static final String CARE_GIVER_HSA_ID = "careGiverHsaId";
    private static final int MAX_DAYS_SINCE_SICK_LEAVE_COMPLEDTED = 5;
    private final List<String> unitAndRelatedSubUnits = List.of(CARE_UNIT_ID, ANOTHER_CARE_UNIT_ID);

    private static final String DIAGNOSIS_CODE_1 = "diagnosKod1";
    private static final String DIAGNOSIS_CODE_2 = "diagnosKod2";
    private static final String DOCTOR_ID = "doctorId";
    private static final String DOCTOR_NAME = "Ajla";
    private static final String ID = "id";

    @Mock
    private SjukfallCertificateDao sjukfallCertificateDao;
    @Mock
    private HsaServiceProvider hsaServiceProvider;
    @Mock
    private DoctorsForCareUnitComponent doctorsForCareUnitComponent;

    private PopulateFilterService populateFilterService;

    @BeforeEach
    void setUp() {
        populateFilterService = new PopulateFilterServiceImpl(sjukfallCertificateDao, doctorsForCareUnitComponent, hsaServiceProvider);
    }

    @Test
    void shouldReturnEmptyList() {
        final var populateFiltersRequestDTO = new PopulateFiltersRequestDTO();
        final var result = populateFilterService.populateFilters(populateFiltersRequestDTO);
        assertEquals(0, result.getActiveDoctors().size());
        assertEquals(0, result.getDiagnosisCodes().size());
    }

    @Test
    void shouldReturnListOfActiveDoctors() {
        final var populateFiltersRequestDTO = new PopulateFiltersRequestDTO();
        populateFiltersRequestDTO.setCareUnitId(CARE_UNIT_ID);
        populateFiltersRequestDTO.setMaxDaysSinceSickLeaveCompleted(MAX_DAYS_SINCE_SICK_LEAVE_COMPLEDTED);

        final var sickLeaveCertificates = List.of(getSickLeaveCertificate(null), getSickLeaveCertificate(null));
        final var expectedResult = List.of(Lakare.create(DOCTOR_ID, DOCTOR_NAME));

        when(hsaServiceProvider.getCareGiverHsaId(CARE_UNIT_ID)).thenReturn(CARE_GIVER_HSA_ID);
        when(hsaServiceProvider.getUnitAndRelatedSubUnits(CARE_UNIT_ID)).thenReturn(unitAndRelatedSubUnits);
        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(CARE_GIVER_HSA_ID, unitAndRelatedSubUnits,
            MAX_DAYS_SINCE_SICK_LEAVE_COMPLEDTED)).thenReturn(sickLeaveCertificates);
        when(doctorsForCareUnitComponent.getDoctorsForCareUnit(sickLeaveCertificates)).thenReturn(expectedResult);

        final var result = populateFilterService.populateFilters(populateFiltersRequestDTO);

        assertEquals(expectedResult, result.getActiveDoctors());
    }

    @Test
    void shouldReturnListOfDiagnosKod() {
        final var populateFiltersRequestDTO = new PopulateFiltersRequestDTO();
        populateFiltersRequestDTO.setCareUnitId(CARE_UNIT_ID);
        populateFiltersRequestDTO.setMaxDaysSinceSickLeaveCompleted(MAX_DAYS_SINCE_SICK_LEAVE_COMPLEDTED);

        final var expectedResult = List.of(DiagnosKod.create(DIAGNOSIS_CODE_1), DiagnosKod.create(DIAGNOSIS_CODE_2));
        final var sickLeaveCertificate = List.of(getSickLeaveCertificate(DIAGNOSIS_CODE_1), getSickLeaveCertificate(DIAGNOSIS_CODE_2));

        when(hsaServiceProvider.getCareGiverHsaId(CARE_UNIT_ID)).thenReturn(CARE_GIVER_HSA_ID);
        when(hsaServiceProvider.getUnitAndRelatedSubUnits(CARE_UNIT_ID)).thenReturn(unitAndRelatedSubUnits);
        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(CARE_GIVER_HSA_ID, unitAndRelatedSubUnits,
            MAX_DAYS_SINCE_SICK_LEAVE_COMPLEDTED)).thenReturn(sickLeaveCertificate);

        final var result = populateFilterService.populateFilters(populateFiltersRequestDTO);

        assertEquals(expectedResult, result.getDiagnosisCodes());
    }

    @Test
    void shouldNotReturnDuplicatedValuesOfDiagnosKod() {
        final var populateFiltersRequestDTO = new PopulateFiltersRequestDTO();
        populateFiltersRequestDTO.setCareUnitId(CARE_UNIT_ID);
        populateFiltersRequestDTO.setMaxDaysSinceSickLeaveCompleted(MAX_DAYS_SINCE_SICK_LEAVE_COMPLEDTED);

        final var expectedResult = List.of(DiagnosKod.create(DIAGNOSIS_CODE_1));
        final var sickLeaveCertificate = List.of(getSickLeaveCertificate(DIAGNOSIS_CODE_1), getSickLeaveCertificate(DIAGNOSIS_CODE_1));

        when(hsaServiceProvider.getCareGiverHsaId(CARE_UNIT_ID)).thenReturn(CARE_GIVER_HSA_ID);
        when(hsaServiceProvider.getUnitAndRelatedSubUnits(CARE_UNIT_ID)).thenReturn(unitAndRelatedSubUnits);
        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(CARE_GIVER_HSA_ID, unitAndRelatedSubUnits,
            MAX_DAYS_SINCE_SICK_LEAVE_COMPLEDTED)).thenReturn(sickLeaveCertificate);

        final var result = populateFilterService.populateFilters(populateFiltersRequestDTO);

        assertEquals(expectedResult, result.getDiagnosisCodes());
    }

    private static SjukfallCertificate getSickLeaveCertificate(String diagnosisCode) {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode(diagnosisCode);
        return sjukfallCertificate;
    }
}
