package se.inera.intyg.intygstjanst.web.service.dto.citizen;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CitizenCertificateRecipientDTO {
    String id;
    String name;
    LocalDateTime sent;
}
