package se.inera.intyg.intygstjanst.persistence.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CitizenCertificatesDao extends JpaRepository<CitizenCertificate, String> {

    @Query("SELECT c FROM CitizenCertificate c WHERE c.patientId = :patientId AND c.type IN :certificateTypes AND c.unitId IN :units")
    List<CitizenCertificate> findByPatientId(String patientId, List<String> certificateTypes,
                                                       List<String> units,
                                                       List<String> statuses,
                                                       List<String> years);
}
