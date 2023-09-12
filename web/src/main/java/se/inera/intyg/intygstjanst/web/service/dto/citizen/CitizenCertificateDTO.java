package se.inera.intyg.intygstjanst.web.service.dto.citizen;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CitizenCertificateDTO {
    String id;
    CitizenCertificateTypeDTO type;
    CitizenCertificateSummaryDTO summary;
    CitizenCertificateIssuerDTO issuer;
    CitizenCertificateUnitDTO unit;
    CitizenCertificateRecipientDTO recipient;
    LocalDateTime issued;
    List<CitizenCertificateRelationDTO> relations;
}
