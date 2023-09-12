package se.inera.intyg.intygstjanst.web.service.dto.citizen;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@Builder
public class CitizenCertificatesRequestDTO {
    private String patientId;
    @Builder.Default
    private List<CitizenCertificateStatusTypeDTO> statuses = Collections.emptyList();
    @Builder.Default
    private List<String> units = Collections.emptyList();
    @Builder.Default
    private List<String> certificateTypes = Collections.emptyList();
    @Builder.Default
    private List<String> years = Collections.emptyList();
}
