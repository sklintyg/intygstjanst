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
package se.inera.intyg.intygstjanst.web.integration.rehabstod.converter;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;
import se.inera.intyg.intygstjanst.web.integration.util.SjukfallCertTestHelper;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsData;

/**
 * Created by eriklupander on 2016-02-04.
 */
public class SjukfallCertificateRegisterIntygsDataConverterTest {

    private SjukfallCertTestHelper testHelper = new SjukfallCertTestHelper();
    private SjukfallCertificateIntygsDataConverter testee = new SjukfallCertificateIntygsDataConverter();

    @Test
    public void testConvert() {
        List<IntygsData> intygsDataList = testee.buildIntygsData(testHelper.intygsList());
        assertEquals(1, intygsDataList.size());
        assertEquals(2, intygsDataList.get(0).getArbetsformaga().getFormaga().size());
    }
}
