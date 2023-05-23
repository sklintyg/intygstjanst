package se.inera.intyg.intygstjanst.web.integration.reko;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SetRekoStatusToSickLeaveRequestDTO {
    String patientId;
    String status;
    String careProviderId;
    String careUnitId;
    String unitId;
    int maxCertificateGap;
    int maxDaysSinceSickLeaveCompleted;
}
