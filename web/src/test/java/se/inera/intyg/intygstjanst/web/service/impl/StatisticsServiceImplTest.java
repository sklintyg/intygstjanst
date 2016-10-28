/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
import static org.mockito.Mockito.*;

import javax.jms.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
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
        serviceImpl.created(null, null, null, null);
        verify(template, never()).send(any(MessageCreator.class));
    }

    @Test
    public void disabledServiceDoesNothingOnRevoked() {
        serviceImpl.revoked(null);
        verify(template, never()).send(any(MessageCreator.class));
    }

    @Test
    public void serviceSendsDocumentAndIdForCreate() throws JMSException {
        final String xml = "The document";
        final String id = "The id";
        org.springframework.test.util.ReflectionTestUtils.setField(serviceImpl, "enabled", Boolean.TRUE);
        ArgumentCaptor<MessageCreator> captor = ArgumentCaptor.forClass(MessageCreator.class);

        TextMessage message = mock(TextMessage.class);
        Session session = mock(Session.class);
        when(session.createTextMessage(xml)).thenReturn(message);

        serviceImpl.created(xml, id, "luse", "unit");

        verify(template, only()).send(captor.capture());
        captor.getValue().createMessage(session);
        verify(message).setStringProperty("action", "created");
        verify(message).setStringProperty("certificate-id", "The id");
    }

    @Test
    public void serviceSendsDocumentAndIdForRevoke() throws Exception {
        final String type = "lisjp";
        final String xmlBody = "xml body";
        org.springframework.test.util.ReflectionTestUtils.setField(serviceImpl, "enabled", Boolean.TRUE);
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

        serviceImpl.revoked(certificate);

        verify(template, only()).send(captor.capture());
        captor.getValue().createMessage(session);
        verify(message).setStringProperty("action", "revoked");
        verify(message).setStringProperty("certificate-id", "The id");
    }
}
