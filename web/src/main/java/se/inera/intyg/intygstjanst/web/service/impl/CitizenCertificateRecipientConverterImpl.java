package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRecipientConverter;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateRecipientType;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;
import se.inera.intyg.intygstjanst.web.service.repo.RecipientRepo;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CitizenCertificateRecipientConverterImpl implements CitizenCertificateRecipientConverter {

    private final RecipientRepo recipientRepo;

    public CitizenCertificateRecipientConverterImpl(RecipientRepo recipientRepo) {
        this.recipientRepo = recipientRepo;
    }

    @Override
    public Optional<CitizenCertificateRecipientDTO> convert(String certificateType, LocalDateTime sent) {
        return recipientRepo
                .listRecipients()
                .stream()
                .filter(
                        (recipient) -> recipient.getCertificateTypes().contains(certificateType)
                                && recipient.getRecipientType() == CertificateRecipientType.HUVUDMOTTAGARE
                )
                .findFirst()
                .map(recipient ->
                        CitizenCertificateRecipientDTO
                                .builder()
                                .id(recipient.getId())
                                .name(recipient.getName())
                                .sent(sent)
                                .build()
                );
    }
}
