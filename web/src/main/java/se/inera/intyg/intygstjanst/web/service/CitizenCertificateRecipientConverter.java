package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CitizenCertificateRecipientConverter {
    Optional<CitizenCertificateRecipientDTO> convert(String certificateType, LocalDateTime sent);
}
