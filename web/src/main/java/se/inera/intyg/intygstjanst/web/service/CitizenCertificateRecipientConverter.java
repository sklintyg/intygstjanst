package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationDTO;

import java.util.List;

public interface CitizenCertificateRecipientConverter {
    List<CitizenCertificateRecipientDTO> get();
}
