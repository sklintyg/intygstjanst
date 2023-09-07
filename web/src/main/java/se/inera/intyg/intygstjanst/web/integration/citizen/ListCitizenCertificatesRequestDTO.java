package se.inera.intyg.intygstjanst.web.integration.citizen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateStatusTypeDTO;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListCitizenCertificatesRequestDTO {
    private String patientId;
    private List<CitizenCertificateStatusTypeDTO> statuses;
    private List<String> units;
    private List<String> certificateTypes;
    private List<String> years;
}
