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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateWorkCapacity;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.inera.intyg.intygstjanst.web.integration.rehabstod.converter.SjukfallCertificateIntygsDataConverter;
import se.inera.intyg.intygstjanst.web.integration.sickleave.converter.IntygsDataConverter;

@ExtendWith(MockitoExtension.class)
class IntygDataServiceImplTest {

    private static final int MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED = 5;
    private static final String UNIT_ID = "unitId";
    private final ArrayList<String> hsaIdList = new ArrayList<>();
    @Mock
    private HsaService hsaService;
    @Mock
    private SjukfallCertificateDao sjukfallCertificateDao;
    @Mock
    private IntygsDataConverter intygDataConverter;
    private IntygDataServiceImpl listActiveSickLeaveCertificateService;
    private static final LocalDateTime CERT_SIGNING_DATETIME = LocalDateTime.parse("2016-02-01T15:00:00");
    private static final String DOCTOR_HSA_ID = "doctor-1";
    private static final String DOCTOR_NAME = "doctor-1-name";
    private static final String AG1_14 = "ag1-14";
    private static final String DOCTOR_ID = "caregiver-1";
    private static final String CARE_UNIT_ID = "careunit-1";
    private static final String CARE_UNIT_NAME = "careunit-1-name";
    private static final String TOLVAN_TOLVANSSON = "Tolvan Tolvansson";
    private static final String TOLVAN_TOLVANSSON_PNR = "19121212-1212";

    @BeforeEach
    void setUp() {
        listActiveSickLeaveCertificateService = new IntygDataServiceImpl(hsaService, sjukfallCertificateDao,
            intygDataConverter);
    }

    @Test
    void shouldReturnListOfIntygData() {
        when(hsaService.getHsaIdForVardgivare(UNIT_ID)).thenReturn(UNIT_ID);
        when(hsaService.getHsaIdForUnderenheter(anyString())).thenReturn(hsaIdList);
        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(anyString(), anyList(),
            anyInt())).thenReturn(List.of(buildSjukfallCertificate(false), buildSjukfallCertificate(false)));

        final var result = listActiveSickLeaveCertificateService.getIntygData(UNIT_ID, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);
        assertEquals(2, result.size());
    }

    @Test
    void shouldConvertToListOfIntygData() {
        final var sjukfallCertificates = List.of(buildSjukfallCertificate(false), buildSjukfallCertificate(false));
        final var intygsData = new ArrayList<>(
            new SjukfallCertificateIntygsDataConverter().buildIntygsData(sjukfallCertificates));
        final var expectedIntygData = intygsData.stream().map(intygDataConverter::map).collect(Collectors.toList());
        when(hsaService.getHsaIdForVardgivare(UNIT_ID)).thenReturn(UNIT_ID);
        when(hsaService.getHsaIdForUnderenheter(anyString())).thenReturn(hsaIdList);
        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(anyString(), anyList(),
            anyInt())).thenReturn(List.of(buildSjukfallCertificate(false), buildSjukfallCertificate(false)));

        final var result = listActiveSickLeaveCertificateService.getIntygData(UNIT_ID, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);
        assertIterableEquals(expectedIntygData, result);
    }

    @Test
    void shouldNotReturnIntygDataIfTestCertificate() {
        when(hsaService.getHsaIdForVardgivare(UNIT_ID)).thenReturn(UNIT_ID);
        when(hsaService.getHsaIdForUnderenheter(anyString())).thenReturn(hsaIdList);
        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(anyString(), anyList(),
            anyInt())).thenReturn(List.of(buildSjukfallCertificate(false), buildSjukfallCertificate(true)));

        final var result = listActiveSickLeaveCertificateService.getIntygData(UNIT_ID, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);
        assertEquals(1, result.size());
    }

    private SjukfallCertificate buildSjukfallCertificate(boolean testCertificate) {

        SjukfallCertificate sc = new SjukfallCertificate(UUID.randomUUID().toString());
        sc.setCareGiverId(DOCTOR_ID);
        sc.setCareUnitId(CARE_UNIT_ID);
        sc.setCareUnitName(CARE_UNIT_NAME);
        sc.setSigningDateTime(CERT_SIGNING_DATETIME);
        sc.setSjukfallCertificateWorkCapacity(defaultWorkCapacities());
        sc.setCivicRegistrationNumber(TOLVAN_TOLVANSSON_PNR);
        sc.setDiagnoseCode("M16");
        sc.setPatientName(TOLVAN_TOLVANSSON);
        sc.setSigningDoctorId(DOCTOR_HSA_ID);
        sc.setSigningDoctorName(DOCTOR_NAME);
        sc.setType(AG1_14);
        sc.setDeleted(false);
        sc.setEmployment("STUDERANDE,ARBETSSOKANDE");
        sc.setBiDiagnoseCode1("J21");
        sc.setBiDiagnoseCode2("J22");
        sc.setTestCertificate(testCertificate);
        return sc;
    }

    private List<SjukfallCertificateWorkCapacity> defaultWorkCapacities() {
        List<SjukfallCertificateWorkCapacity> workCapacities = new ArrayList<>();
        SjukfallCertificateWorkCapacity wc = new SjukfallCertificateWorkCapacity();

        wc.setCapacityPercentage(100);
        wc.setFromDate(LocalDate.now().minusWeeks(1).format(DateTimeFormatter.ISO_DATE));
        wc.setToDate(LocalDate.now().plusWeeks(1).format(DateTimeFormatter.ISO_DATE));
        workCapacities.add(wc);

        SjukfallCertificateWorkCapacity wc2 = new SjukfallCertificateWorkCapacity();
        wc2.setCapacityPercentage(75);
        wc2.setFromDate(LocalDate.now().minusWeeks(3).format(DateTimeFormatter.ISO_DATE));
        wc2.setToDate(LocalDate.now().minusWeeks(1).format(DateTimeFormatter.ISO_DATE));
        workCapacities.add(wc2);

        SjukfallCertificateWorkCapacity wc3 = new SjukfallCertificateWorkCapacity();
        wc3.setCapacityPercentage(50);
        wc3.setFromDate(LocalDate.now().minusWeeks(4).format(DateTimeFormatter.ISO_DATE));
        wc3.setToDate(LocalDate.now().minusWeeks(3).format(DateTimeFormatter.ISO_DATE));
        workCapacities.add(wc3);
        return workCapacities;
    }
}
