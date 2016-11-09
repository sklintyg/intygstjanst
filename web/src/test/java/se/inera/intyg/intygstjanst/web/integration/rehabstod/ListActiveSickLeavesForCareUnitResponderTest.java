/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
import static org.mockito.Mockito.when;

import java.util.ArrayList;

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

/**
 * Created by eriklupander on 2016-02-04.
 */
@RunWith(MockitoJUnitRunner.class)
public class ListActiveSickLeavesForCareUnitResponderTest {

    private static final String HSA_ID = "enhet-1";

    @Mock
    private HsaService hsaService;

    @Mock
    private SjukfallCertificateDao sjukfallCertificateDao;

    @InjectMocks
    private ListActiveSickLeavesForCareUnitResponderImpl testee;

    private HsaId hsaId = new HsaId();

    @Before
    public void init() {
        hsaId.setExtension(HSA_ID);
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
        when(hsaService.getHsaIdForUnderenheter(HSA_ID)).thenReturn(new ArrayList<>());

        ListActiveSickLeavesForCareUnitType params = new ListActiveSickLeavesForCareUnitType();
        params.setEnhetsId(hsaId);
        ListActiveSickLeavesForCareUnitResponseType responseType = testee.listActiveSickLeavesForCareUnit("", params);
        assertNotNull(responseType);
        assertEquals(ResultCodeEnum.OK, responseType.getResultCode());
    }
}
