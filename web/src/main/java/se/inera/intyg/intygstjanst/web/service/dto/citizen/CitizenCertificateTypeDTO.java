package se.inera.intyg.intygstjanst.web.service.dto.citizen;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CitizenCertificateTypeDTO {
    String id;
    String name;
}
