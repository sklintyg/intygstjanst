package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.persistence.model.dao.CitizenCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRecipientConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRelationConverter;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CitizenCertificateConverterImpl implements CitizenCertificateConverter {
    private final CitizenCertificateRecipientConverter citizenCertificateRecipientConverter;
    private final CitizenCertificateRelationConverter citizenCertificateRelationConverter;

    public CitizenCertificateConverterImpl(CitizenCertificateRecipientConverter citizenCertificateRecipientConverter,
                                           CitizenCertificateRelationConverter citizenCertificateRelationConverter) {
        this.citizenCertificateRecipientConverter = citizenCertificateRecipientConverter;
        this.citizenCertificateRelationConverter = citizenCertificateRelationConverter;
    }

    @Override
    public CitizenCertificateDTO get(CitizenCertificate citizenCertificate) {
        return CitizenCertificateDTO
                .builder()
                .recipient(getRecipient("", "", ""))
                .relations(getRelations(citizenCertificate.getId(), citizenCertificate.getRelations()))
                .build();
    }

    private CitizenCertificateRecipientDTO getRecipient(String id, String name, String sent) {
        return citizenCertificateRecipientConverter.get("", "", "");
    }

    private CitizenCertificateRelationDTO getRelation(String certificateId, Relation relation) {
        return citizenCertificateRelationConverter.get(
                certificateId,
                relation.getFromIntygsId(),
                relation.getFromIntygsId(),
                relation.getCreated(),
                relation.getRelationKod());
    }

    private List<CitizenCertificateRelationDTO> getRelations(String certificateId, List<Relation> relations) {
        return relations
                .stream()
                .map((relation) -> getRelation(certificateId, relation))
                .collect(Collectors.toList());
    }
}
