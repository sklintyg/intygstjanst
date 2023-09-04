package se.inera.intyg.intygstjanst.persistence.model.dao;

import java.util.List;

public abstract class CitizenCertificatesDao {

    public abstract List<CitizenCertificate> getCertificatesForPatient(String patientId,
                                                                       List<String> certificateTypes,
                                                                       List<String> units,
                                                                       List<String> statuses, // Should we change it to string or keep it as a type, in that case we need new/move type in correct layer
                                                                       List<String> years);
}
