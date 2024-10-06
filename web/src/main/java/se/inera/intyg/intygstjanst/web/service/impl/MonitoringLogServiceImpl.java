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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.logging.LogMarkers;
import se.inera.intyg.intygstjanst.logging.MdcCloseableMap;
import se.inera.intyg.intygstjanst.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.schemas.contract.Personnummer;

@Service
public class MonitoringLogServiceImpl implements MonitoringLogService {

    private static final String SPACE = " ";

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringLogService.class);

    @Override
    public void logCertificateRegistered(String certificateId, String certificateType, String careUnit) {
        try (MdcCloseableMap mdc =
            MdcCloseableMap.builder()
                .put(MdcLogConstants.EVENT_CERTIFICATE_ID, certificateId)
                .put(MdcLogConstants.EVENT_CERTIFICATE_TYPE, certificateType)
                .put(MdcLogConstants.EVENT_CERTIFICATE_CARE_UNIT_ID, careUnit)
                .build()
        ) {
            logEvent(MonitoringEvent.CERTIFICATE_REGISTERED, certificateId, certificateType, careUnit);
        }
    }

    @Override
    public void logCertificateRetrieved(String certificateId, String certificateType, String careUnit, String partId) {
        try (MdcCloseableMap mdc =
            MdcCloseableMap.builder()
                .put(MdcLogConstants.EVENT_CERTIFICATE_ID, certificateId)
                .put(MdcLogConstants.EVENT_CERTIFICATE_TYPE, certificateType)
                .put(MdcLogConstants.EVENT_CERTIFICATE_CARE_UNIT_ID, careUnit)
                .put(MdcLogConstants.EVENT_PART_ID, partId)
                .build()
        ) {
            logEvent(MonitoringEvent.CERTIFICATE_RETRIEVED, certificateId, certificateType, careUnit, partId);
        }
    }

    @Override
    public void logCertificateSent(String certificateId, String certificateType, String careUnit,
        String recipient) {
        try (MdcCloseableMap mdc =
            MdcCloseableMap.builder()
                .put(MdcLogConstants.EVENT_CERTIFICATE_ID, certificateId)
                .put(MdcLogConstants.EVENT_CERTIFICATE_TYPE, certificateType)
                .put(MdcLogConstants.EVENT_CERTIFICATE_CARE_UNIT_ID, careUnit)
                .put(MdcLogConstants.EVENT_RECIPIENT, recipient)
                .build()
        ) {
            logEvent(MonitoringEvent.CERTIFICATE_SENT, certificateId, certificateType, careUnit, recipient);
        }
    }

    @Override
    public void logCertificateRevoked(String certificateId, String certificateType, String careUnit) {
        try (MdcCloseableMap mdc =
            MdcCloseableMap.builder()
                .put(MdcLogConstants.EVENT_CERTIFICATE_ID, certificateId)
                .put(MdcLogConstants.EVENT_CERTIFICATE_TYPE, certificateType)
                .put(MdcLogConstants.EVENT_CERTIFICATE_CARE_UNIT_ID, careUnit)
                .build()
        ) {
            logEvent(MonitoringEvent.CERTIFICATE_REVOKED, certificateId, certificateType, careUnit);
        }
    }

    @Override
    public void logCertificateRevokeSent(String certificateId, String certificateType, String careUnit, String recipientId) {
        try (MdcCloseableMap mdc =
            MdcCloseableMap.builder()
                .put(MdcLogConstants.EVENT_CERTIFICATE_ID, certificateId)
                .put(MdcLogConstants.EVENT_CERTIFICATE_TYPE, certificateType)
                .put(MdcLogConstants.EVENT_CERTIFICATE_CARE_UNIT_ID, careUnit)
                .put(MdcLogConstants.EVENT_RECIPIENT, recipientId)
                .build()
        ) {
            logEvent(MonitoringEvent.CERTIFICATE_REVOKE_SENT, certificateId, certificateType, careUnit, recipientId);
        }
    }

    @Override
    public void logCertificateListedByCitizen(Personnummer citizenId) {
        final var hashedCitizenId = Personnummer.getPersonnummerHashSafe(citizenId);
        try (MdcCloseableMap mdc =
            MdcCloseableMap.builder()
                .put(MdcLogConstants.USER_ID, hashedCitizenId)
                .build()
        ) {
            logEvent(MonitoringEvent.CERTIFICATE_LISTED_BY_CITIZEN, hashedCitizenId);
        }
    }

    @Override
    public void logCertificateListedByCare(Personnummer citizenId) {
        final var hashedCitizenId = Personnummer.getPersonnummerHashSafe(citizenId);
        try (MdcCloseableMap mdc =
            MdcCloseableMap.builder()
                .put(MdcLogConstants.USER_ID, hashedCitizenId)
                .build()
        ) {
            logEvent(MonitoringEvent.CERTIFICATE_LISTED_BY_CARE, hashedCitizenId);
        }
    }

    @Override
    public void logCertificateStatusChanged(String certificateId, String status) {
        try (MdcCloseableMap mdc =
            MdcCloseableMap.builder()
                .put(MdcLogConstants.EVENT_CERTIFICATE_ID, certificateId)
                .build()
        ) {
            logEvent(MonitoringEvent.CERTIFICATE_STATUS_CHANGED, certificateId, status);
        }
    }

    @Override
    public void logStatisticsCreated(String certificateId, String certificateType, String careUnit) {
        try (MdcCloseableMap mdc =
            MdcCloseableMap.builder()
                .put(MdcLogConstants.EVENT_CERTIFICATE_ID, certificateId)
                .put(MdcLogConstants.EVENT_CERTIFICATE_TYPE, certificateType)
                .put(MdcLogConstants.EVENT_CERTIFICATE_CARE_UNIT_ID, careUnit)
                .build()
        ) {
            logEvent(MonitoringEvent.STATISTICS_CREATED, certificateId, certificateType, careUnit);
        }
    }

    @Override
    public void logStatisticsSent(String certificateId, String certificateType, String careUnit, String recipient) {
        try (MdcCloseableMap mdc =
            MdcCloseableMap.builder()
                .put(MdcLogConstants.EVENT_CERTIFICATE_ID, certificateId)
                .put(MdcLogConstants.EVENT_CERTIFICATE_TYPE, certificateType)
                .put(MdcLogConstants.EVENT_CERTIFICATE_CARE_UNIT_ID, careUnit)
                .put(MdcLogConstants.EVENT_RECIPIENT, recipient)
                .build()
        ) {
            logEvent(MonitoringEvent.STATISTICS_SENT, certificateId, certificateType, careUnit, recipient);
        }
    }

    @Override
    public void logStatisticsRevoked(String certificateId, String certificateType, String careUnit) {
        try (MdcCloseableMap mdc =
            MdcCloseableMap.builder()
                .put(MdcLogConstants.EVENT_CERTIFICATE_ID, certificateId)
                .put(MdcLogConstants.EVENT_CERTIFICATE_TYPE, certificateType)
                .put(MdcLogConstants.EVENT_CERTIFICATE_CARE_UNIT_ID, careUnit)
                .build()
        ) {
            logEvent(MonitoringEvent.STATISTICS_REVOKED, certificateId, certificateType, careUnit);
        }
    }

    @Override
    public void logStatisticsMessageSent(String certificateId, String topic) {
        try (MdcCloseableMap mdc =
            MdcCloseableMap.builder()
                .put(MdcLogConstants.EVENT_CERTIFICATE_ID, certificateId)
                .build()
        ) {
            logEvent(MonitoringEvent.STATISTICS_MESSAGE_SENT, topic, certificateId);
        }
    }

    @Override
    public void logSendMessageToCareReceived(String certificateId, String careUnit) {
        try (MdcCloseableMap mdc =
            MdcCloseableMap.builder()
                .put(MdcLogConstants.EVENT_CERTIFICATE_ID, certificateId)
                .put(MdcLogConstants.EVENT_CERTIFICATE_CARE_UNIT_ID, careUnit)
                .build()
        ) {
            logEvent(MonitoringEvent.SEND_MESSAGE_TO_CARE_RECEIVED, certificateId, careUnit);
        }
    }

    @Override
    public void logSendMessageToRecipient(String certificateId, String recipient) {
        try (MdcCloseableMap mdc =
            MdcCloseableMap.builder()
                .put(MdcLogConstants.EVENT_CERTIFICATE_ID, certificateId)
                .put(MdcLogConstants.EVENT_RECIPIENT, recipient)
                .build()
        ) {
            logEvent(MonitoringEvent.SEND_MESSAGE_TO_RECIPIENT, certificateId, recipient);
        }
    }

    @Override
    public void logApprovedReceiversRegistered(String receivers, String certificateId) {
        try (MdcCloseableMap mdc =
            MdcCloseableMap.builder()
                .put(MdcLogConstants.EVENT_CERTIFICATE_ID, certificateId)
                .build()
        ) {
            logEvent(MonitoringEvent.APPROVED_RECEIVER_REGISTERED, receivers, certificateId);
        }
    }

    @Override
    public void logTestCertificateErased(String certificateId, String careUnit) {
        try (MdcCloseableMap mdc =
            MdcCloseableMap.builder()
                .put(MdcLogConstants.EVENT_CERTIFICATE_ID, certificateId)
                .put(MdcLogConstants.EVENT_CERTIFICATE_CARE_UNIT_ID, careUnit)
                .build()
        ) {
            logEvent(MonitoringEvent.TEST_CERTIFICATE_ERASED, certificateId, careUnit);
        }
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
