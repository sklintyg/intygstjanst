package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.intygstjanst.persistence.model.dao.CitizenCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRecipientConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRelationConverter;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CitizenCertificateConverterImpl implements CitizenCertificateConverter {
    private final CitizenCertificateRecipientConverter citizenCertificateRecipientConverter;
    private final CitizenCertificateRelationConverter citizenCertificateRelationConverter;
    private final IntygModuleRegistryImpl intygModuleRegistry;

    public CitizenCertificateConverterImpl(CitizenCertificateRecipientConverter citizenCertificateRecipientConverter,
                                           CitizenCertificateRelationConverter citizenCertificateRelationConverter, IntygModuleRegistryImpl intygModuleRegistry) {
        this.citizenCertificateRecipientConverter = citizenCertificateRecipientConverter;
        this.citizenCertificateRelationConverter = citizenCertificateRelationConverter;
        this.intygModuleRegistry = intygModuleRegistry;
    }

    @Override
    public CitizenCertificateDTO get(CitizenCertificate citizenCertificate, List<Relation> relations) {
        //final var moduleApi = intygModuleRegistry.getModuleApi(citizenCertificate.getType(), citizenCertificate.getTypeVersion());

        return CitizenCertificateDTO
                .builder()
                .id(citizenCertificate.getId())
                .type(getType("", citizenCertificate.getType()))
                .issuer(getIssuer(citizenCertificate.getDoctorName()))
                .issued(citizenCertificate.getSignedDate())
                .summary(getSummary(citizenCertificate.getAdditionalInfo()))
                .recipient(citizenCertificateRecipientConverter.get("", "", ""))
                .relations(getRelations(citizenCertificate.getId(), relations))
                .build();
    }

    private CitizenCertificateIssuerDTO getIssuer(String name) {
        return CitizenCertificateIssuerDTO
                .builder()
                .name(name)
                .build();
    }

    private CitizenCertificateTypeDTO getType(String name, String id) {
        return CitizenCertificateTypeDTO
                .builder()
                .id(id)
                .name(name) // where can we find the typename? Not moduleapi?
                .build();
    }

    private CitizenCertificateSummaryDTO getSummary(String name) {
        return CitizenCertificateSummaryDTO
                .builder()
                .name(name)
                .label("") //from moudleapi using type and version
                .build();
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
