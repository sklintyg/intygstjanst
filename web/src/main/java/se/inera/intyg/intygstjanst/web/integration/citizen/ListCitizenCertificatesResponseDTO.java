package se.inera.intyg.intygstjanst.web.integration.citizen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListCitizenCertificatesResponseDTO {
    private List<CitizenCertificateDTO> content;
}
