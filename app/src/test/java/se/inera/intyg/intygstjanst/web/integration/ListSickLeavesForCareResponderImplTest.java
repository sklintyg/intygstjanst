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
package se.inera.intyg.intygstjanst.web.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineServiceImpl;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.csintegration.aggregator.ValidSickLeaveAggregator;
import se.inera.intyg.intygstjanst.web.integration.converter.SjukfallCertificateConverter;
import se.inera.intyg.intygstjanst.web.integration.converter.SjukfallConverter;
import se.inera.intyg.intygstjanst.web.integration.hsa.HsaService;
import se.inera.intyg.intygstjanst.web.integration.util.SjukfallCertTestHelper;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.ListSickLeavesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.ListSickLeavesForCareType;
import se.riv.clinicalprocess.healthcond.certificate.listsickleavesforcare.v1.Sjukfall;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;

/**
 * @author eriklupander
 */
@ExtendWith(MockitoExtension.class)
class ListSickLeavesForCareResponderImplTest {

    @Mock
    private ValidSickLeaveAggregator validSickLeaveAggregator;

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

    @BeforeEach
    void init() {
        enhetsId = new HsaId();
        enhetsId.setExtension(SjukfallCertTestHelper.CARE_UNIT_1_ID);

        lakareId = new HsaId();
        lakareId.setExtension(SjukfallCertTestHelper.DOCTOR_HSA_ID);
    }

    @Test
    void testListSickLeavesForCare() {
        final var sjukfallCertificates = buildSjukfallCertificates();
        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(or(isNull(), anyString()), anyList(), anyInt()))
            .thenReturn(sjukfallCertificates);
        when(validSickLeaveAggregator.get(sjukfallCertificates)).thenReturn(sjukfallCertificates);

        ListSickLeavesForCareResponseType response = testee.listSickLeavesForCare("", buildParams(100, enhetsId, null));
        assertEquals(1, response.getSjukfallLista().getSjukfall().size());
        Sjukfall sjukfall = response.getSjukfallLista().getSjukfall().getFirst();

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
    void testListSickLeavesForCareForCorrectDoctor() {
        final var sjukfallCertificates = buildSjukfallCertificates();
        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(or(isNull(), anyString()), anyList(), anyInt()))
            .thenReturn(sjukfallCertificates);
        when(validSickLeaveAggregator.get(sjukfallCertificates)).thenReturn(sjukfallCertificates);

        ListSickLeavesForCareResponseType response = testee.listSickLeavesForCare("", buildParams(100, enhetsId, lakareId));
        assertEquals(1, response.getSjukfallLista().getSjukfall().size());
    }

    @Test
    void testListSickLeavesForCareForInCorrectDoctorReturnsZeroRows() {
        lakareId.setExtension("other-doctor");
        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(or(isNull(), anyString()), anyList(), anyInt()))
            .thenReturn(buildSjukfallCertificates());

        ListSickLeavesForCareResponseType response = testee.listSickLeavesForCare("", buildParams(100, enhetsId, lakareId));
        assertEquals(0, response.getSjukfallLista().getSjukfall().size());
    }

    @Test
    void testListSickLeavesForCareReturns0WhenTooLargeMinSjukskrivningslangd() {

        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(or(isNull(), anyString()), anyList(), anyInt()))
            .thenReturn(buildSjukfallCertificates());

        ListSickLeavesForCareResponseType response = testee.listSickLeavesForCare("", buildParams(100, enhetsId, null, 100, 200));
        assertEquals(0, response.getSjukfallLista().getSjukfall().size());
    }

    @Test
    void testListSickLeavesForCareReturns0WhenTooShortMaxSjukskrivningslangd() {

        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(or(isNull(), anyString()), anyList(), anyInt()))
            .thenReturn(buildSjukfallCertificates());

        ListSickLeavesForCareResponseType response = testee.listSickLeavesForCare("", buildParams(100, enhetsId, null, 1, 2));
        assertEquals(0, response.getSjukfallLista().getSjukfall().size());
    }

    @Test
    void testListSickLeavesForCareWhenZeroSjukfallCertsAreReturnedFromDao() {

        when(sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(or(isNull(), anyString()), anyList(), anyInt()))
            .thenReturn(new ArrayList<>());

        ListSickLeavesForCareResponseType response = testee.listSickLeavesForCare("", buildParams(100, enhetsId, null));
        assertEquals(0, response.getSjukfallLista().getSjukfall().size());
    }

    @Test
    void testListSickLeavesForCareWithNullEnhetsIdReturnsError() {
        final var params = buildParams(100, null, null);
        assertThrows(IllegalArgumentException.class, () ->
            testee.listSickLeavesForCare("", params));
    }

    @Test
    void testListSickLeavesForCareWithEmptyEnhetsIdReturnsError() {
        enhetsId.setExtension("");
        final var params = buildParams(100, enhetsId, null);
        assertThrows(IllegalArgumentException.class, () ->
            testee.listSickLeavesForCare("", params));
    }

    @Test
    void testListSickLeavesForCareWithNegativeGlappReturnsError() {
        final var params = buildParams(-1, enhetsId, null);
        assertThrows(IllegalArgumentException.class, () ->
            testee.listSickLeavesForCare("", params));
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