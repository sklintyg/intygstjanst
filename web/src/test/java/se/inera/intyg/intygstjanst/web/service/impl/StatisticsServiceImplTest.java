/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import javax.jms.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.util.ReflectionTestUtils;

import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsServiceImplTest {

    @Mock
    private JmsTemplate template;

    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private StatisticsServiceImpl serviceImpl;

    @Test
    public void disabledServiceDoesNothingOnCreated() {
        serviceImpl.created(null, null, null, null);
        verify(template, never()).send(any(MessageCreator.class));
    }

    @Test
    public void disabledServiceDoesNothingOnRevoked() {
        serviceImpl.revoked(null,null,null,null);
        verify(template, never()).send(any(MessageCreator.class));
    }

    @Test
    public void serviceSendsDocumentAndIdForCreate() throws JMSException {
        final String xml = "The document";
        final String id = "The id";
        ReflectionTestUtils.setField(serviceImpl, "enabled", Boolean.TRUE);
        ArgumentCaptor<MessageCreator> captor = ArgumentCaptor.forClass(MessageCreator.class);

        TextMessage message = mock(TextMessage.class);
        Session session = mock(Session.class);
        when(session.createTextMessage(xml)).thenReturn(message);

        boolean created = serviceImpl.created(xml, id, "luse", "unit");

        assertTrue(created);
        verify(template, only()).send(captor.capture());
        captor.getValue().createMessage(session);
        verify(message).setStringProperty("action", "created");
        verify(message).setStringProperty("certificate-id", id);
        verify(monitoringLogService, only()).logStatisticsSent(id, "luse", "unit");
    }

    @Test
    public void serviceSendsDocumentAndIdForRevoke() throws Exception {
        final String type = "lisjp";
        final String xmlBody = "xml body";
        ReflectionTestUtils.setField(serviceImpl, "enabled", Boolean.TRUE);
        ArgumentCaptor<MessageCreator> captor = ArgumentCaptor.forClass(MessageCreator.class);
        ModuleApi moduleApi = mock(ModuleApi.class);
        when(moduleApi.getUtlatandeFromXml(xmlBody)).thenReturn(mock(Utlatande.class));

        Certificate certificate = mock(Certificate.class);
        OriginalCertificate originalCertificate = mock(OriginalCertificate.class);
        when(originalCertificate.getDocument()).thenReturn(xmlBody);
        when(certificate.getOriginalCertificate()).thenReturn(originalCertificate);
        when(certificate.getId()).thenReturn("The id");
        when(certificate.getType()).thenReturn(type);

        TextMessage message = mock(TextMessage.class);
        Session session = mock(Session.class);
        when(session.createTextMessage(xmlBody)).thenReturn(message);

        boolean revoked = serviceImpl.revoked(certificate.getOriginalCertificate().getDocument(), certificate.getId(), certificate.getType(), "unit");

        assertTrue(revoked);
        verify(template, only()).send(captor.capture());
        captor.getValue().createMessage(session);
        verify(message).setStringProperty("action", "revoked");
        verify(message).setStringProperty("certificate-id", "The id");
        verify(monitoringLogService, only()).logStatisticsRevoked("The id", certificate.getType(), "unit");
    }

    @Test
    public void serviceSendsDocumentAndIdForMessageSent() throws Exception {
        final String messageBody = "Message body";
        final String certificateId = "Certificate-id";

        ReflectionTestUtils.setField(serviceImpl, "enabled", Boolean.TRUE);
        ArgumentCaptor<MessageCreator> captor = ArgumentCaptor.forClass(MessageCreator.class);

        TextMessage message = mock(TextMessage.class);
        Session session = mock(Session.class);
        when(session.createTextMessage(messageBody)).thenReturn(message);

        boolean messageSent = serviceImpl.messageSent(messageBody, certificateId, "topic");

        assertTrue(messageSent);
        verify(template, only()).send(captor.capture());
        captor.getValue().createMessage(session);
        verify(message).setStringProperty("action", "message-sent");
        verify(message).setStringProperty("certificate-id", certificateId);
        verify(monitoringLogService, only()).logStatisticsMessageSent(certificateId, "topic");
    }

    @Test
    public void testNoJmsTemplateConfigured() throws JMSException {
        ReflectionTestUtils.setField(serviceImpl, "jmsTemplate", null);
        ReflectionTestUtils.setField(serviceImpl, "enabled", Boolean.TRUE);

        TextMessage message = mock(TextMessage.class);
        Session session = mock(Session.class);
        when(session.createTextMessage("The document")).thenReturn(message);

        boolean created = serviceImpl.created("The document", "The id", "luse", "unit");

        assertFalse(created);
        verifyZeroInteractions(template);
        verifyZeroInteractions(monitoringLogService);
    }

    @Test
    public void testJmsTemplateThrowsJmsException() throws JMSException {
        ReflectionTestUtils.setField(serviceImpl, "enabled", Boolean.TRUE);
        doThrow(mock(JmsException.class)).when(template).send(any(MessageCreator.class));

        TextMessage message = mock(TextMessage.class);
        Session session = mock(Session.class);
        when(session.createTextMessage("The document")).thenReturn(message);

        boolean created = serviceImpl.created("The document", "The id", "luse", "unit");

        assertFalse(created);
        verify(template, only()).send(any(MessageCreator.class));
        verifyZeroInteractions(monitoringLogService);
    }
}
