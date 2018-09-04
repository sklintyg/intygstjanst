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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.ReceiverApprovalStatus;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversType;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiver;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.ReceiverService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.riv.clinicalprocess.healthcond.certificate.receiver.types.v1.ApprovalStatusType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

;

@RunWith(MockitoJUnitRunner.class)
public class RegisterApprovedReceiversResponderImplTest {

    private static final String LOGICAL_ADDRESS = "logical-address";
    private static final String INTYG_ID = "intyg-123";

    @Mock
    private ReceiverService receiverService;

    @Mock
    private RecipientService recipientService;

    @InjectMocks
    private RegisterApprovedReceiversResponderImpl testee;

    @Test
    public void testRegister() throws RecipientUnknownException {
        Recipient recipientFk = new Recipient(LOGICAL_ADDRESS, "name", "FKASSA", "HUVUDMOTTAGARE", "lisjp", true, true);
        Recipient recipientAf = new Recipient(LOGICAL_ADDRESS, "name", "AF", "MOTTAGARE", "lisjp", true, true);

        when(recipientService.getRecipient("FKASSA")).thenReturn(recipientFk);
        when(recipientService.getRecipient("AF")).thenReturn(recipientAf);

        when(recipientService.listRecipients(new CertificateType("lisjp")))
                .thenReturn(Arrays.asList(recipientFk, recipientAf));

        RegisterApprovedReceiversResponseType response = testee.registerApprovedReceivers(LOGICAL_ADDRESS, buildReq("FKASSA", "AF"));
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
        verify(receiverService, times(2)).registerApprovedReceiver(any(ApprovedReceiver.class));
        verify(recipientService, times(2)).getRecipient(anyString());
    }

    @Test
    public void testHuvudmottagareIsAddedIfOmittedRegister() throws RecipientUnknownException {
        Recipient recipientFk = new Recipient(LOGICAL_ADDRESS, "name", "FKASSA", "HUVUDMOTTAGARE", "lisjp", true, true);
        Recipient recipientAf = new Recipient(LOGICAL_ADDRESS, "name", "AF", "MOTTAGARE", "lisjp", true, true);

        when(recipientService.getRecipient("AF")).thenReturn(recipientAf);

        when(recipientService.listRecipients(new CertificateType("lisjp")))
                .thenReturn(Arrays.asList(recipientFk, recipientAf));

        RegisterApprovedReceiversResponseType response = testee.registerApprovedReceivers(LOGICAL_ADDRESS, buildReq("AF"));
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
        verify(recipientService, times(1)).getRecipient(anyString());

        ArgumentCaptor<ApprovedReceiver> approvedReceiverCaptor = ArgumentCaptor.forClass(ApprovedReceiver.class);
        verify(receiverService, times(2)).registerApprovedReceiver(approvedReceiverCaptor.capture());
        List<ApprovedReceiver> approvedReceivers = approvedReceiverCaptor.getAllValues();

        // This assert proves that FKASSA was added even though it wasn't part of the request.
        assertTrue(approvedReceivers.stream().anyMatch(ar -> "FKASSA".equals(ar.getReceiverId())));
    }

    @Test
    public void testReturnsErrorWhenNullIntygId() {
        RegisterApprovedReceiversType req = buildReq("FKASSA");
        req.getIntygId().setExtension(null);
        RegisterApprovedReceiversResponseType response = testee.registerApprovedReceivers(LOGICAL_ADDRESS, req);

        assertEquals(ResultCodeType.ERROR, response.getResult().getResultCode());
        verifyZeroInteractions(receiverService);
    }

    @Test
    public void testReturnsErrorWhenBlankIntygId() {
        RegisterApprovedReceiversType req = buildReq("FKASSA");
        req.getIntygId().setExtension("");
        RegisterApprovedReceiversResponseType response = testee.registerApprovedReceivers(LOGICAL_ADDRESS, req);

        assertEquals(ResultCodeType.ERROR, response.getResult().getResultCode());
        verifyZeroInteractions(receiverService);
    }

    @Test
    public void testReturnsErrorWhenBlankIntygsTyp() {
        RegisterApprovedReceiversType req = buildReq("FKASSA");
        req.getTypAvIntyg().setCode("");
        RegisterApprovedReceiversResponseType response = testee.registerApprovedReceivers(LOGICAL_ADDRESS, req);

        assertEquals(ResultCodeType.ERROR, response.getResult().getResultCode());
        verifyZeroInteractions(receiverService);
    }

    @Test
    public void testReturnsErrorWhenBlankReceiverId() {
        RegisterApprovedReceiversResponseType response = testee.registerApprovedReceivers(LOGICAL_ADDRESS, buildReq(""));

        assertEquals(ResultCodeType.ERROR, response.getResult().getResultCode());
        verifyZeroInteractions(receiverService);
    }

    @Test
    public void testReturnsErrorWhenUnknownReceiverId() throws RecipientUnknownException {
        when(recipientService.getRecipient(anyString())).thenThrow(new RecipientUnknownException(""));
        RegisterApprovedReceiversResponseType response = testee.registerApprovedReceivers(LOGICAL_ADDRESS, buildReq("OKAND"));

        assertEquals(ResultCodeType.ERROR, response.getResult().getResultCode());
        verifyZeroInteractions(receiverService);
    }

    private RegisterApprovedReceiversType buildReq(String... receivers) {
        RegisterApprovedReceiversType req = new RegisterApprovedReceiversType();
        IntygId intygId = new IntygId();
        intygId.setExtension(INTYG_ID);
        req.setIntygId(intygId);

        TypAvIntyg typAvIntyg = new TypAvIntyg();
        typAvIntyg.setCode("lisjp");
        req.setTypAvIntyg(typAvIntyg);

        Arrays.stream(receivers).forEach(rec -> {
            ReceiverApprovalStatus receiverApprovalStatus = new ReceiverApprovalStatus();
            receiverApprovalStatus.setReceiverId(rec);
            receiverApprovalStatus.setApprovalStatus(ApprovalStatusType.YES);
            req.getApprovedReceivers().addAll(Arrays.asList(receiverApprovalStatus));
        });

        return req;
    }
}
