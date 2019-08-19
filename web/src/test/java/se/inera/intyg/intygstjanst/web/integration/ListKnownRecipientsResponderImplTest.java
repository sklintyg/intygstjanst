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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listknownrecipients.v1.ListKnownRecipientsResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listknownrecipients.v1.ListKnownRecipientsType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listknownrecipients.v1.RecipientType;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.builder.RecipientBuilder;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType;

@RunWith(MockitoJUnitRunner.class)
public class ListKnownRecipientsResponderImplTest {

    @Mock
    private RecipientService recipientService;
    @InjectMocks
    private ListKnownRecipientsResponderImpl responder;

    @Test
    public void testListKnownRecipients() {
        final String recipientId = "recipientId";
        final String recipientName = "recipientName";
        when(recipientService.listRecipients()).thenReturn(Arrays.asList(new RecipientBuilder()
                .setLogicalAddress("logicalAddress")
                .setName(recipientName)
                .setId(recipientId)
                .setCertificateTypes("certificateTypes")
                .setActive(true)
                .setTrusted(true)
                .build(),
            new RecipientBuilder()
                .setLogicalAddress("logicalAddress2")
                .setName("name2")
                .setId("id2")
                .setCertificateTypes("certificateTypes")
                .setActive(true)
                .setTrusted(false)
                .build()));
        ListKnownRecipientsResponseType response = responder.listKnownRecipients("", new ListKnownRecipientsType());
        assertNotNull(response);
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
        assertEquals(2, response.getRecipient().size());
        assertEquals(recipientId, response.getRecipient().get(0).getId());
        assertEquals(recipientName, response.getRecipient().get(0).getName());
        assertTrue(response.getRecipient().get(0).isTrusted());
        assertFalse(response.getRecipient().stream().allMatch(RecipientType::isTrusted));
    }
}
