package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateDTOConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRecipientConverter;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.*;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;

@Service
public class CitizenCertificateDTOConverterImpl implements CitizenCertificateDTOConverter {
    private final CitizenCertificateRecipientConverter citizenCertificateRecipientConverter;

    public CitizenCertificateDTOConverterImpl(CitizenCertificateRecipientConverter citizenCertificateRecipientConverter) {
        this.citizenCertificateRecipientConverter = citizenCertificateRecipientConverter;
    }

    @Override
    public CitizenCertificateDTO convert(CitizenCertificate certificate, String typeName, String summaryLabel) {
        return CitizenCertificateDTO
                .builder()
                .id(certificate.getId())
                .type(getType(typeName, certificate.getType(), certificate.getTypeVersion()))
                .summary(getSummary(certificate.getAdditionalInfo(), summaryLabel))
                .issuer(getIssuer(certificate.getIssuerName()))
                .unit(getUnit(certificate.getUnitId(), certificate.getUnitName()))
                .recipient(
                        citizenCertificateRecipientConverter.convert(certificate.getType(), certificate.getSentDate()).orElse(null)
                )
                .issued(certificate.getIssued())
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
