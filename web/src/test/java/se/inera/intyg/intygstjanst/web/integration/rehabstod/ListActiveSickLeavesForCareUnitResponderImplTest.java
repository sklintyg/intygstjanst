/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integration.rehabstod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponseType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ResultCodeEnum;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;

/**
 * Created by eriklupander on 2016-02-04.
 */
@RunWith(MockitoJUnitRunner.class)
public class ListActiveSickLeavesForCareUnitResponderImplTest {

    private static final String CAREGIVER_HSAID = "vardgivare-1";
    private static final String CAREUNIT_HSAID = "enhet-1";
    private static final int MAX_DAGAR_SEDAN_AVSLUT = 0;

    @Mock
    private HsaService hsaService;

    @Mock
    private SjukfallCertificateDao sjukfallCertificateDao;

    @InjectMocks
    private ListActiveSickLeavesForCareUnitResponderImpl testee;

    private HsaId hsaId = new HsaId();

    @Before
    public void init() {
        hsaId.setExtension(CAREUNIT_HSAID);
    }

    @Test
    public void testWithNoCareUnitId() {
        ListActiveSickLeavesForCareUnitType params = new ListActiveSickLeavesForCareUnitType();
        ListActiveSickLeavesForCareUnitResponseType responseType = testee.listActiveSickLeavesForCareUnit("", params);
        assertNotNull(responseType);
        assertEquals(ResultCodeEnum.ERROR, responseType.getResultCode());
    }

    @Test
    public void testNormalHappyPath() {
        when(hsaService.getHsaIdForVardgivare(CAREUNIT_HSAID)).thenReturn(CAREGIVER_HSAID);
        when(hsaService.getHsaIdForUnderenheter(CAREUNIT_HSAID)).thenReturn(new ArrayList<>());

        ListActiveSickLeavesForCareUnitType params = new ListActiveSickLeavesForCareUnitType();
        params.setEnhetsId(hsaId);
        params.setMaxDagarSedanAvslut(0);

        ListActiveSickLeavesForCareUnitResponseType responseType = testee.listActiveSickLeavesForCareUnit("", params);
        assertNotNull(responseType);
        assertEquals(ResultCodeEnum.OK, responseType.getResultCode());
    }

    @Test
    public void testHandlesPersonnummerWithoutDash() {
        when(hsaService.getHsaIdForVardgivare(CAREUNIT_HSAID)).thenReturn(CAREGIVER_HSAID);
        when(hsaService.getHsaIdForUnderenheter(CAREUNIT_HSAID)).thenReturn(new ArrayList<>());

        ListActiveSickLeavesForCareUnitType params = new ListActiveSickLeavesForCareUnitType();
        params.setEnhetsId(hsaId);
        PersonId patientId = new PersonId();
        patientId.setExtension("191212121212");
        params.setPersonId(patientId);
        params.setMaxDagarSedanAvslut(MAX_DAGAR_SEDAN_AVSLUT);

        ListActiveSickLeavesForCareUnitResponseType responseType = testee.listActiveSickLeavesForCareUnit("", params);
        assertNotNull(responseType);
        assertEquals(ResultCodeEnum.OK, responseType.getResultCode());

        verify(sjukfallCertificateDao, times(1))
            .findActiveSjukfallCertificateForPersonOnCareUnits(CAREGIVER_HSAID, Arrays.asList(CAREUNIT_HSAID),
                "19121212-1212", MAX_DAGAR_SEDAN_AVSLUT);
    }

    @Test
    public void testNullPatientIdExtensionRunsExpectedDaoMethod() {
        when(hsaService.getHsaIdForVardgivare(CAREUNIT_HSAID)).thenReturn(CAREGIVER_HSAID);
        when(hsaService.getHsaIdForUnderenheter(CAREUNIT_HSAID)).thenReturn(new ArrayList<>());

        ListActiveSickLeavesForCareUnitType params = new ListActiveSickLeavesForCareUnitType();
        params.setEnhetsId(hsaId);
        PersonId patientId = new PersonId();
        patientId.setExtension(null);
        params.setPersonId(patientId);

        ListActiveSickLeavesForCareUnitResponseType responseType = testee.listActiveSickLeavesForCareUnit("", params);
        verify(sjukfallCertificateDao, times(1)).findActiveSjukfallCertificateForCareUnits(anyString(), anyList(), anyInt());
        verify(sjukfallCertificateDao, times(0))
            .findActiveSjukfallCertificateForPersonOnCareUnits(anyString(), anyList(), anyString(), anyInt());
    }

    @Test
    public void testEmptyStringPatientIdExtensionRunsExpectedDaoMethod() {
        when(hsaService.getHsaIdForVardgivare(CAREUNIT_HSAID)).thenReturn(CAREGIVER_HSAID);
        when(hsaService.getHsaIdForUnderenheter(CAREUNIT_HSAID)).thenReturn(new ArrayList<>());

        ListActiveSickLeavesForCareUnitType params = new ListActiveSickLeavesForCareUnitType();
        params.setEnhetsId(hsaId);
        PersonId patientId = new PersonId();
        patientId.setExtension(" ");
        params.setPersonId(patientId);

        ListActiveSickLeavesForCareUnitResponseType responseType = testee.listActiveSickLeavesForCareUnit("", params);
        verify(sjukfallCertificateDao, times(1)).findActiveSjukfallCertificateForCareUnits(anyString(), anyList(), anyInt());
        verify(sjukfallCertificateDao, times(0))
            .findActiveSjukfallCertificateForPersonOnCareUnits(anyString(), anyList(), anyString(), anyInt());
    }

    @Test
    public void testFilteringOfTestCertificates() {
        final SjukfallCertificate realSjukfallCertificate = new SjukfallCertificate("realSjukfallCertificateId");
        final SjukfallCertificate testSjukfallCertificate = new SjukfallCertificate("testSjukfallCertificateId");
        testSjukfallCertificate.setTestCertificate(true);
        final List<SjukfallCertificate> sjukfallCertificateList = Arrays.asList(realSjukfallCertificate, testSjukfallCertificate);

        when(hsaService.getHsaIdForVardgivare(CAREUNIT_HSAID)).thenReturn(CAREGIVER_HSAID);
        when(hsaService.getHsaIdForUnderenheter(CAREUNIT_HSAID)).thenReturn(new ArrayList<>());
        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(anyString(), anyList(), anyInt()))
            .thenReturn(sjukfallCertificateList);

        ListActiveSickLeavesForCareUnitType params = new ListActiveSickLeavesForCareUnitType();
        params.setEnhetsId(hsaId);
        PersonId patientId = new PersonId();
        patientId.setExtension(" ");
        params.setPersonId(patientId);

        ListActiveSickLeavesForCareUnitResponseType responseType = testee.listActiveSickLeavesForCareUnit("", params);

        assertNotNull(responseType);
        assertEquals(ResultCodeEnum.OK, responseType.getResultCode());
        assertEquals(1, responseType.getIntygsLista().getIntygsData().size());
        assertEquals("realSjukfallCertificateId", responseType.getIntygsLista().getIntygsData().get(0).getIntygsId());
    }
}
