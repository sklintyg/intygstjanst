package se.inera.certificate.service;

public interface MonitoringLogService {

    void logCertificateRegistered(String certificateId, String certificateType, String careUnit);
    void logCertificateSent(String certificateId, String certificateType, String careUnit, String recipient);
    void logCertificateSentAndNotifiedByWiretapping(String certificateId, String certificateType, String careUnit, String recipient);
    void logCertificateRevoked(String certificateId, String certificateType, String careUnit);
    void logCertificateRevokeSent(String certificateId, String certificateType, String careUnit, String recipientId);
    void logCertificateListedByCitizen(String citizenId);
    void logCertificateListedByCare(String citizenId);
    void logCertificateStatusChanged(String certificateId, String status);
    void logConsentGiven(String citizenId);
    void logConsentRevoked(String citizenId);
    void logStatisticsSent(String certificateId, String certificateType, String careUnit);
    void logStatisticsRevoked(String certificateId, String certificateType, String careUnit);
}
