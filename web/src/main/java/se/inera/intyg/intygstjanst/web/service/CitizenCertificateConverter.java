package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.persistence.model.dao.CitizenCertificate;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;

public interface CitizenCertificateConverter {
    CitizenCertificateDTO get(CitizenCertificate citizenCertificate);
}
