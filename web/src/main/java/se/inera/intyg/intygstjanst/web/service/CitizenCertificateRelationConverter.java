package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationDTO;

public interface CitizenCertificateRelationConverter {
    CitizenCertificateRelationDTO get(String certificateId,
                                      String toCertificateId,
                                      String fromCertificateId,
                                      String timeStamp,
                                      String code);
}
