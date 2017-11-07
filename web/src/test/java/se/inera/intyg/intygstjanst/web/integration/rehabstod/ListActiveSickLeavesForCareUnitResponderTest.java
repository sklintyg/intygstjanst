/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitResponseType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ListActiveSickLeavesForCareUnitType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listactivesickleavesforcareunit.v1.ResultCodeEnum;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2016-02-04.
 */
@RunWith(MockitoJUnitRunner.class)
public class ListActiveSickLeavesForCareUnitResponderTest {

    private static final String CAREGIVER_HSAID = "vardgivare-1";
    private static final String CAREUNIT_HSAID = "enhet-1";

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

        ListActiveSickLeavesForCareUnitResponseType responseType = testee.listActiveSickLeavesForCareUnit("", params);
        assertNotNull(responseType);
        assertEquals(ResultCodeEnum.OK, responseType.getResultCode());

        verify(sjukfallCertificateDao, times(1))
                .findActiveSjukfallCertificateForPersonOnCareUnits(CAREGIVER_HSAID, Arrays.asList(CAREUNIT_HSAID),
                        "19121212-1212");
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
        verify(sjukfallCertificateDao, times(1)).findActiveSjukfallCertificateForCareUnits(anyString(), anyList());
        verify(sjukfallCertificateDao, times(0)).findActiveSjukfallCertificateForPersonOnCareUnits(anyString(), anyList(), anyString());
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
        verify(sjukfallCertificateDao, times(1)).findActiveSjukfallCertificateForCareUnits(anyString(), anyList());
        verify(sjukfallCertificateDao, times(0)).findActiveSjukfallCertificateForPersonOnCareUnits(anyString(), anyList(), anyString());
    }
}
