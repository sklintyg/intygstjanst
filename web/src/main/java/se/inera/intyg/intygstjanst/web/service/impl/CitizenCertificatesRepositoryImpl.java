package se.inera.intyg.intygstjanst.web.service.impl;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.persistence.model.dao.*;
import se.inera.intyg.intygstjanst.web.integration.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificatesRepository;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.schemas.contract.Personnummer;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class CitizenCertificatesRepositoryImpl implements CitizenCertificatesRepository {
    private final RelationDao relationDao;
    private final CitizenCertificatesDao citizenCertificatesDao;
    private final CitizenCertificateConverter citizenCertificateConverter;
    private final CertificateDao certificateDao;

    public CitizenCertificatesRepositoryImpl(RelationDao relationDao,
                                             CitizenCertificatesDao citizenCertificatesDao,
                                             CitizenCertificateConverter citizenCertificateConverter,
                                             CertificateDao certificateDao) {
        this.relationDao = relationDao;
        this.citizenCertificatesDao = citizenCertificatesDao;
        this.citizenCertificateConverter = citizenCertificateConverter;
        this.certificateDao = certificateDao;
    }

    @Override
    public List<CitizenCertificateDTO> getCertificatesForPatient(String patientId,
                                                                 List<String> certificateTypes,
                                                                 List<String> units,
                                                                 List<CitizenCertificateStatusTypeDTO> statuses,
                                                                 List<String> years) {

        final var certificates = getCertificates(patientId, certificateTypes, units);

        final var certificateIds = certificates
                .stream()
                .map((certificate) -> certificate.getId())
                .collect(Collectors.toList());

        final var relations = relationDao.getRelations(certificateIds, List.of(RelationKod.ERSATT.toString()));

        return certificates
                .stream()
                .map((certificate) -> citizenCertificateConverter.get(certificate, filterRelations(certificate.getId(), relations)))
                .collect(Collectors.toList());
    }

    private List<CitizenCertificate> getCitizenCertificates(String patientId,
                                                               List<String> certificateTypes,
                                                               List<String> units,
                                                               List<CitizenCertificateStatusTypeDTO> statuses,
                                                               List<String> years) {
        return citizenCertificatesDao.findByPatientId(
                patientId,
                certificateTypes, // should be filtered depending on status filter
                units,
                statuses.stream().map(Enum::toString).collect(Collectors.toList()), // change this to values for determining logic sent/not sent
                years
        );
    }

    private List<Certificate> getCertificates(String patientId,
                                                List<String> certificateTypes,
                                                List<String> units) {
        return certificateDao.findCertificates(
                Personnummer.createPersonnummer(patientId).get(),
                units.toArray(new String[0]),
                null,
                null,
                null,
                false,
                new HashSet<String>(certificateTypes), // should be filtered depending on status filter
                null
        );
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
