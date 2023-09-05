package se.inera.intyg.intygstjanst.web.service.dto.citizen;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CitizenCertificateUnitDTO {
    String id;
    String name;
}
