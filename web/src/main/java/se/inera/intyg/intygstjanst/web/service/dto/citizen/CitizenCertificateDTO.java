package se.inera.intyg.intygstjanst.web.service.dto.citizen;

import lombok.Data;

import java.util.List;

@Data
public class CitizenCertificateDTO {
    String id;
    CitizenCertificateTypeDTO type;
    CitizenCertificateSummaryDTO summary;
    CitizenCertificateIssuerDTO issuer;
    CitizenCertificateUnitDTO unit;
    CitizenCertificateRecipientDTO recipient;
    String issued;
    List<CitizenCertificateRelationDTO> relations;
}
