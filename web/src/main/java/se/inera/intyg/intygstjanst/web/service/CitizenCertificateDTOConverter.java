package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;

public interface CitizenCertificateDTOConverter {
    CitizenCertificateDTO get(CitizenCertificate certificate, String typeName, String summaryLabel) throws ModuleNotFoundException;
}
