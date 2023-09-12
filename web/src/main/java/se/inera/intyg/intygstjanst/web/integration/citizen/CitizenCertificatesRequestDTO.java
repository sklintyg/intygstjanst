package se.inera.intyg.intygstjanst.web.integration.citizen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateStatusTypeDTO;

import java.util.Collections;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CitizenCertificatesRequestDTO {
    private String patientId;
    private List<CitizenCertificateStatusTypeDTO> statuses = Collections.emptyList();
    private List<String> units = Collections.emptyList();
    private List<String> certificateTypes = Collections.emptyList();
    private List<String> years = Collections.emptyList();
}
