/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsServiceImplTest {

    @Mock
    private JmsTemplate template = mock(JmsTemplate.class);

    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private StatisticsServiceImpl serviceImpl = new StatisticsServiceImpl(); 

    @Test
    public void disabledServiceDoesNothingOnCreated() {
        serviceImpl.created(null);
        verify(template, never()).send(any(MessageCreator.class));
    }

    @Test
    public void disabledServiceDoesNothingOnRevoked() {
        serviceImpl.revoked(null);
        verify(template, never()).send(any(MessageCreator.class));
    }

    @Test
    public void serviceSendsDocumentAndIdForCreate() throws JMSException {
        org.springframework.test.util.ReflectionTestUtils.setField(serviceImpl, "enabled", Boolean.TRUE);
        ArgumentCaptor<MessageCreator> captor = ArgumentCaptor.forClass(MessageCreator.class);

        Certificate certificate = mock(Certificate.class);
        when(certificate.getDocument()).thenReturn("The document");
        when(certificate.getId()).thenReturn("The id");
        
        TextMessage message = mock(TextMessage.class);
        Session session = mock(Session.class);
        when(session.createTextMessage("The document")).thenReturn(message);
        
        serviceImpl.created(certificate);
        
        verify(template, only()).send(captor.capture());
        captor.getValue().createMessage(session);
        verify(message).setStringProperty("action", "created");
        verify(message).setStringProperty("certificate-id", "The id");
    }

    @Test
    public void serviceSendsDocumentAndIdForRevoke() throws JMSException {
        org.springframework.test.util.ReflectionTestUtils.setField(serviceImpl, "enabled", Boolean.TRUE);
        ArgumentCaptor<MessageCreator> captor = ArgumentCaptor.forClass(MessageCreator.class);

        Certificate certificate = mock(Certificate.class);
        when(certificate.getDocument()).thenReturn("The document");
        when(certificate.getId()).thenReturn("The id");
        
        TextMessage message = mock(TextMessage.class);
        Session session = mock(Session.class);
        when(session.createTextMessage("The document")).thenReturn(message);
        
        serviceImpl.revoked(certificate);
        
        verify(template, only()).send(captor.capture());
        captor.getValue().createMessage(session);
        verify(message).setStringProperty("action", "revoked");
        verify(message).setStringProperty("certificate-id", "The id");
    }
}
