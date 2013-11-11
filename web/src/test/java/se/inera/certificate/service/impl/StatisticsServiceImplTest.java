package se.inera.certificate.service.impl;

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

import se.inera.certificate.model.dao.Certificate;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsServiceImplTest {

    @Mock
    private JmsTemplate template = mock(JmsTemplate.class);

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
        verify(message).setJMSCorrelationID("C:The id");
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
        verify(message).setJMSCorrelationID("R:The id");
    }
}
