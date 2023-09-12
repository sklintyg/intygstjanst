package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateTextService;

@Service
public class CitizenCertificateTextServiceImpl implements CitizenCertificateTextService {

    private final IntygModuleRegistry intygModuleRegistry;

    public CitizenCertificateTextServiceImpl(IntygModuleRegistry intygModuleRegistry) {
        this.intygModuleRegistry = intygModuleRegistry;
    }

    @Override
    public String getTypeName(String typeId) {
        try {
            return intygModuleRegistry.getModuleEntryPoint(typeId).getModuleName();
        } catch (ModuleNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getAdditionalInfoLabel(String typeId, String typeVersion) {
        try {
            final var moduleApi = intygModuleRegistry.getModuleApi(typeId, typeVersion);
        } catch (ModuleNotFoundException e) {
            throw new RuntimeException(e);
        }
        return "Avser";
    }
}
