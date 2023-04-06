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

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.web.service.DoctorsForCareUnitComponent;
import se.inera.intyg.intygstjanst.web.service.SickLeaveInformationService;

@ExtendWith(MockitoExtension.class)
class DoctorsForCareUnitComponentImplTest {

    @Mock
    private SickLeaveInformationService sickLeaveInformationService;
    private DoctorsForCareUnitComponent doctorsForCareUnitComponent;
    private static final String DOCTOR_ID = "doctorId";
    private static final String DOCTOR_NAME = "Bosse";
    private static final String ANOTHER_DOCTOR_NAME = "Ajla";
    private static final String ANOTHER_DOCTOR_ID = "anotherDoctorId";

    @BeforeEach
    void setUp() {
        doctorsForCareUnitComponent = new DoctorsForCareUnitComponentImpl(sickLeaveInformationService);
    }

    @Test
    void shouldReturnEmptyList() {
        final var sickLeaveCertificates = List.of(new SjukfallCertificate(null));
        final var result = doctorsForCareUnitComponent.getDoctorsForCareUnit(sickLeaveCertificates, null);
        assertEquals(0, result.size());
    }

    @Test
    void shouldReturnSortedListOfDoctorsWithActiveSickLeaves() {
        final var sickLeaveCertificates = List.of(getSjukfallCertificate(DOCTOR_ID), getSjukfallCertificate(ANOTHER_DOCTOR_ID));

        when(sickLeaveInformationService.getEmployee(DOCTOR_ID)).thenReturn(Lakare.create(DOCTOR_ID, DOCTOR_NAME));
        when(sickLeaveInformationService.getEmployee(ANOTHER_DOCTOR_ID)).thenReturn(Lakare.create(ANOTHER_DOCTOR_ID, ANOTHER_DOCTOR_NAME));

        final var expectedResult = List.of(Lakare.create(ANOTHER_DOCTOR_ID, ANOTHER_DOCTOR_NAME), Lakare.create(DOCTOR_ID, DOCTOR_NAME));

        final var result = doctorsForCareUnitComponent.getDoctorsForCareUnit(sickLeaveCertificates, null);

        assertEquals(expectedResult, result);
    }

    @Test
    void shouldDecorateWithHsaId() {
        final var sickLeaveCertificates = List.of(getSjukfallCertificate(DOCTOR_ID), getSjukfallCertificate(ANOTHER_DOCTOR_ID));

        when(sickLeaveInformationService.getEmployee(DOCTOR_ID)).thenReturn(Lakare.create(DOCTOR_ID, DOCTOR_NAME));
        when(sickLeaveInformationService.getEmployee(ANOTHER_DOCTOR_ID)).thenReturn(Lakare.create(ANOTHER_DOCTOR_ID, DOCTOR_NAME));

        final var expectedResult = List.of(
            Lakare.create(DOCTOR_ID, DOCTOR_NAME + " (" + DOCTOR_ID + ")"),
            Lakare.create(ANOTHER_DOCTOR_ID, DOCTOR_NAME + " (" + ANOTHER_DOCTOR_ID + ")"));

        final var result = doctorsForCareUnitComponent.getDoctorsForCareUnit(sickLeaveCertificates, null);

        assertEquals(expectedResult, result);
    }

    @Test
    void shouldRemoveDuplicatedDoctors() {
        final var sickLeaveCertificates = List.of(getSjukfallCertificate(DOCTOR_ID), getSjukfallCertificate(DOCTOR_ID));

        when(sickLeaveInformationService.getEmployee(DOCTOR_ID)).thenReturn(Lakare.create(DOCTOR_ID, DOCTOR_NAME));
        when(sickLeaveInformationService.getEmployee(DOCTOR_ID)).thenReturn(Lakare.create(DOCTOR_ID, DOCTOR_NAME));

        final var expectedResult = List.of(Lakare.create(DOCTOR_ID, DOCTOR_NAME));

        final var result = doctorsForCareUnitComponent.getDoctorsForCareUnit(sickLeaveCertificates, null);

        assertEquals(expectedResult, result);
    }

    @Test
    void shouldReturnEmptyListIfDoctorIdIsProvided() {
        final var sickLeaveCertificates = List.of(getSjukfallCertificate(DOCTOR_ID), getSjukfallCertificate(DOCTOR_ID));

        final var expectedResult = Collections.emptyList();

        final var result = doctorsForCareUnitComponent.getDoctorsForCareUnit(sickLeaveCertificates, DOCTOR_ID);

        assertEquals(expectedResult, result);
    }

    private SjukfallCertificate getSjukfallCertificate(String id) {
        final var sjukfallCertificate = new SjukfallCertificate(id);
        sjukfallCertificate.setSigningDoctorId(id);
        return sjukfallCertificate;
    }
}