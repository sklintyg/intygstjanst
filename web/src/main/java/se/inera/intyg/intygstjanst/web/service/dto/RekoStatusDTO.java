package se.inera.intyg.intygstjanst.web.service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RekoStatusDTO {
    String patientId;
    RekoStatusType status;
    LocalDateTime registrationTimestamp;
    LocalDateTime sickLeaveTimestamp;
    String careProviderId;
    String careUnitId;
    String unitId;

}
