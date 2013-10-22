package se.inera.certificate.migration.listeners;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import se.inera.certificate.migration.model.OriginalCertificate;
import se.inera.certificate.migration.processors.CertificateProcessingException;

import ch.qos.logback.core.Appender;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyString;

public class CertificateProcessingSkipListenerTest {

    private CertificateProcessingSkipListener skipListener;
    
    public CertificateProcessingSkipListenerTest() {
        this.skipListener = new CertificateProcessingSkipListener();
    }

    
    @Test
    public void testOnSkipInProcess() {
        
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        final Appender mockAppender = mock(Appender.class);
        when(mockAppender.getName()).thenReturn("MOCK");
        root.addAppender(mockAppender);
        
        OriginalCertificate cert = new OriginalCertificate();
        cert.setCertificateId("abc123");
        cert.setOriginalCertificateId(123);
        
        CertificateProcessingException cpe = new CertificateProcessingException("Something went wrong");
        
        skipListener.onSkipInProcess(cert, cpe);
        
        verify(mockAppender).doAppend(anyString());
        
    }
}
