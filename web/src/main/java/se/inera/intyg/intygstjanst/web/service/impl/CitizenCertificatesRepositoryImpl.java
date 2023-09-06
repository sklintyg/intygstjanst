package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Repository;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.persistence.model.dao.*;
import se.inera.intyg.intygstjanst.web.integration.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateFilterService;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificatesRepository;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.schemas.contract.Personnummer;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CitizenCertificatesRepositoryImpl implements CitizenCertificatesRepository {
    private final RelationDao relationDao;
    private final CitizenCertificateConverter citizenCertificateConverter;
    private final CertificateDao certificateDao;
    private final CitizenCertificateFilterService citizenCertificateFilterService;

    public CitizenCertificatesRepositoryImpl(RelationDao relationDao,
                                             CitizenCertificateConverter citizenCertificateConverter,
                                             CertificateDao certificateDao, CitizenCertificateFilterService citizenCertificateFilterService) {
        this.relationDao = relationDao;
        this.citizenCertificateConverter = citizenCertificateConverter;
        this.certificateDao = certificateDao;
        this.citizenCertificateFilterService = citizenCertificateFilterService;
    }

    @Override
    public List<CitizenCertificateDTO> getCertificatesForPatient(String patientId,
                                                                 List<String> certificateTypes,
                                                                 List<String> units,
                                                                 List<CitizenCertificateStatusTypeDTO> statuses,
                                                                 List<String> years) {

        final var certificates = getCertificates(patientId, certificateTypes, units);

        if (certificates.isEmpty()) {
            return Collections.emptyList();
        }


        final var relations = relationDao.getRelations(getCertificateIds(certificates), List.of(RelationKod.ERSATT.toString()));

        return certificates
                .stream()
                .filter((certificate) -> certificate.getCertificateMetaData() != null
                        && !certificate.getCertificateMetaData().isRevoked()
                )
                .map((certificate) -> citizenCertificateConverter.get(certificate, filterRelations(certificate.getId(), relations)))
                .filter((certificate) -> citizenCertificateFilterService.filterOnYears(certificate, years))
                .filter((certificate) -> citizenCertificateFilterService.filterOnSentStatus(certificate, statuses))
                .collect(Collectors.toList());
    }

    private List<String> getCertificateIds(List<Certificate> certificates) {
        return certificates
                .stream()
                .map(Certificate::getId)
                .collect(Collectors.toList());
    }

    private List<Certificate> getCertificates(String patientId,
                                                List<String> certificateTypes,
                                                List<String> units) {
        final var convertedPatientId = convertPatientId(patientId);

        if (convertedPatientId == null) {
            return Collections.emptyList();
        }

        return certificateDao.findCertificates(
                convertedPatientId,
                units.toArray(new String[0]),
                LocalDateTime.now().minusYears(100),
                null,
                "signedDate",
                false,
                new HashSet<>(certificateTypes), // should be filtered depending on status filter
                null
        );
    }

    private Personnummer convertPatientId(String patientId) {
        return Personnummer.createPersonnummer(patientId).orElse(null);
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
