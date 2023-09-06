package se.inera.intyg.intygstjanst.web.service.repo;

import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;

import java.util.List;

public interface CitizenCertificatesRepository {

    List<CitizenCertificate> getCertificatesForPatient(String patientId);
}
