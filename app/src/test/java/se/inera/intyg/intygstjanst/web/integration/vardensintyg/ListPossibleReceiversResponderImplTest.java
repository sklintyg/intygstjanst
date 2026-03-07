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
package se.inera.intyg.intygstjanst.web.integration.vardensintyg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.clinicalprocess.healthcond.certificate.receiver.types.v1.CertificateReceiverTypeType;
import se.inera.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listpossiblereceivers.v1.ListPossibleReceiversType;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateRecipientType;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

@ExtendWith(MockitoExtension.class)
class ListPossibleReceiversResponderImplTest {

    private static final String LOGICAL_ADDRESS = "logical-address";
    private static final String INTYG_TYP = "af00213";

    @Mock
    private RecipientService recipientService;

    @InjectMocks
    private ListPossibleReceiversResponderImpl testee;

    @Test
    void testListOk() {
        CertificateType certificateType = new CertificateType(INTYG_TYP);

        when(recipientService.listRecipients(certificateType)).thenReturn(buildRecipients("AF", INTYG_TYP));

        ListPossibleReceiversResponseType resp = testee.listPossibleReceivers(LOGICAL_ADDRESS, buildReq(INTYG_TYP));

        assertEquals(1, resp.getReceiverList().size());
        assertEquals("AF", resp.getReceiverList().getFirst().getReceiverId());
        assertEquals("AF-name", resp.getReceiverList().getFirst().getReceiverName());
        assertEquals("HUVUDMOTTAGARE", resp.getReceiverList().getFirst().getReceiverType().name());
        assertEquals(CertificateReceiverTypeType.HUVUDMOTTAGARE, resp.getReceiverList().getFirst().getReceiverType());
    }

    @Test
    void testMissingIntygsTypThrowsException() {
        final var listPossibleReceiversType = buildReq(null);
        assertThrows(IllegalArgumentException.class,
            () -> testee.listPossibleReceivers(LOGICAL_ADDRESS, listPossibleReceiversType)
        );
    }

    private List<Recipient> buildRecipients(String recipientId, String certTypes) {
        return List.of(new Recipient(LOGICAL_ADDRESS, recipientId + "-name", recipientId,
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
