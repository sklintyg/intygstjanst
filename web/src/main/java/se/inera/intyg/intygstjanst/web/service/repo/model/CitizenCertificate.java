package se.inera.intyg.intygstjanst.web.service.repo.model;

import lombok.Builder;
import lombok.Data;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationDTO;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CitizenCertificate {
    private String id;
    private String type;
    private String typeVersion;
    private String unitName;
    private String unitId;
    private String issuerName;
    private String additionalInfo;
    private LocalDateTime issued;
    private LocalDateTime sentDate;
    private List<CitizenCertificateRelationDTO> relations;
}
