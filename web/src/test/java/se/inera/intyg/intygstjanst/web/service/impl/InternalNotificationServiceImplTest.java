/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import jakarta.jms.Queue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;

@RunWith(MockitoJUnitRunner.class)
public class InternalNotificationServiceImplTest {

    private static final String INTYG_ID = "intyg-1";
    private static final String ENHET_ID = "enhet-1";
    private static final String INTYG_TYP = "lijsp";
    private static final String PERSON_ID = "personId";
    private static final String HSA_ID = "hsaId";

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Queue internalNotificationQueue;

    @InjectMocks
    private InternalNotificationServiceImpl testee;

    @Test
    public void testSendsNotificationIfSentByCitizen() {
        testee.notifyCareIfSentByCitizen(buildCert(), PERSON_ID, null);
        verify(jmsTemplate, times(1)).send(any(Queue.class), any(MessageCreator.class));
    }

    @Test
    public void testDoesNotNotifiyWhenHsaIdIsDefined() {
        testee.notifyCareIfSentByCitizen(buildCert(), PERSON_ID, HSA_ID);
        verifyNoInteractions(jmsTemplate);
    }

    private Certificate buildCert() {
        Certificate c = new Certificate(INTYG_ID);
        c.setCareUnitId(ENHET_ID);
        c.setType(INTYG_TYP);
        return c;
    }
}
