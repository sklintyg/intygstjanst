package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;

public interface CitizenCertificateDTOConverter {
    CitizenCertificateDTO convert(CitizenCertificate certificate, String typeName, String summaryLabel);
}
