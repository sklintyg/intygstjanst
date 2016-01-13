package se.inera.certificate.service;

import se.inera.certificate.modules.support.api.dto.Personnummer;

public interface MonitoringLogService {

    void logCertificateRegistered(String certificateId, String certificateType, String careUnit);
    void logCertificateSent(String certificateId, String certificateType, String careUnit, String recipient);
    void logCertificateSentAndNotifiedByWiretapping(String certificateId, String certificateType, String careUnit, String recipient);
    void logCertificateRevoked(String certificateId, String certificateType, String careUnit);
    void logCertificateRevokeSent(String certificateId, String certificateType, String careUnit, String recipientId);
    void logCertificateListedByCitizen(Personnummer citizenId);
    void logCertificateListedByCare(Personnummer citizenId);
    void logCertificateStatusChanged(String certificateId, String status);
    void logConsentGiven(Personnummer citizenId);
    void logConsentRevoked(Personnummer citizenId);
    void logStatisticsSent(String certificateId, String certificateType, String careUnit);
    void logStatisticsRevoked(String certificateId, String certificateType, String careUnit);
}
