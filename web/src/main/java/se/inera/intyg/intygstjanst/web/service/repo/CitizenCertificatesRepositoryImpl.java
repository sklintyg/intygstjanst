package se.inera.intyg.intygstjanst.web.service.repo;

import org.springframework.stereotype.Repository;
import se.inera.intyg.intygstjanst.persistence.model.dao.*;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificateConverter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CitizenCertificatesRepositoryImpl implements CitizenCertificatesRepository {
    private final RelationDao relationDao;
    private final CitizenCertificateConverter citizenCertificateConverter;
    private final CertificateDao certificateDao;

    public CitizenCertificatesRepositoryImpl(RelationDao relationDao,
                                             CitizenCertificateConverter citizenCertificateConverter,
                                             CertificateDao certificateDao) {
        this.relationDao = relationDao;
        this.citizenCertificateConverter = citizenCertificateConverter;
        this.certificateDao = certificateDao;
    }

    @Override
    public List<CitizenCertificate> getCertificatesForPatient(String patientId) {

        final var certificates = certificateDao.findCertificatesForPatient(patientId);

        if (certificates.isEmpty()) {
            return Collections.emptyList();
        }

        final var relations = relationDao.getRelations(
                getCertificateIds(certificates),
                getRevokedCertificateIds(certificates)
        );

        return certificates
                .stream()
                .filter((certificate) -> !certificate.getCertificateMetaData().isRevoked())
                .map((certificate) -> citizenCertificateConverter.convert(
                            certificate,
                            relations.get(certificate.getId())
                        )
                )
                .collect(Collectors.toList());
    }

    private List<String> getCertificateIds(List<Certificate> certificates) {
        return certificates
                .stream()
                .filter((certificate) -> !certificate.getCertificateMetaData().isRevoked())
                .map(Certificate::getId)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> getRevokedCertificateIds(List<Certificate> certificates) {
        return certificates
                .stream()
                .filter((certificate) -> certificate.getCertificateMetaData().isRevoked())
                .map(Certificate::getId)
                .distinct()
                .collect(Collectors.toList());
    }
}
