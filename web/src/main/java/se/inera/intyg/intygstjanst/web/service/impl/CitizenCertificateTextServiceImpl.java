package se.inera.intyg.intygstjanst.web.service.impl;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateTextService;

public class CitizenCertificateTextServiceImpl implements CitizenCertificateTextService {

    private final IntygModuleRegistry intygModuleRegistry;

    public CitizenCertificateTextServiceImpl(IntygModuleRegistry intygModuleRegistry) {
        this.intygModuleRegistry = intygModuleRegistry;
    }

    @Override
    public String getTypeName(String typeId) throws ModuleNotFoundException {
        return intygModuleRegistry.getModuleEntryPoint(typeId).getModuleName();
    }

    @Override
    public String getAdditionalInfoLabel(String typeId, String typeVersion) throws ModuleNotFoundException {
        final var moduleApi = intygModuleRegistry.getModuleApi(typeId, typeVersion);
        return "Avser";
    }
}
