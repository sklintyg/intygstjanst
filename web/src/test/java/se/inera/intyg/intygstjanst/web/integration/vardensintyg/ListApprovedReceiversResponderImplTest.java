/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.clinicalprocess.healthcond.certificate.receiver.types.v1.CertificateReceiverTypeType;
import se.inera.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listapprovedreceivers.v1.ListApprovedReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listapprovedreceivers.v1.ListApprovedReceiversType;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiver;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiverDao;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateRecipientType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

@RunWith(MockitoJUnitRunner.class)
public class ListApprovedReceiversResponderImplTest {

    private static final String LOGICAL_ADDRESS = "logical-address";
    private static final String INTYG_ID = "intyg-123";
    private static final String INTYG_TYP = "af00213";

    @Mock
    private ApprovedReceiverDao approvedReceiverDao;

    @Mock
    private RecipientService recipientService;

    @InjectMocks
    private ListApprovedReceiversResponderImpl testee;

    @Test
    public void testListOk() throws RecipientUnknownException, ModuleNotFoundException {
        when(approvedReceiverDao.getApprovedReceiverIdsForCertificate(INTYG_ID)).thenReturn(Arrays.asList(buildApprovedReceiver("AF")));
        when(recipientService.getRecipient("AF")).thenReturn(buildRecipient("AF", INTYG_TYP));

        ListApprovedReceiversResponseType resp = testee.listApprovedReceivers(LOGICAL_ADDRESS, buildReq(INTYG_ID, INTYG_TYP));

        assertEquals(1, resp.getReceiverList().size());
        assertEquals("AF", resp.getReceiverList().get(0).getReceiverId());
        assertEquals("AF-name", resp.getReceiverList().get(0).getReceiverName());
        assertEquals("HUVUDMOTTAGARE", resp.getReceiverList().get(0).getReceiverType().name());
        assertEquals(CertificateReceiverTypeType.HUVUDMOTTAGARE, resp.getReceiverList().get(0).getReceiverType());
    }

    private ApprovedReceiver buildApprovedReceiver(String mottagareId) {
        ApprovedReceiver ar = new ApprovedReceiver();
        ar.setReceiverId(mottagareId);
        ar.setCertificateId(INTYG_ID);
        ar.setApproved(true);
        return ar;
    }

    @Test
    public void testReturnsEmptyWhenUnknownIntygTyp() {
        ListApprovedReceiversResponseType resp = testee.listApprovedReceivers(LOGICAL_ADDRESS, buildReq(INTYG_ID, "okand-typ"));
        assertEquals(0, resp.getReceiverList().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailsWithNullIntygId() {
        testee.listApprovedReceivers(LOGICAL_ADDRESS, buildReq(null, INTYG_TYP));
    }

    private Recipient buildRecipient(String recipientId, String certTypes) {
        return new Recipient(LOGICAL_ADDRESS, recipientId + "-name", recipientId, CertificateRecipientType.HUVUDMOTTAGARE.name(), certTypes,
            true, true);
    }

    private ListApprovedReceiversType buildReq(String intygsId, String intygTyp) {
        ListApprovedReceiversType req = new ListApprovedReceiversType();
        if (intygsId != null) {
            IntygId intygId = new IntygId();
            intygId.setExtension(intygsId);
            req.setIntygsId(intygId);
        } else {
            req.setIntygsId(null);
        }
        return req;
    }
}
