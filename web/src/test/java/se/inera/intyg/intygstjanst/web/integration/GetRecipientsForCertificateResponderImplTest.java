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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v11.GetRecipientsForCertificateResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v11.GetRecipientsForCertificateResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v11.GetRecipientsForCertificateType;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiver;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.builder.RecipientBuilder;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetRecipientsForCertificateResponderImplTest {

    private static final String LOGICAL_ADDRESS = "logicalAddress";

    @Mock
    private RecipientService recipientService;

    @InjectMocks
    private GetRecipientsForCertificateResponderInterface responder = new GetRecipientsForCertificateResponderImpl();

    @Test
    public void getRecipientsForCertificateTest() throws RecipientUnknownException {
        final String intygsTyp = "intygsTyp";
        final String recipientId = "recipientId";
        final String recipientName = "recipientName";

        when(recipientService.listRecipients(any(String.class))).thenReturn(Arrays
                .asList(new RecipientBuilder()
                        .setLogicalAddress("logicalAddress")
                        .setName(recipientName)
                        .setId(recipientId)
                        .setCertificateTypes("certificateTypes")
                        .setActive(true)
                        .setTrusted(true)
                        .build()));

        GetRecipientsForCertificateResponseType res = responder.getRecipientsForCertificate(LOGICAL_ADDRESS, createRequest(intygsTyp));

        assertNotNull(res);
        assertEquals(ResultCodeType.OK, res.getResult().getResultCode());
        assertEquals(1, res.getRecipient().size());
        assertEquals(recipientId, res.getRecipient().get(0).getId());
        assertEquals(recipientName, res.getRecipient().get(0).getName());
        assertTrue(res.getRecipient().get(0).isTrusted());

        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        verify(recipientService).listRecipients(typeCaptor.capture());
        assertEquals(intygsTyp, typeCaptor.getValue());
    }

    @Test
    public void getRecipientsForCertificateNoRecipientsFoundTest() throws RecipientUnknownException {
        final String intygsId = "intygsId";
        when(recipientService.listRecipients(any(String.class))).thenReturn(new ArrayList<>());

        GetRecipientsForCertificateResponseType res = responder.getRecipientsForCertificate(LOGICAL_ADDRESS, createRequest(intygsId));

        assertNotNull(res);
        assertEquals(ResultCodeType.ERROR, res.getResult().getResultCode());
        assertEquals(ErrorIdType.APPLICATION_ERROR, res.getResult().getErrorId());

        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        verify(recipientService).listRecipients(typeCaptor.capture());
        assertEquals(intygsId, typeCaptor.getValue());
    }

    @Test
    public void getRecipientsForCertificateInactiveTest() throws RecipientUnknownException {
        final String intygsTyp = "intygsTyp";

        when(recipientService.listRecipients(any(String.class))).thenReturn(Arrays
                .asList(new RecipientBuilder()
                        .setLogicalAddress("logicalAddress")
                        .setName("recipientName")
                        .setId("recipientId")
                        .setCertificateTypes("certificateTypes")
                        .setActive(false)
                        .setTrusted(true)
                        .build()));

        GetRecipientsForCertificateResponseType res = responder.getRecipientsForCertificate(LOGICAL_ADDRESS, createRequest(intygsTyp));

        assertNotNull(res);
        assertEquals(ResultCodeType.ERROR, res.getResult().getResultCode());

        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        verify(recipientService).listRecipients(typeCaptor.capture());
        assertEquals(intygsTyp, typeCaptor.getValue());
    }

    @Test
    public void getRecipientsForCertificateUntrustedTest() throws RecipientUnknownException {
        final String intygsTyp = "intygsTyp";
        final String recipientId = "recipientId";
        final String recipientName = "recipientName";

        when(recipientService.listRecipients(any(String.class))).thenReturn(Arrays
                .asList(new RecipientBuilder()
                        .setLogicalAddress("logicalAddress")
                        .setName(recipientName)
                        .setId(recipientId)
                        .setCertificateTypes("certificateTypes")
                        .setActive(true)
                        .setTrusted(false)
                        .build()));

        GetRecipientsForCertificateResponseType res = responder.getRecipientsForCertificate(LOGICAL_ADDRESS, createRequest(intygsTyp));

        assertNotNull(res);
        assertEquals(ResultCodeType.OK, res.getResult().getResultCode());
        assertEquals(1, res.getRecipient().size());
        assertEquals(recipientId, res.getRecipient().get(0).getId());
        assertEquals(recipientName, res.getRecipient().get(0).getName());
        assertFalse(res.getRecipient().get(0).isTrusted());

        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        verify(recipientService).listRecipients(typeCaptor.capture());
        assertEquals(intygsTyp, typeCaptor.getValue());
    }

    private GetRecipientsForCertificateType createRequest(String id) {
        GetRecipientsForCertificateType parameters = new GetRecipientsForCertificateType();
        parameters.setCertificateId(id);
        return parameters;
    }

    private ApprovedReceiver getDefaultApprovedReceiver() {
        ApprovedReceiver approvedReceiver = new ApprovedReceiver();
        approvedReceiver.setId(new Random().nextLong());
        approvedReceiver.setApproved(true);
        approvedReceiver.setCertificateId(UUID.randomUUID().toString());
        approvedReceiver.setReceiverId("recipientId");
        return approvedReceiver;
    }
}
