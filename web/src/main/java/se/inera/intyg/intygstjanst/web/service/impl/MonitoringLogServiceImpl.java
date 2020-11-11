/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.monitoring.logging.LogMarkers;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.schemas.contract.Personnummer;

@Service
public class MonitoringLogServiceImpl implements MonitoringLogService {

    private static final String SPACE = " ";

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringLogService.class);

    @Override
    public void logCertificateRegistered(String certificateId, String certificateType, String careUnit) {
        logEvent(MonitoringEvent.CERTIFICATE_REGISTERED, certificateId, certificateType, careUnit);
    }

    @Override
    public void logCertificateRetrieved(String certificateId, String certificateType, String careUnit, String partId) {
        logEvent(MonitoringEvent.CERTIFICATE_RETRIEVED, certificateId, certificateType, careUnit, partId);
    }

    @Override
    public void logCertificateSent(String certificateId, String certificateType, String careUnit,
        String recipient) {
        logEvent(MonitoringEvent.CERTIFICATE_SENT, certificateId, certificateType, careUnit, recipient);
    }

    @Override
    public void logCertificateRevoked(String certificateId, String certificateType, String careUnit) {
        logEvent(MonitoringEvent.CERTIFICATE_REVOKED, certificateId, certificateType, careUnit);
    }

    @Override
    public void logCertificateRevokeSent(String certificateId, String certificateType, String careUnit, String recipientId) {
        logEvent(MonitoringEvent.CERTIFICATE_REVOKE_SENT, certificateId, certificateType, careUnit, recipientId);
    }

    @Override
    public void logCertificateListedByCitizen(Personnummer citizenId) {
        logEvent(MonitoringEvent.CERTIFICATE_LISTED_BY_CITIZEN, Personnummer.getPersonnummerHashSafe(citizenId));
    }

    @Override
    public void logCertificateListedByCare(Personnummer citizenId) {
        logEvent(MonitoringEvent.CERTIFICATE_LISTED_BY_CARE, Personnummer.getPersonnummerHashSafe(citizenId));
    }

    @Override
    public void logCertificateStatusChanged(String certificateId, String status) {
        logEvent(MonitoringEvent.CERTIFICATE_STATUS_CHANGED, certificateId, status);
    }

    @Override
    public void logStatisticsCreated(String certificateId, String certificateType, String careUnit) {
        logEvent(MonitoringEvent.STATISTICS_CREATED, certificateId, certificateType, careUnit);
    }

    @Override
    public void logStatisticsSent(String certificateId, String certificateType, String careUnit, String recipient) {
        logEvent(MonitoringEvent.STATISTICS_SENT, certificateId, certificateType, careUnit, recipient);
    }

    @Override
    public void logStatisticsRevoked(String certificateId, String certificateType, String careUnit) {
        logEvent(MonitoringEvent.STATISTICS_REVOKED, certificateId, certificateType, careUnit);
    }

    @Override
    public void logStatisticsMessageSent(String certificateId, String topic) {
        logEvent(MonitoringEvent.STATISTICS_MESSAGE_SENT, topic, certificateId);
    }

    @Override
    public void logSendMessageToCareReceived(String intygsId, String careUnit) {
        logEvent(MonitoringEvent.SEND_MESSAGE_TO_CARE_RECEIVED, intygsId, careUnit);
    }

    @Override
    public void logSendMessageToRecipient(String intygsId, String recipient) {
        logEvent(MonitoringEvent.SEND_MESSAGE_TO_RECIPIENT, intygsId, recipient);
    }

    @Override
    public void logApprovedReceiversRegistered(String receivers, String intygsId) {
        logEvent(MonitoringEvent.APPROVED_RECEIVER_REGISTERED, receivers, intygsId);
    }

    @Override
    public void logTestCertificateErased(String certificateId, String careUnit) {
        logEvent(MonitoringEvent.TEST_CERTIFICATE_ERASED, certificateId, careUnit);
    }

    private void logEvent(MonitoringEvent logEvent, Object... logMsgArgs) {

        StringBuilder logMsg = new StringBuilder();
        logMsg.append(logEvent.name()).append(SPACE).append(logEvent.getMessage());

        LOG.info(LogMarkers.MONITORING, logMsg.toString(), logMsgArgs);
    }

    private enum MonitoringEvent {
        CERTIFICATE_REGISTERED("Certificate '{}' with type '{}', care unit '{}' - registered"),
        CERTIFICATE_RETRIEVED("Certificate '{}' with type '{}', care unit '{}' - retrieved by part '{}'"),
        CERTIFICATE_SENT("Certificate '{}' with type '{}', care unit '{}' - sent to '{}'"),
        CERTIFICATE_REVOKED("Certificate '{}' with type '{}', care unit '{}' - revoked"),
        CERTIFICATE_REVOKE_SENT("Certificate '{}' with type '{}', care unit '{}' - revoke sent to '{}'"),
        CERTIFICATE_LISTED_BY_CITIZEN("Certificates for citizen '{}' - listed by citizen"),
        CERTIFICATE_LISTED_BY_CARE("Certificates for citizen '{}' - listed by care"),
        CERTIFICATE_STATUS_CHANGED("Certificate '{}' - changed to status '{}'"),
        STATISTICS_CREATED("Certificate '{}' with type '{}', care unit '{}' - sent to statistics"),
        STATISTICS_SENT("Certificate '{}' with type '{}', care unit '{}', sent to '{}' - sent to statistics"),
        STATISTICS_REVOKED("Certificate '{}' with type '{}', care unit '{}' - revoke sent to statistics"),
        STATISTICS_MESSAGE_SENT("Message with topic '{}' for certificate '{}' - sent to statistics"),
        SEND_MESSAGE_TO_CARE_RECEIVED("Message with id '{}', care unit recipient '{}' - was received and forwarded to its recipient."),
        SEND_MESSAGE_TO_RECIPIENT("Message with id '{}' sent to recipient '{}'"),
        APPROVED_RECEIVER_REGISTERED("Approved receiver '{}' registered for certificate '{}'"),
        TEST_CERTIFICATE_ERASED("Test certificate '{}' on care unit '{}' was erased");

        private final String msg;

        MonitoringEvent(String msg) {
            this.msg = msg;
        }

        public String getMessage() {
            return msg;
        }
    }
}
