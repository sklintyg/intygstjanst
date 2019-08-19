/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.inera.intyg.common.support.Constants.KV_INTYGSTYP_CODE_SYSTEM;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetypeinfo.v1.GetCertificateTypeInfoResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetypeinfo.v1.GetCertificateTypeInfoResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetypeinfo.v1.GetCertificateTypeInfoType;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateTypeInfo;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;

@RunWith(MockitoJUnitRunner.class)
public class GetCertificateTypeInfoResponderImplTest {

    private static final String LOGICAL_ADDRESS = "logical-address";
    private static final String DEFAULT_VERSION = "1.0";
    @Mock
    private CertificateService certificateService = mock(CertificateService.class);

    @InjectMocks
    private GetCertificateTypeInfoResponderInterface testee = new GetCertificateTypeInfoResponderImpl();

    @Test
    public void testGetCertificateTypeSuccess() {
        String intygsId = UUID.randomUUID().toString();
        String intygsTyp = LisjpEntryPoint.MODULE_ID;

        when(certificateService.getCertificateTypeInfo(anyString())).thenReturn(buildCertType(intygsTyp));

        GetCertificateTypeInfoResponseType response = testee.getCertificateTypeInfo(LOGICAL_ADDRESS, buildReq(intygsId));

        assertEquals(intygsTyp, response.getTyp().getCode());
        assertEquals(DEFAULT_VERSION, response.getTypVersion());
        assertEquals(KV_INTYGSTYP_CODE_SYSTEM, response.getTyp().getCodeSystem());
    }

    @Test(expected = ServerException.class)
    public void testGetCertificateTypeNotFound() {
        String intygsId = UUID.randomUUID().toString();
        when(certificateService.getCertificateTypeInfo(anyString())).thenReturn(null);
        testee.getCertificateTypeInfo(LOGICAL_ADDRESS, buildReq(intygsId));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCertificateTypeEmptyIntygsId() {
        testee.getCertificateTypeInfo(LOGICAL_ADDRESS, buildReq(" "));
    }

    private GetCertificateTypeInfoType buildReq(String intygsId) {
        GetCertificateTypeInfoType req = new GetCertificateTypeInfoType();
        req.setIntygsId(intygsId);
        return req;
    }

    private CertificateTypeInfo buildCertType(String intygsTyp) {
        TypAvIntyg typAvIntyg = new TypAvIntyg();
        typAvIntyg.setCode(intygsTyp);
        typAvIntyg.setCodeSystem(KV_INTYGSTYP_CODE_SYSTEM);
        return new CertificateTypeInfo(typAvIntyg, DEFAULT_VERSION);
    }

}
