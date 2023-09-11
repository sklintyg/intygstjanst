package se.inera.intyg.intygstjanst.web.service.repo.model;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationType;

import java.util.Optional;

@Service
public class CitizenCertificateRelationConverterImpl implements CitizenCertificateRelationConverter {

    @Override
    public Optional<CitizenCertificateRelationDTO> convert(String certificateId, Relation relation) {
        if (!certificateId.equals(relation.getToIntygsId()) && !certificateId.equals(relation.getFromIntygsId())) {
            return Optional.empty();
        }

        return Optional.of(
                CitizenCertificateRelationDTO
                    .builder()
                    .certificateId(getRelatedId(certificateId, relation.getToIntygsId(), relation.getFromIntygsId()))
                    .timestamp(relation.getCreated().toString())
                    .type(getType(relation.getRelationKod(), certificateId, relation.getToIntygsId()))
                    .build()
        );
    }

    private String getRelatedId(String id, String toId, String fromId) {
        return id.equals(toId) ? fromId : toId;
    }

    private CitizenCertificateRelationType getType(String code, String certificateId, String toCertificateId) {
        if (code.equals(RelationKod.ERSATT.toString())) {
            return certificateId.equals(toCertificateId)
                    ? CitizenCertificateRelationType.RENEWED
                    : CitizenCertificateRelationType.RENEWS;
        }

        return CitizenCertificateRelationType.UNKNOWN;
    }
}
