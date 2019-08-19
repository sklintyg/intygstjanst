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
package se.inera.intyg.intygstjanst.web.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import javax.jms.Queue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;

@RunWith(MockitoJUnitRunner.class)
public class InternalNotificationServiceImplTest {

    private static final String INTYG_ID = "intyg-1";
    private static final String ENHET_ID = "enhet-1";
    private static final String INTYG_TYP = "lijsp";

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Queue internalNotificationQueue;

    @InjectMocks
    private InternalNotificationServiceImpl testee;

    @Test
    public void testSendsNotification() {
        testee.notifyCareIfSentByCitizen(buildCert(), buildSkickadAv(false));
        verify(jmsTemplate, times(1)).send(any(Queue.class), any(MessageCreator.class));
    }

    @Test
    public void testDoesNotNotifiyWhenVardIsSkickadAv() {
        testee.notifyCareIfSentByCitizen(buildCert(), buildSkickadAv(true));
        verifyZeroInteractions(jmsTemplate);
    }

    private SendCertificateToRecipientType.SkickatAv buildSkickadAv(boolean isSentByVard) {
        SendCertificateToRecipientType.SkickatAv skickatAv = new SendCertificateToRecipientType.SkickatAv();
        if (isSentByVard) {
            HosPersonal hosPersonal = new HosPersonal();
            skickatAv.setHosPersonal(hosPersonal);
        } else {
            skickatAv.setPersonId(new PersonId());
        }
        return skickatAv;
    }

    private Certificate buildCert() {
        Certificate c = new Certificate(INTYG_ID);
        c.setCareUnitId(ENHET_ID);
        c.setType(INTYG_TYP);
        return c;
    }
}
