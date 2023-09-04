package se.inera.intyg.intygstjanst.persistence.model.dao.impl;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.persistence.model.dao.*;

import java.util.List;
import java.util.stream.Collectors;

public class CitizenCertificatesDaoImpl extends CitizenCertificatesDao {
    private final RelationDao relationDao;
    private final CitizenCertificatesRepository citizenCertificatesRepository;

    public CitizenCertificatesDaoImpl(RelationDao relationDao,
                                      CitizenCertificatesRepository citizenCertificatesRepository) {
        this.relationDao = relationDao;
        this.citizenCertificatesRepository = citizenCertificatesRepository;
    }

    @Override
    public List<CitizenCertificate> getCertificatesForPatient(String patientId,
                                                              List<String> certificateTypes,
                                                              List<String> units,
                                                              List<String> statuses,
                                                              List<String> years) {

        final var certificates = citizenCertificatesRepository.findByPatientId(
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

        certificates.forEach((certificate) ->
                certificate.setRelations(
                    filterRelations(certificate.getId(), relations)
                )
        );

        return certificates;
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
