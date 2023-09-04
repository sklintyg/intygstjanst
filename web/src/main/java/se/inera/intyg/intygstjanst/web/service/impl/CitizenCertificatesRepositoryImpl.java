package se.inera.intyg.intygstjanst.web.service.impl;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.persistence.model.dao.*;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificatesRepository;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;

import java.util.List;
import java.util.stream.Collectors;

public class CitizenCertificatesRepositoryImpl implements CitizenCertificatesRepository {
    private final RelationDao relationDao;
    private final CitizenCertificatesDao citizenCertificatesDao;
    private final CitizenCertificateConverter citizenCertificateConverter;

    public CitizenCertificatesRepositoryImpl(RelationDao relationDao,
                                             CitizenCertificatesDao citizenCertificatesDao,
                                             CitizenCertificateConverter citizenCertificateConverter) {
        this.relationDao = relationDao;
        this.citizenCertificatesDao = citizenCertificatesDao;
        this.citizenCertificateConverter = citizenCertificateConverter;
    }

    @Override
    public List<CitizenCertificateDTO> getCertificatesForPatient(String patientId,
                                                                 List<String> certificateTypes,
                                                                 List<String> units,
                                                                 List<String> statuses,
                                                                 List<String> years) {

        final var certificates = citizenCertificatesDao.findByPatientId(
                patientId,
                certificateTypes,
                units,
                statuses,
                years
        );

        final var certificateIds = certificates
                .stream()
                .map(CitizenCertificate::getId)
                .collect(Collectors.toList());

        final var relations = relationDao.getRelations(certificateIds, List.of(RelationKod.ERSATT.toString()));

        return certificates
                .stream()
                .map((certificate) -> citizenCertificateConverter.get(certificate, relations))
                .collect(Collectors.toList());
    }

    private List<Relation> filterRelations(String certificateId, List<Relation> relations) {
        return relations
                .stream()
                .filter(
                        (relation) -> relation.getToIntygsId().equals(certificateId) || relation.getFromIntygsId().equals(certificateId)
                )
                .collect(Collectors.toList());
    }
}
