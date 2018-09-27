/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineServiceImpl;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.integration.converter.SjukfallCertificateConverter;
import se.inera.intyg.intygstjanst.web.integration.converter.SjukfallConverter;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.inera.intyg.intygstjanst.web.integration.util.SjukfallCertTestHelper;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.ListSickLeavesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.ListSickLeavesForCareType;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukfall;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author eriklupander
 */
@RunWith(MockitoJUnitRunner.class)
public class ListSickLeavesForCareResponderImplTest {

    @Mock
    private HsaService hsaService;

    @Spy
    private SjukfallEngineService sjukfallEngineService = new SjukfallEngineServiceImpl();

    @Mock
    private SjukfallCertificateDao sjukfallCertificateDao;

    @Spy
    private SjukfallConverter sjukfallConverter;

    @Spy
    private SjukfallCertificateConverter sjukfallCertificateConverter;

    @InjectMocks
    private ListSickLeavesForCareResponderImpl testee = new ListSickLeavesForCareResponderImpl();

    private HsaId enhetsId;
    private HsaId lakareId;

    private SjukfallCertTestHelper testHelper = new SjukfallCertTestHelper();

    @Before
    public void init() {
        enhetsId = new HsaId();
        enhetsId.setExtension(SjukfallCertTestHelper.CARE_UNIT_1_ID);

        lakareId = new HsaId();
        lakareId.setExtension(SjukfallCertTestHelper.DOCTOR_HSA_ID);
    }

    @Test
    public void testListSickLeavesForCare() {

        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(or(isNull(), anyString()), anyList(), anyInt()))
                .thenReturn(buildSjukfallCertificates());

        ListSickLeavesForCareResponseType response = testee.listSickLeavesForCare("", buildParams(100, enhetsId, null));
        assertEquals(1, response.getSjukfallLista().getSjukfall().size());
        Sjukfall sjukfall = response.getSjukfallLista().getSjukfall().get(0);

        assertEquals(SjukfallCertTestHelper.CARE_UNIT_1_ID, sjukfall.getEnhetsId().getExtension());
        assertEquals(SjukfallCertTestHelper.DOCTOR_HSA_ID, sjukfall.getPersonalId().getExtension());
        assertEquals(SjukfallCertTestHelper.PERSONNUMMER, sjukfall.getPersonId().getExtension());
        assertEquals(SjukfallCertTestHelper.PATIENT_NAME, sjukfall.getPatientFullstandigtNamn());
        assertEquals(SjukfallCertTestHelper.DIAGNOSE_CODE, sjukfall.getDiagnoskod().getCode());
        assertEquals(1, sjukfall.getAntalIntyg());
        assertEquals(75, sjukfall.getSjukskrivningsgrad().getAktivGrad());
        assertEquals(2, sjukfall.getSjukskrivningsgrad().getGrader().getGrad().size());

        // Order is important
        assertEquals(100, sjukfall.getSjukskrivningsgrad().getGrader().getGrad().get(0).intValue());
        assertEquals(75, sjukfall.getSjukskrivningsgrad().getGrader().getGrad().get(1).intValue());

        assertEquals(LocalDate.now().minusWeeks(3), sjukfall.getStartdatum());
        assertEquals(LocalDate.now().plusWeeks(1), sjukfall.getSlutdatum());

        // The test data is minus three weeks -> plus one week, which including "today" is 4 weeks + 1 day
        assertEquals(29, sjukfall.getSjukskrivningslangd());
    }

    @Test
    public void testListSickLeavesForCareForCorrectDoctor() {

        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(or(isNull(), anyString()), anyList(), anyInt()))
                .thenReturn(buildSjukfallCertificates());

        ListSickLeavesForCareResponseType response = testee.listSickLeavesForCare("", buildParams(100, enhetsId, lakareId));
        assertEquals(1, response.getSjukfallLista().getSjukfall().size());
    }

    @Test
    public void testListSickLeavesForCareForInCorrectDoctorReturnsZeroRows() {
        lakareId.setExtension("other-doctor");
        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(or(isNull(), anyString()), anyList(), anyInt()))
                .thenReturn(buildSjukfallCertificates());

        ListSickLeavesForCareResponseType response = testee.listSickLeavesForCare("", buildParams(100, enhetsId, lakareId));
        assertEquals(0, response.getSjukfallLista().getSjukfall().size());
    }

    @Test
    public void testListSickLeavesForCareReturns0WhenTooLargeMinSjukskrivningslangd() {

        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(or(isNull(), anyString()), anyList(), anyInt()))
                .thenReturn(buildSjukfallCertificates());

        ListSickLeavesForCareResponseType response = testee.listSickLeavesForCare("", buildParams(100, enhetsId, null, 100, 200));
        assertEquals(0, response.getSjukfallLista().getSjukfall().size());
    }

    @Test
    public void testListSickLeavesForCareReturns0WhenTooShortMaxSjukskrivningslangd() {

        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(or(isNull(), anyString()), anyList(), anyInt()))
                .thenReturn(buildSjukfallCertificates());

        ListSickLeavesForCareResponseType response = testee.listSickLeavesForCare("", buildParams(100, enhetsId, null, 1, 2));
        assertEquals(0, response.getSjukfallLista().getSjukfall().size());
    }

    @Test
    public void testListSickLeavesForCareWhenZeroSjukfallCertsAreReturnedFromDao() {

        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(or(isNull(), anyString()), anyList(), anyInt())).thenReturn(new ArrayList<>());

        ListSickLeavesForCareResponseType response = testee.listSickLeavesForCare("", buildParams(100, enhetsId, null));
        assertEquals(0, response.getSjukfallLista().getSjukfall().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListSickLeavesForCareWithNullEnhetsIdReturnsError() {
        ListSickLeavesForCareResponseType response = testee.listSickLeavesForCare("", buildParams(100, null, null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListSickLeavesForCareWithEmptyEnhetsIdReturnsError() {
        enhetsId.setExtension("");
        ListSickLeavesForCareResponseType response = testee.listSickLeavesForCare("", buildParams(100, enhetsId, null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListSickLeavesForCareWithNegativeGlappReturnsError() {
        ListSickLeavesForCareResponseType response = testee.listSickLeavesForCare("", buildParams(-1, enhetsId, null));
    }

    private List<SjukfallCertificate> buildSjukfallCertificates() {
        return testHelper.intygsList();
    }

    private ListSickLeavesForCareType buildParams(int maxDagarMellanIntyg, HsaId careUnitId, HsaId doctorId) {
        ListSickLeavesForCareType params = new ListSickLeavesForCareType();
        params.setEnhetsId(careUnitId);
        params.setMaxDagarMellanIntyg(maxDagarMellanIntyg);
        params.getPersonalId().add(doctorId);
        return params;
    }

    private ListSickLeavesForCareType buildParams(int maxDagarMellanIntyg, HsaId careUnitId, HsaId doctorId, int minSjukskrivningslangd,
            int maxSjukskrivningslangd) {
        ListSickLeavesForCareType params = new ListSickLeavesForCareType();
        params.setEnhetsId(careUnitId);
        params.setMaxDagarMellanIntyg(maxDagarMellanIntyg);
        params.getPersonalId().add(doctorId);
        params.setMinstaSjukskrivningslangd(minSjukskrivningslangd);
        params.setMaxSjukskrivningslangd(maxSjukskrivningslangd);
        return params;
    }

}
