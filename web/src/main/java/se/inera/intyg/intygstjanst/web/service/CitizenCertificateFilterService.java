package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.ListCitizenCertificatesRequestDTO;


public interface CitizenCertificateFilterService {
    boolean filter(CitizenCertificateDTO certificate, ListCitizenCertificatesRequestDTO request);
}
