package se.inera.intyg.intygstjanst.web.service.repo.model;

import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationDTO;

import java.time.LocalDateTime;

public interface CitizenCertificateRelationConverter {
    CitizenCertificateRelationDTO get(String certificateId,
                                      String toCertificateId,
                                      String fromCertificateId,
                                      LocalDateTime timeStamp,
                                      String code);
}
