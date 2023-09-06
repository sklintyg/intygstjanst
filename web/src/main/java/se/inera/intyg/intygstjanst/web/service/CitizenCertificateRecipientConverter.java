package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;

import java.time.LocalDateTime;

public interface CitizenCertificateRecipientConverter {
    CitizenCertificateRecipientDTO get(String certificateType, LocalDateTime sent);
}
