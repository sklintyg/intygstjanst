package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificatesRequestDTO;

import java.util.List;

public interface ListCitizenCertificatesService {
    List<CitizenCertificateDTO> get(CitizenCertificatesRequestDTO request);
}
