package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CitizenCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRecipientConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRelationConverter;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.*;

import java.util.List;
import java.util.Objects;
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
        try { // TODO: How should we handle error?
            final var moduleApi = intygModuleRegistry.getModuleApi(citizenCertificate.getType(), citizenCertificate.getTypeVersion());
            final var moduleEntryPoint = intygModuleRegistry.getModuleEntryPoint(citizenCertificate.getType());

            return CitizenCertificateDTO
                    .builder()
                    .id(citizenCertificate.getId())
                    .type(getType(moduleEntryPoint.getModuleName(), citizenCertificate.getType(), citizenCertificate.getTypeVersion()))
                    .issuer(getIssuer(citizenCertificate.getDoctorName()))
                    .issued(citizenCertificate.getSignedDate())
                    .summary(getSummary(citizenCertificate.getAdditionalInfo()))
                    .recipient(citizenCertificateRecipientConverter.get("", "", ""))
                    .relations(getRelations(citizenCertificate.getId(), relations))
                    .build();

        } catch (ModuleNotFoundException e) {
            return null;
        }
    }

    @Override
    public CitizenCertificateDTO get(Certificate certificate, List<Relation> relations) {
        //final var moduleApi = intygModuleRegistry.getModuleApi(certificate.getType(), certificate.getTypeVersion());
        //final var moduleEntryPoint = intygModuleRegistry.getModuleEntryPoint(certificate.getType());

        return CitizenCertificateDTO
                .builder()
                .id(certificate.getId())
                .type(getType("", certificate.getType(), certificate.getTypeVersion()))
                .summary(getSummary(certificate.getAdditionalInfo()))
                .issuer(getIssuer(certificate.getSigningDoctorName()))
                .unit(getUnit(certificate.getCareUnitId(), certificate.getCareUnitName()))
                .recipient(citizenCertificateRecipientConverter.get(certificate.getStates()))
                .issued(certificate.getSignedDate().toString())
                .relations(getRelations(certificate.getId(), relations))
                .build();
    }

    private CitizenCertificateIssuerDTO getIssuer(String name) {
        return CitizenCertificateIssuerDTO
                .builder()
                .name(name)
                .build();
    }

    private CitizenCertificateTypeDTO getType(String name, String id, String version) {
        return CitizenCertificateTypeDTO
                .builder()
                .id(id)
                .name(name)
                .version(version)
                .build();
    }

    private CitizenCertificateSummaryDTO getSummary(String name) {
        return CitizenCertificateSummaryDTO
                .builder()
                .value(name)
                .label("") //TODO: From module api
                .build();
    }

    private CitizenCertificateRelationDTO getRelation(String certificateId, Relation relation) {
        return citizenCertificateRelationConverter.get(
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

    private CitizenCertificateUnitDTO getUnit(String id, String name) {
        return CitizenCertificateUnitDTO
            .builder()
            .id(id)
            .name(name)
            .build();
    }
}
