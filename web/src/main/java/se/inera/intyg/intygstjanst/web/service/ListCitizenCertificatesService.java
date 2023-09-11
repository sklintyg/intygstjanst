package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.ListCitizenCertificatesRequestDTO;

import java.util.List;

public interface ListCitizenCertificatesService {
    List<CitizenCertificateDTO> get(ListCitizenCertificatesRequestDTO request);
}
