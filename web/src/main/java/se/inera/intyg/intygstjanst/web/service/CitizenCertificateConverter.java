package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.web.integration.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificate;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;

import java.util.List;

public interface CitizenCertificateConverter {
    List<CitizenCertificateDTO> get(CitizenCertificate citizenCertificate);
}
