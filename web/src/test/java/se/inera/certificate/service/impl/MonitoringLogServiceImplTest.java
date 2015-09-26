package se.inera.certificate.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import se.inera.certificate.service.MonitoringLogService;

@RunWith(MockitoJUnitRunner.class)
public class MonitoringLogServiceImplTest {
    
    private static final String CERTIFICATE_ID = "CERTIFICATE_ID";
    private static final String CERTIFICATE_TYPE = "CERTIFICATE_TYPE";
    private static final String CARE_UNIT = "CARE_UNIT";
    private static final String RECIPIENT = "RECIPIENT"; 
    private static final String CITIZEN = "CITIZEN"; 
    private static final String STATUS = "STATUS"; 

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;
    
    MonitoringLogService logService = new MonitoringLogServiceImpl();

    @Before
    public void setup() {
        
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(mockAppender);
    }
    
    @After
    public void teardown() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAppender(mockAppender);
    }
    
    @Test
    public void shouldLogCertificateRegistered() {
        logService.logCertificateRegistered(CERTIFICATE_ID, CERTIFICATE_TYPE, CARE_UNIT);
        verifyLog(Level.INFO, "CERTIFICATE_REGISTERED Certificate 'CERTIFICATE_ID' with type 'CERTIFICATE_TYPE', care unit 'CARE_UNIT' - registered");
    }

    private void verifyLog(Level logLevel, String logMessage) {
        // Verify and capture logging interaction
        verify(mockAppender).doAppend(captorLoggingEvent.capture());
        final LoggingEvent loggingEvent = captorLoggingEvent.getValue();

        // Verify log
        assertThat(loggingEvent.getLevel(), is(logLevel));
        assertThat(loggingEvent.getFormattedMessage(), 
                is(logMessage));
    }

    @Test
    public void shouldLogCertificateSent() {
        logService.logCertificateSent(CERTIFICATE_ID, CERTIFICATE_TYPE, CARE_UNIT, RECIPIENT);
        verifyLog(Level.INFO, "CERTIFICATE_SENT Certificate 'CERTIFICATE_ID' with type 'CERTIFICATE_TYPE', care unit 'CARE_UNIT' - sent to 'RECIPIENT'");
    }

    @Test
    public void shouldLogCertificateSentAndNotifiedByWiretapping() {
        logService.logCertificateSentAndNotifiedByWiretapping(CERTIFICATE_ID, CERTIFICATE_TYPE, CARE_UNIT, RECIPIENT);
        verifyLog(Level.INFO, "CERTIFICATE_SENT_AND_NOTIFIED_BY_WIRETAPPING Certificate 'CERTIFICATE_ID' with type 'CERTIFICATE_TYPE', care unit 'CARE_UNIT' - sent to 'RECIPIENT' (notification received by wiretapping)");
    }

    @Test
    public void shouldLogCertificateRevoked() {
        logService.logCertificateRevoked(CERTIFICATE_ID, CERTIFICATE_TYPE, CARE_UNIT);
        verifyLog(Level.INFO, "CERTIFICATE_REVOKED Certificate 'CERTIFICATE_ID' with type 'CERTIFICATE_TYPE', care unit 'CARE_UNIT' - revoked");
    }

    @Test
    public void shouldLogCertificateRevokeSent() {
        logService.logCertificateRevokeSent(CERTIFICATE_ID, CERTIFICATE_TYPE, CARE_UNIT, RECIPIENT);
        verifyLog(Level.INFO, "CERTIFICATE_REVOKE_SENT Certificate 'CERTIFICATE_ID' with type 'CERTIFICATE_TYPE', care unit 'CARE_UNIT' - revoke sent to 'RECIPIENT'");
    }

    @Test
    public void shouldLogCertificateListedByCitizen() {
        logService.logCertificateListedByCitizen(CITIZEN);
        verifyLog(Level.INFO, "CERTIFICATE_LISTED_BY_CITIZEN Certificates for citizen 'CITIZEN' - listed by citizen");
    }

    @Test
    public void shouldLogCertificateListedByCare() {
        logService.logCertificateListedByCare(CITIZEN);
        verifyLog(Level.INFO, "CERTIFICATE_LISTED_BY_CARE Certificates for citizen 'CITIZEN' - listed by care");
    }

    @Test
    public void shouldLogCertificateStatusChanged() {
        logService.logCertificateStatusChanged(CERTIFICATE_ID, STATUS);
        verifyLog(Level.INFO, "CERTIFICATE_STATUS_CHANGED Certificate 'CERTIFICATE_ID' - changed to status 'STATUS'");
    }

    @Test
    public void shouldLogConsentGiven() {
        logService.logConsentGiven(CITIZEN);
        verifyLog(Level.INFO, "CONSENT_GIVEN Consent given by citizen 'CITIZEN'");
    }

    @Test
    public void shouldLogConsentRevoked() {
        logService.logConsentRevoked(CITIZEN);
        verifyLog(Level.INFO, "CONSENT_REVOKED Consent revoked by citizen 'CITIZEN'");
    }

    @Test
    public void shouldLogStatisticsSent() {
        logService.logStatisticsSent(CERTIFICATE_ID, CERTIFICATE_TYPE, CARE_UNIT);
        verifyLog(Level.INFO, "STATISTICS_SENT Certificate 'CERTIFICATE_ID' with type 'CERTIFICATE_TYPE', care unit 'CARE_UNIT' - sent to statistics");
    }

    @Test
    public void shouldLogStatisticsRevoked() {
        logService.logStatisticsRevoked(CERTIFICATE_ID, CERTIFICATE_TYPE, CARE_UNIT);
        verifyLog(Level.INFO, "STATISTICS_REVOKED Certificate 'CERTIFICATE_ID' with type 'CERTIFICATE_TYPE', care unit 'CARE_UNIT' - revoke sent to statistics");
    }
}
