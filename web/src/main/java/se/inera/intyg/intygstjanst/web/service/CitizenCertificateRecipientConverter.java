package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;

import java.time.LocalDateTime;

public interface CitizenCertificateRecipientConverter {
    CitizenCertificateRecipientDTO convert(String certificateType, LocalDateTime sent);
}
