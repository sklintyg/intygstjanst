package se.inera.intyg.intygstjanst.web.service;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.*;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;

@Service
public class CitizenCertificateDTOConverterImpl implements CitizenCertificateDTOConverter {
    private final CitizenCertificateRecipientConverter citizenCertificateRecipientConverter;

    public CitizenCertificateDTOConverterImpl(CitizenCertificateRecipientConverter citizenCertificateRecipientConverter) {
        this.citizenCertificateRecipientConverter = citizenCertificateRecipientConverter;
    }

    @Override
    public CitizenCertificateDTO get(CitizenCertificate certificate, String typeName, String summaryLabel) throws ModuleNotFoundException {
        return CitizenCertificateDTO
                .builder()
                .id(certificate.getId())
                .type(getType(typeName, certificate.getType(), certificate.getTypeVersion()))
                .summary(getSummary(certificate.getAdditionalInfo(), summaryLabel))
                .issuer(getIssuer(certificate.getIssuerName()))
                .unit(getUnit(certificate.getUnitId(), certificate.getUnitName()))
                .recipient(citizenCertificateRecipientConverter.get(certificate.getType(), certificate.getSentDate()))
                .issued(certificate.getIssued().toString())
                .relations(certificate.getRelations())
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

    private CitizenCertificateSummaryDTO getSummary(String value, String label) {
        return CitizenCertificateSummaryDTO
                .builder()
                .value(value)
                .label(label)
                .build();
    }

    private CitizenCertificateUnitDTO getUnit(String id, String name) {
        return CitizenCertificateUnitDTO
            .builder()
            .id(id)
            .name(name)
            .build();
    }
}
