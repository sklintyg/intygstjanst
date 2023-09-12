package se.inera.intyg.intygstjanst.web.service.dto.citizen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitizenCertificatesRequestDTO {
    private String patientId;
    private List<CitizenCertificateStatusTypeDTO> statuses = Collections.emptyList();
    private List<String> units = Collections.emptyList();
    private List<String> certificateTypes = Collections.emptyList();
    private List<String> years = Collections.emptyList();
}
