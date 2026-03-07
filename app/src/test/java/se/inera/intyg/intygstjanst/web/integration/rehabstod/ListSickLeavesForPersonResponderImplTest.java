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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listsickleavesforperson.v1.ListSickLeavesForPersonResponseType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listsickleavesforperson.v1.ListSickLeavesForPersonType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listsickleavesforperson.v1.ResultCodeEnum;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.csintegration.aggregator.ValidSickLeaveAggregator;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;

/**
 * Created by Magnus Ekstrand on 2018-10-23.
 */
@ExtendWith(MockitoExtension.class)
class ListSickLeavesForPersonResponderImplTest {

    @Mock
    private ValidSickLeaveAggregator validSickLeaveAggregator;

    @Mock
    private SjukfallCertificateDao sjukfallCertificateDao;

    @InjectMocks
    private ListSickLeavesForPersonResponderImpl testee;

    @Test
    void testWithNoPersonId() {
        ListSickLeavesForPersonType params = new ListSickLeavesForPersonType();
        ListSickLeavesForPersonResponseType responseType = testee.listSickLeavesForPerson("", params);
        assertNotNull(responseType);
        assertEquals(ResultCodeEnum.ERROR, responseType.getResult().getResultCode());
        assertEquals("Could not parse passed personnummer", responseType.getResult().getResultMessage());
    }

    @Test
    void testNormalHappyPath() {
        PersonId patientId = new PersonId();
        patientId.setExtension("191212121212");

        ListSickLeavesForPersonType params = new ListSickLeavesForPersonType();
        params.setPersonId(patientId);

        ListSickLeavesForPersonResponseType responseType = testee.listSickLeavesForPerson("", params);
        assertNotNull(responseType);
        assertEquals(ResultCodeEnum.OK, responseType.getResult().getResultCode());
    }

    @Test
    void testFilteringOfTestCertificates() {
        PersonId patientId = new PersonId();
        patientId.setExtension("191212121212");

        final SjukfallCertificate realSjukfallCertificate = new SjukfallCertificate("realSjukfallCertificateId");
        final SjukfallCertificate testSjukfallCertificate = new SjukfallCertificate("testSjukfallCertificateId");
        testSjukfallCertificate.setTestCertificate(true);
        final List<SjukfallCertificate> sjukfallCertificateList = Arrays.asList(realSjukfallCertificate, testSjukfallCertificate);

        when(sjukfallCertificateDao.findSjukfallCertificateForPerson(anyString())).thenReturn(sjukfallCertificateList);
        doReturn(List.of(realSjukfallCertificate)).when(validSickLeaveAggregator).get(sjukfallCertificateList);

        ListSickLeavesForPersonType params = new ListSickLeavesForPersonType();
        params.setPersonId(patientId);

        ListSickLeavesForPersonResponseType responseType = testee.listSickLeavesForPerson("", params);

        assertNotNull(responseType);
        assertEquals(ResultCodeEnum.OK, responseType.getResult().getResultCode());
        assertEquals(1, responseType.getIntygsLista().getIntygsData().size());
        assertEquals("realSjukfallCertificateId", responseType.getIntygsLista().getIntygsData().getFirst().getIntygsId());
    }

}