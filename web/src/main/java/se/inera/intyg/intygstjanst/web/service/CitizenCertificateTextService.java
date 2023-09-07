package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;

public interface CitizenCertificateTextService {
    String getTypeName(String typeId) throws ModuleNotFoundException;

    String getAdditionalInfoLabel(String additionalInfo);
}
