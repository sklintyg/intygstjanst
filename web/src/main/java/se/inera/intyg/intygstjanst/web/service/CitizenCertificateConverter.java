package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.persistence.model.dao.CitizenCertificate;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;

import java.util.List;

public interface CitizenCertificateConverter {
    List<CitizenCertificateDTO> get(CitizenCertificate citizenCertificate);
}
