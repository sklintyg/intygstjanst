package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.web.integration.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;

import java.util.List;

public interface CitizenCertificatesRepository {

    List<CitizenCertificateDTO> getCertificatesForPatient(String patientId,
                                                                 List<String> certificateTypes,
                                                                 List<String> units,
                                                                 List<CitizenCertificateStatusTypeDTO> statuses, // Should we change it to string or keep it as a type, in that case we need new/move type in correct layer
                                                                 List<String> years);
}
