/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.common.support.Constants.KV_INTYGSTYP_CODE_SYSTEM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificatetypeinfo.v1.GetCertificateTypeInfoType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v11.GetRecipientsForCertificateResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v11.GetRecipientsForCertificateResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v11.GetRecipientsForCertificateType;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateTypeInfo;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.intygstjanst.web.service.builder.RecipientBuilder;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;

@RunWith(MockitoJUnitRunner.class)
public class GetRecipientsForCertificateResponderImplTest {

    private static final String LOGICAL_ADDRESS = "logicalAddress";
    private static final String DEFAULT_TYPE_VERSION = "1.0";

    @Mock
    private CertificateService certificateService;

    @Mock
    private RecipientService recipientService;

    @InjectMocks
    private GetRecipientsForCertificateResponderInterface responder = new GetRecipientsForCertificateResponderImpl();

    @Test
    public void getRecipientsForCertificateTest() {
        final String intygsId = "intygsId";
        when(recipientService.listRecipients(any(String.class))).thenReturn(getRecipientList(true, true));

        GetRecipientsForCertificateResponseType res =
            responder.getRecipientsForCertificate(LOGICAL_ADDRESS, createRecipientsForCertificateRequest(intygsId));

        assertNotNull(res);
        assertEquals(ResultCodeType.OK, res.getResult().getResultCode());
        assertEquals(1, res.getRecipient().size());
        assertEquals("recipientId", res.getRecipient().get(0).getId());
        assertEquals("recipientName", res.getRecipient().get(0).getName());
        assertTrue(res.getRecipient().get(0).isTrusted());

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(recipientService).listRecipients(stringCaptor.capture());
        assertEquals(intygsId, stringCaptor.getValue());
    }

    @Test
    public void getRecipientsForCertificateWhenNoRecipientsWereFoundTest() throws InvalidCertificateException {
        final String intygsId = "intygsId";

        when(recipientService.listRecipients(any(String.class))).thenReturn(new ArrayList<>());
        when(certificateService.getCertificateTypeInfo(any(String.class))).thenReturn(createCertificateType(null, null));

        GetRecipientsForCertificateResponseType res =
            responder.getRecipientsForCertificate(LOGICAL_ADDRESS, createRecipientsForCertificateRequest(intygsId));

        assertNotNull(res);
        assertEquals(ResultCodeType.ERROR, res.getResult().getResultCode());
        assertEquals(ErrorIdType.APPLICATION_ERROR, res.getResult().getErrorId());

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(recipientService).listRecipients(stringCaptor.capture());
        assertEquals(intygsId, stringCaptor.getValue());

        ArgumentCaptor<GetCertificateTypeInfoType> typeCaptor = ArgumentCaptor.forClass(GetCertificateTypeInfoType.class);
        verify(certificateService).getCertificateTypeInfo(stringCaptor.capture());
    }

    @Test
    public void getRecipientForCertificateWhenNoApprovedRecieversWereFoundTest() throws InvalidCertificateException {
        final String intygsId = "intygsId";

        when(recipientService.listRecipients(any(String.class))).thenReturn(new ArrayList<>());
        when(recipientService.listRecipients(any(CertificateType.class))).thenReturn(getRecipientList(true, true));
        when(certificateService.getCertificateTypeInfo(any(String.class)))
            .thenReturn(createCertificateType(LisjpEntryPoint.MODULE_ID, DEFAULT_TYPE_VERSION));

        GetRecipientsForCertificateResponseType res =
            responder.getRecipientsForCertificate(LOGICAL_ADDRESS, createRecipientsForCertificateRequest(intygsId));

        assertNotNull(res);
        assertEquals(ResultCodeType.OK, res.getResult().getResultCode());
        assertEquals(1, res.getRecipient().size());
        assertEquals("recipientId", res.getRecipient().get(0).getId());
        assertEquals("recipientName", res.getRecipient().get(0).getName());
        assertTrue(res.getRecipient().get(0).isTrusted());

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(recipientService).listRecipients(stringCaptor.capture());

        ArgumentCaptor<CertificateType> ctypeCaptor = ArgumentCaptor.forClass(CertificateType.class);
        verify(recipientService).listRecipients(ctypeCaptor.capture());

        ArgumentCaptor<GetCertificateTypeInfoType> gctypeCaptor = ArgumentCaptor.forClass(GetCertificateTypeInfoType.class);
        verify(certificateService).getCertificateTypeInfo(stringCaptor.capture());
    }

    @Test
    public void getRecipientsForCertificateInactiveTest() {
        final String intygsId = "intygsId";

        when(recipientService.listRecipients(any(String.class))).thenReturn(getRecipientList(false, true));

        GetRecipientsForCertificateResponseType res =
            responder.getRecipientsForCertificate(LOGICAL_ADDRESS, createRecipientsForCertificateRequest(intygsId));

        assertNotNull(res);
        assertEquals(ResultCodeType.ERROR, res.getResult().getResultCode());

        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        verify(recipientService).listRecipients(typeCaptor.capture());
        assertEquals(intygsId, typeCaptor.getValue());
    }

    @Test
    public void getRecipientsForCertificateUntrustedTest() {
        final String intygsId = "intygsId";

        when(recipientService.listRecipients(any(String.class))).thenReturn(getRecipientList(true, false));

        GetRecipientsForCertificateResponseType res =
            responder.getRecipientsForCertificate(LOGICAL_ADDRESS, createRecipientsForCertificateRequest(intygsId));

        assertNotNull(res);
        assertEquals(ResultCodeType.OK, res.getResult().getResultCode());
        assertEquals(1, res.getRecipient().size());
        assertEquals("recipientId", res.getRecipient().get(0).getId());
        assertEquals("recipientName", res.getRecipient().get(0).getName());
        assertFalse(res.getRecipient().get(0).isTrusted());

        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        verify(recipientService).listRecipients(typeCaptor.capture());
        assertEquals(intygsId, typeCaptor.getValue());
    }

    private GetRecipientsForCertificateType createRecipientsForCertificateRequest(String id) {
        GetRecipientsForCertificateType reguest = new GetRecipientsForCertificateType();
        reguest.setCertificateId(id);
        return reguest;
    }

    private CertificateTypeInfo createCertificateType(String certificateType, String version) {
        TypAvIntyg typAvIntyg = new TypAvIntyg();
        if (StringUtils.isNoneBlank(certificateType)) {
            typAvIntyg.setCode(certificateType);
            typAvIntyg.setCodeSystem(KV_INTYGSTYP_CODE_SYSTEM);
        }
        return new CertificateTypeInfo(typAvIntyg, version);
    }

    private List<Recipient> getRecipientList(boolean active, boolean trusted) {
        return Collections.singletonList(new RecipientBuilder()
            .setLogicalAddress("logicalAddress")
            .setName("recipientName")
            .setId("recipientId")
            .setCertificateTypes("certificateTypes")
            .setActive(active)
            .setTrusted(trusted)
            .build());
    }

}
