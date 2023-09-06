package se.inera.intyg.intygstjanst.web.service.repo;

import se.inera.intyg.intygstjanst.web.integration.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;

import java.util.List;

public interface CitizenCertificatesRepository {

    List<CitizenCertificate> getCertificatesForPatient(String patientId,
                                                       List<String> certificateTypes,
                                                       List<String> units,
                                                       List<CitizenCertificateStatusTypeDTO> statuses, // Should we change it to string or keep it as a type, in that case we need new/move type in correct layer
                                                       List<String> years);
}
