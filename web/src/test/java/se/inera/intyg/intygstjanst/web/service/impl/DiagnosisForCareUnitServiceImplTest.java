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

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKod;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.web.service.DiagnosisForCareUnitService;

class DiagnosisForCareUnitServiceImplTest {

    private DiagnosisForCareUnitService diagnosisForCareUnitService;
    private static final String DIAGNOSIS_CODE_1 = "diagnosKod1";
    private static final String DIAGNOSIS_CODE_2 = "diagnosKod2";
    private static final String ID = "id";

    @BeforeEach
    void setUp() {
        diagnosisForCareUnitService = new DiagnosisForCareUnitServiceImpl();
    }

    @Test
    void shouldReturnListOfDiagnosKod() {
        final var expectedResult = List.of(DiagnosKod.create(DIAGNOSIS_CODE_1), DiagnosKod.create(DIAGNOSIS_CODE_2));
        final var sickLeaveCertificate = List.of(getSickLeaveCertificate(DIAGNOSIS_CODE_1), getSickLeaveCertificate(DIAGNOSIS_CODE_2));
        final var result = diagnosisForCareUnitService.getDiagnosisForCareUnit(sickLeaveCertificate);
        assertEquals(expectedResult, result);
    }

    @Test
    void shouldNotReturnDuplicatedValuesOfDiagnosKod() {
        final var expectedResult = List.of(DiagnosKod.create(DIAGNOSIS_CODE_1));
        final var sickLeaveCertificate = List.of(getSickLeaveCertificate(DIAGNOSIS_CODE_1), getSickLeaveCertificate(DIAGNOSIS_CODE_1));
        final var result = diagnosisForCareUnitService.getDiagnosisForCareUnit(sickLeaveCertificate);
        assertEquals(expectedResult, result);
    }

    private static SjukfallCertificate getSickLeaveCertificate(String diagnosisCode) {
        final var sjukfallCertificate = new SjukfallCertificate(ID);
        sjukfallCertificate.setDiagnoseCode(diagnosisCode);
        return sjukfallCertificate;
    }
}