package se.inera.intyg.intygstjanst.web.service;

public interface SetRekoStatusToSickLeave {
    void set(String patientId,
             String status,
             String careProviderId,
             String careUnitId,
             String unitId,
             int maxCertificateGap,
             int maxDaysSinceSickLeaveCompleted);
}
