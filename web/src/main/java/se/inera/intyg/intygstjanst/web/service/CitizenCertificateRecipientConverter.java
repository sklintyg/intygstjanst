package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;

import java.util.Collection;

public interface CitizenCertificateRecipientConverter {
    CitizenCertificateRecipientDTO get(String id, String name, String sent);

    CitizenCertificateRecipientDTO get(Collection<CertificateStateHistoryEntry> states);
}
