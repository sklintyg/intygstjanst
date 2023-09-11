package se.inera.intyg.intygstjanst.web.service.repo.model;

import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationDTO;

import java.util.Optional;

public interface CitizenCertificateRelationConverter {
    Optional<CitizenCertificateRelationDTO> convert(String certificateId, Relation relation);
}
