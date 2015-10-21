package se.inera.certificate.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.inera.certificate.logging.LogMarkers;
import se.inera.certificate.modules.support.api.dto.Personnummer;
import se.inera.certificate.service.MonitoringLogService;

@Service
public class MonitoringLogServiceImpl implements MonitoringLogService {

    private static final String SPACE = " ";

    private static final Logger LOG = LoggerFactory.getLogger(MonitoringLogService.class);

    @Override
    public void logCertificateRegistered(String certificateId, String certificateType, String careUnit) {
        logEvent(MonitoringEvent.CERTIFICATE_REGISTERED, certificateId, certificateType, careUnit);
    }

    @Override
    public void logCertificateSent(String certificateId, String certificateType, String careUnit,
            String recipient) {
        logEvent(MonitoringEvent.CERTIFICATE_SENT, certificateId, certificateType, careUnit, recipient);
    }

    @Override
    public void logCertificateSentAndNotifiedByWiretapping(String certificateId, String certificateType, String careUnit, String recipient) {
        logEvent(MonitoringEvent.CERTIFICATE_SENT_AND_NOTIFIED_BY_WIRETAPPING, certificateId, certificateType, careUnit, recipient);
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
        logEvent(MonitoringEvent.CERTIFICATE_LISTED_BY_CITIZEN, Personnummer.getPnrHashSafe(citizenId));
    }

    @Override
    public void logCertificateListedByCare(Personnummer citizenId) {
        logEvent(MonitoringEvent.CERTIFICATE_LISTED_BY_CARE, Personnummer.getPnrHashSafe(citizenId));
    }

    @Override
    public void logCertificateStatusChanged(String certificateId, String status) {
        logEvent(MonitoringEvent.CERTIFICATE_STATUS_CHANGED, certificateId, status);
    }

    @Override
    public void logConsentGiven(Personnummer citizenId) {
        logEvent(MonitoringEvent.CONSENT_GIVEN, Personnummer.getPnrHashSafe(citizenId));
    }

    @Override
    public void logConsentRevoked(Personnummer citizenId) {
        logEvent(MonitoringEvent.CONSENT_REVOKED, Personnummer.getPnrHashSafe(citizenId));
    }

    @Override
    public void logStatisticsSent(String certificateId, String certificateType, String careUnit) {
        logEvent(MonitoringEvent.STATISTICS_SENT, certificateId, certificateType, careUnit);
    }

    @Override
    public void logStatisticsRevoked(String certificateId, String certificateType, String careUnit) {
        logEvent(MonitoringEvent.STATISTICS_REVOKED, certificateId, certificateType, careUnit);
    }

    private void logEvent(MonitoringEvent logEvent, Object... logMsgArgs) {

        StringBuilder logMsg = new StringBuilder();
        logMsg.append(logEvent.name()).append(SPACE).append(logEvent.getMessage());

        LOG.info(LogMarkers.MONITORING, logMsg.toString(), logMsgArgs);
    }

    private enum MonitoringEvent {
        CERTIFICATE_REGISTERED("Certificate '{}' with type '{}', care unit '{}' - registered"),
        CERTIFICATE_SENT("Certificate '{}' with type '{}', care unit '{}' - sent to '{}'"),
        CERTIFICATE_SENT_AND_NOTIFIED_BY_WIRETAPPING("Certificate '{}' with type '{}', care unit '{}' - sent to '{}' (notification received by wiretapping)"),
        CERTIFICATE_REVOKED("Certificate '{}' with type '{}', care unit '{}' - revoked"),
        CERTIFICATE_REVOKE_SENT("Certificate '{}' with type '{}', care unit '{}' - revoke sent to '{}'"),
        CERTIFICATE_LISTED_BY_CITIZEN("Certificates for citizen '{}' - listed by citizen"),
        CERTIFICATE_LISTED_BY_CARE("Certificates for citizen '{}' - listed by care"),
        CERTIFICATE_STATUS_CHANGED("Certificate '{}' - changed to status '{}'"),
        CONSENT_GIVEN("Consent given by citizen '{}'"),
        CONSENT_REVOKED("Consent revoked by citizen '{}'"),
        STATISTICS_SENT("Certificate '{}' with type '{}', care unit '{}' - sent to statistics"),
        STATISTICS_REVOKED("Certificate '{}' with type '{}', care unit '{}' - revoke sent to statistics");

        private String msg;

        MonitoringEvent(String msg) {
            this.msg = msg;
        }

        public String getMessage() {
            return msg;
        }
    }
}
