package se.inera.intyg.intygstjanst.web.service.repo.model;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CitizenCertificateConverterImpl implements CitizenCertificateConverter {
    private final CitizenCertificateRelationConverter citizenCertificateRelationConverter;

    public CitizenCertificateConverterImpl(CitizenCertificateRelationConverter citizenCertificateRelationConverter) {
        this.citizenCertificateRelationConverter = citizenCertificateRelationConverter;
    }

    @Override
    public CitizenCertificate convert(Certificate certificate, List<Relation> relations) {
        final var sentState = getSentState(certificate.getStates());

        return CitizenCertificate
                .builder()
                .id(certificate.getId())
                .type(certificate.getType())
                .typeVersion(certificate.getTypeVersion())
                .additionalInfo(certificate.getAdditionalInfo())
                .issuerName(certificate.getSigningDoctorName())
                .unitId(certificate.getCareUnitId())
                .unitName(certificate.getCareUnitName())
                .issued(certificate.getSignedDate())
                .sentDate(sentState.map(CertificateStateHistoryEntry::getTimestamp).orElse(null))
                .relations(getRelations(certificate.getId(), relations))
                .build();
    }

    private CitizenCertificateRelationDTO getRelation(String certificateId, Relation relation) {
        return citizenCertificateRelationConverter.convert(
                certificateId,
                relation.getToIntygsId(),
                relation.getFromIntygsId(),
                relation.getCreated(),
                relation.getRelationKod());
    }

    private List<CitizenCertificateRelationDTO> getRelations(String certificateId, List<Relation> relations) {
        return relations
                .stream()
                .map((relation) -> getRelation(certificateId, relation))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Optional<CertificateStateHistoryEntry> getSentState(Collection<CertificateStateHistoryEntry> states) {
        return states.stream().filter((state) -> state.getState() == CertificateState.SENT).findFirst();
    }
}
