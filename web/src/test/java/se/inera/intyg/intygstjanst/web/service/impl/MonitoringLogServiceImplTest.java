/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.schemas.contract.Personnummer;

@RunWith(MockitoJUnitRunner.class)
public class MonitoringLogServiceImplTest {

    private static final String CERTIFICATE_ID = "CERTIFICATE_ID";
    private static final String CERTIFICATE_TYPE = "CERTIFICATE_TYPE";
    private static final String CARE_UNIT = "CARE_UNIT";
    private static final String PART = "PART";
    private static final String RECIPIENT = "RECIPIENT";
    private static final String CITIZEN = "191212121212";
    private static final String STATUS = "STATUS";
    private static final String MESSAGE_ID = "MESSAGE_ID";
    private static final String TOPIC = "TOPIC";
    private MonitoringLogService logService = new MonitoringLogServiceImpl();

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

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
        verifyLog(Level.INFO,
            "CERTIFICATE_REGISTERED Certificate 'CERTIFICATE_ID' with type 'CERTIFICATE_TYPE', care unit 'CARE_UNIT' - registered");
    }

    @Test
    public void shouldLogCertificateRetrieved() {
        logService.logCertificateRetrieved(CERTIFICATE_ID, CERTIFICATE_TYPE, CARE_UNIT, PART);
        verifyLog(Level.INFO,
            "CERTIFICATE_RETRIEVED Certificate 'CERTIFICATE_ID' with type 'CERTIFICATE_TYPE', care unit 'CARE_UNIT' - retrieved by part 'PART'");
    }

    private void verifyLog(Level logLevel, String logMessage) {
        // Verify and capture logging interaction
        verify(mockAppender).doAppend(captorLoggingEvent.capture());
        final LoggingEvent loggingEvent = captorLoggingEvent.getValue();

        // Verify log
        assertThat(loggingEvent.getLevel(), equalTo(logLevel));
        assertThat(loggingEvent.getFormattedMessage(),
            equalTo(logMessage));
    }

    @Test
    public void shouldLogCertificateSent() {
        logService.logCertificateSent(CERTIFICATE_ID, CERTIFICATE_TYPE, CARE_UNIT, RECIPIENT);
        verifyLog(Level.INFO,
            "CERTIFICATE_SENT Certificate 'CERTIFICATE_ID' with type 'CERTIFICATE_TYPE', care unit 'CARE_UNIT' - sent to 'RECIPIENT'");
    }

    @Test
    public void shouldLogCertificateRevoked() {
        logService.logCertificateRevoked(CERTIFICATE_ID, CERTIFICATE_TYPE, CARE_UNIT);
        verifyLog(Level.INFO,
            "CERTIFICATE_REVOKED Certificate 'CERTIFICATE_ID' with type 'CERTIFICATE_TYPE', care unit 'CARE_UNIT' - revoked");
    }

    @Test
    public void shouldLogCertificateRevokeSent() {
        logService.logCertificateRevokeSent(CERTIFICATE_ID, CERTIFICATE_TYPE, CARE_UNIT, RECIPIENT);
        verifyLog(Level.INFO,
            "CERTIFICATE_REVOKE_SENT Certificate 'CERTIFICATE_ID' with type 'CERTIFICATE_TYPE', care unit 'CARE_UNIT' - revoke sent to 'RECIPIENT'");
    }

    @Test
    public void shouldLogCertificateListedByCitizen() {
        final Personnummer citizenId = createPnr(CITIZEN);
        logService.logCertificateListedByCitizen(citizenId);
        verifyLog(Level.INFO,
            "CERTIFICATE_LISTED_BY_CITIZEN Certificates for citizen '" + citizenId.getPersonnummerHash() + "' - listed by citizen");
    }

    @Test
    public void shouldLogCertificateListedByCare() {
        final Personnummer citizenId = createPnr(CITIZEN);
        logService.logCertificateListedByCare(citizenId);
        verifyLog(Level.INFO,
            "CERTIFICATE_LISTED_BY_CARE Certificates for citizen '" + citizenId.getPersonnummerHash() + "' - listed by care");
    }

    @Test
    public void shouldLogCertificateStatusChanged() {
        logService.logCertificateStatusChanged(CERTIFICATE_ID, STATUS);
        verifyLog(Level.INFO, "CERTIFICATE_STATUS_CHANGED Certificate 'CERTIFICATE_ID' - changed to status 'STATUS'");
    }

    @Test
    public void shouldLogStatisticsCreated() {
        logService.logStatisticsCreated(CERTIFICATE_ID, CERTIFICATE_TYPE, CARE_UNIT);
        verifyLog(Level.INFO,
            "STATISTICS_CREATED Certificate 'CERTIFICATE_ID' with type 'CERTIFICATE_TYPE', care unit 'CARE_UNIT' - sent to statistics");
    }

    @Test
    public void shouldLogStatisticsSent() {
        this.logService.logStatisticsSent(CERTIFICATE_ID, CERTIFICATE_TYPE, CARE_UNIT, RECIPIENT);
        verifyLog(Level.INFO,
            "STATISTICS_SENT Certificate 'CERTIFICATE_ID' with type 'CERTIFICATE_TYPE', care unit 'CARE_UNIT', sent to 'RECIPIENT' - sent to statistics");
    }

    @Test
    public void shouldLogStatisticsRevoked() {
        logService.logStatisticsRevoked(CERTIFICATE_ID, CERTIFICATE_TYPE, CARE_UNIT);
        verifyLog(Level.INFO,
            "STATISTICS_REVOKED Certificate 'CERTIFICATE_ID' with type 'CERTIFICATE_TYPE', care unit 'CARE_UNIT' - revoke sent to statistics");
    }

    @Test
    public void shouldLogStatisticsMessageSent() {
        logService.logStatisticsMessageSent(CERTIFICATE_ID, TOPIC);
        verifyLog(Level.INFO, "STATISTICS_MESSAGE_SENT Message with topic 'TOPIC' for certificate 'CERTIFICATE_ID' - sent to statistics");
    }

    @Test
    public void shouldLogSendMessageToCareReceived() {
        logService.logSendMessageToCareReceived(MESSAGE_ID, CARE_UNIT);
        verifyLog(Level.INFO,
            "SEND_MESSAGE_TO_CARE_RECEIVED Message with id 'MESSAGE_ID', care unit recipient 'CARE_UNIT' - was received and forwarded to its recipient.");
    }

    private Personnummer createPnr(String pnr) {
        return Personnummer.createPersonnummer(pnr)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer"));
    }

}
