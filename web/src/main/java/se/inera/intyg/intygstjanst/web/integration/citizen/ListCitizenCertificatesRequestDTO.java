package se.inera.intyg.intygstjanst.web.integration.citizen;

import lombok.Data;

import java.util.List;

@Data
public class ListCitizenCertificatesRequestDTO {
    private String patientId;
    private List<CitizenCertificateStatusTypeDTO> statuses;
    private List<String> units;
    private List<String> certificateTypes;
    private List<String> years;
}
