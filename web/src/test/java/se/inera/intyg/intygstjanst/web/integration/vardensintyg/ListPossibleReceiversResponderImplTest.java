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
package se.inera.intyg.intygstjanst.web.integration.vardensintyg;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversType;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateRecipientType;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.riv.clinicalprocess.healthcond.certificate.receiver.types.v1.CertificateReceiverTypeType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ListPossibleReceiversResponderImplTest {

    private static final String LOGICAL_ADDRESS = "logical-address";
    private static final String INTYG_TYP = "af00213";

    @Mock
    private RecipientService recipientService;

    @InjectMocks
    private ListPossibleReceiversResponderImpl testee;

    @Test
    public void testListOk() {
        CertificateType certificateType = new CertificateType(INTYG_TYP);

        when(recipientService.listRecipients(certificateType)).thenReturn(buildRecipients("AF", INTYG_TYP));

        ListPossibleReceiversResponseType resp = testee.listPossibleReceivers(LOGICAL_ADDRESS, buildReq(INTYG_TYP));

        assertEquals(1, resp.getRecipient().size());
        assertEquals("AF", resp.getRecipient().get(0).getReceiverId());
        assertEquals("AF-name", resp.getRecipient().get(0).getReceiverName());
        assertEquals("HUVUDMOTTAGARE", resp.getRecipient().get(0).getReceiverType().name());
        assertEquals(CertificateReceiverTypeType.HUVUDMOTTAGARE, resp.getRecipient().get(0).getReceiverType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingIntygsTypThrowsException() {
        testee.listPossibleReceivers(LOGICAL_ADDRESS, buildReq(null));
    }

    private List<Recipient> buildRecipients(String recipientId, String certTypes) {
        return Arrays.asList(new Recipient(LOGICAL_ADDRESS, recipientId + "-name", recipientId,
                CertificateRecipientType.HUVUDMOTTAGARE.name(), certTypes, true, true));
    }

    private ListPossibleReceiversType buildReq(String intygTyp) {
        ListPossibleReceiversType req = new ListPossibleReceiversType();

        TypAvIntyg typAvIntyg = new TypAvIntyg();
        typAvIntyg.setCode(intygTyp);
        req.setIntygTyp(typAvIntyg);
        return req;
    }
}
