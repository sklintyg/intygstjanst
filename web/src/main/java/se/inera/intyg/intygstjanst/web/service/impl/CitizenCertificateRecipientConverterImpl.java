package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRecipientConverter;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateRecipientType;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;
import se.inera.intyg.intygstjanst.web.service.repo.RecipientRepo;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class CitizenCertificateRecipientConverterImpl implements CitizenCertificateRecipientConverter {

    private final RecipientRepo recipientRepo;

    public CitizenCertificateRecipientConverterImpl(RecipientRepo recipientRepo) {
        this.recipientRepo = recipientRepo;
    }

    @Override
    public CitizenCertificateRecipientDTO convert(String certificateType, LocalDateTime sent) {
        final var recipients = recipientRepo
                .listRecipients()
                .stream()
                .filter(
                        (recipient) -> recipient.getCertificateTypes().contains(certificateType)
                        && recipient.getRecipientType() == CertificateRecipientType.HUVUDMOTTAGARE
                )
                .collect(Collectors.toList());

        if (recipients.isEmpty()) {
            return null;
        }

        return CitizenCertificateRecipientDTO
                .builder()
                .id(recipients.get(0).getId())
                .name(recipients.get(0).getName())
                .sent(sent == null ? null : sent.toString())
                .build();
    }
}
