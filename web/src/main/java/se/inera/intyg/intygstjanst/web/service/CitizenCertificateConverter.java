package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.intygstjanst.persistence.model.dao.CitizenCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;

import java.util.List;

public interface CitizenCertificateConverter {
    CitizenCertificateDTO get(CitizenCertificate citizenCertificate, List<Relation> relations) throws ModuleNotFoundException;
}
