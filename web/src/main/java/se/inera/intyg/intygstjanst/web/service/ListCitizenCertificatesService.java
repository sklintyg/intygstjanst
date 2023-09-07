package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;

import java.util.List;

public interface ListCitizenCertificatesService {
    List<CitizenCertificateDTO> get(String patientId,
                                    List<String> certificateTypes,
                                    List<String> units,
                                    List<CitizenCertificateStatusTypeDTO> statuses,
                                    List<String> years
    );
}
