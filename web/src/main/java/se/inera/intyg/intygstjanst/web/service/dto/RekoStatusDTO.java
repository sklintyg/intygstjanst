package se.inera.intyg.intygstjanst.web.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RekoStatusDTO {
    String patientId;
    RekoStatusType status;
    LocalDateTime registrationTimestamp;
    LocalDateTime sickLeaveTimestamp;
    String careProviderId;
    String careUnitId;
    String unitId;

}
