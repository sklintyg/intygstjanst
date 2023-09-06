package se.inera.intyg.intygstjanst.web.service.repo;

import org.springframework.stereotype.Repository;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.persistence.model.dao.*;
import se.inera.intyg.intygstjanst.web.integration.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificateConverter;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CitizenCertificatesRepositoryImpl implements CitizenCertificatesRepository {
    private final RelationDao relationDao;
    private final CitizenCertificateConverter citizenCertificateConverter;
    private final CertificateRepository certificateRepository;

    public CitizenCertificatesRepositoryImpl(RelationDao relationDao,
                                             CitizenCertificateConverter citizenCertificateConverter,
                                             CertificateRepository certificateRepository) {
        this.relationDao = relationDao;
        this.citizenCertificateConverter = citizenCertificateConverter;
        this.certificateRepository = certificateRepository;
    }

    @Transactional
    @Override
    public List<CitizenCertificate> getCertificatesForPatient(String patientId,
                                                              List<String> certificateTypes,
                                                              List<String> units,
                                                              List<CitizenCertificateStatusTypeDTO> statuses,
                                                              List<String> years) {

        final var certificates = certificateRepository.findCertificatesForPatient(patientId);

        if (certificates.isEmpty()) {
            return Collections.emptyList();
        }

        final var relations = relationDao.getRelations(getCertificateIds(certificates), List.of(RelationKod.ERSATT.toString()));

        return certificates
                .stream()
                .map((certificate) -> citizenCertificateConverter.get(certificate, filterRelations(certificate.getId(), relations)))
                .collect(Collectors.toList());
    }

    private List<String> getCertificateIds(List<Certificate> certificates) {
        return certificates
                .stream()
                .map(Certificate::getId)
                .collect(Collectors.toList());
    }

    private List<Relation> filterRelations(String certificateId, List<Relation> relations) {
        return relations
                .stream()
                .filter(
                        (relation) -> relation.getToIntygsId().equals(certificateId)
                                || relation.getFromIntygsId().equals(certificateId)
                )
                .collect(Collectors.toList());
    }
}
