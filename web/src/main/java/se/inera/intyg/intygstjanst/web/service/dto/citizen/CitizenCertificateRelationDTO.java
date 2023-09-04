package se.inera.intyg.intygstjanst.web.service.dto.citizen;

import lombok.Data;

@Data
public class CitizenCertificateRelationDTO {
    String certificateId;
    String type;
    String timestamp;
}
