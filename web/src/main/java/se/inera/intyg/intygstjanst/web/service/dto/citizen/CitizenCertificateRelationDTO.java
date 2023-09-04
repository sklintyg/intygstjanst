package se.inera.intyg.intygstjanst.web.service.dto.citizen;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CitizenCertificateRelationDTO {
    String certificateId;
    CitizenCertificateRelationType type;
    String timestamp;
}
