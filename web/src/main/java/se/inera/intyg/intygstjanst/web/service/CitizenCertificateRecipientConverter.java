package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;

public interface CitizenCertificateRecipientConverter {
    CitizenCertificateRecipientDTO get(String id, String name, String sent);
}
