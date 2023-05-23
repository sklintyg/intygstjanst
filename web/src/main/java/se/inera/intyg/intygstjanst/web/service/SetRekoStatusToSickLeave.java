package se.inera.intyg.intygstjanst.web.service;

import java.time.LocalDateTime;

public interface SetRekoStatusToSickLeave {
    void set(String patientId,
             String status,
             String careProviderId,
             String careUnitId,
             String unitId,
             String staffId,
             String staffName,
             LocalDateTime sickLeaveTimestamp);
}
