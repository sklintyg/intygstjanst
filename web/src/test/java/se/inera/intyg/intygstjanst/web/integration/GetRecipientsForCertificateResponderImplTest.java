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

package se.inera.intyg.intygstjanst.web.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.*;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;

@RunWith(MockitoJUnitRunner.class)
public class GetRecipientsForCertificateResponderImplTest {

    private static final String LOGICAL_ADDRESS = "logicalAddress";

    @Mock
    private RecipientService recipientService;

    @InjectMocks
    private GetRecipientsForCertificateResponderInterface responder = new GetRecipientsForCertificateResponderImpl();

    @Test
    public void getRecipientsForCertificateTest() throws RecipientUnknownException {
        final String intygTyp = "intygTyp";
        final String recipientId = "recipientId";
        final String recipientName = "recipientName";
        when(recipientService.listRecipients(any(CertificateType.class))).thenReturn(Arrays.asList(new Recipient("logicalAddress", recipientName, recipientId, "certificateTypes")));
        GetRecipientsForCertificateResponseType res = responder.getRecipientsForCertificate(LOGICAL_ADDRESS, createRequest(intygTyp));
        assertNotNull(res);
        assertEquals(ResultCodeType.OK, res.getResult().getResultCode());
        assertEquals(1, res.getRecipient().size());
        assertEquals(recipientId, res.getRecipient().get(0).getId());
        assertEquals(recipientName, res.getRecipient().get(0).getName());
        ArgumentCaptor<CertificateType> typeCaptor = ArgumentCaptor.forClass(CertificateType.class);
        verify(recipientService).listRecipients(typeCaptor.capture());
        assertEquals(intygTyp, typeCaptor.getValue().getCertificateTypeId());
    }

    @Test
    public void getRecipientsForCertificateNoRecipientsFoundTest() throws RecipientUnknownException {
        final String intygTyp = "intygTyp";
        when(recipientService.listRecipients(any(CertificateType.class))).thenReturn(new ArrayList<>());
        GetRecipientsForCertificateResponseType res = responder.getRecipientsForCertificate(LOGICAL_ADDRESS, createRequest(intygTyp));
        assertNotNull(res);
        assertEquals(ResultCodeType.ERROR, res.getResult().getResultCode());
        assertEquals(ErrorIdType.APPLICATION_ERROR, res.getResult().getErrorId());
        ArgumentCaptor<CertificateType> typeCaptor = ArgumentCaptor.forClass(CertificateType.class);
        verify(recipientService).listRecipients(typeCaptor.capture());
        assertEquals(intygTyp, typeCaptor.getValue().getCertificateTypeId());
    }

    private GetRecipientsForCertificateType createRequest(String typ) {
        GetRecipientsForCertificateType parameters = new GetRecipientsForCertificateType();
        parameters.setCertificateType(typ);
        return parameters;
    }
}