package se.inera.certificate.integration.module;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.certificate.integration.module.exception.ModuleNotFoundException;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.api.ModuleApi;

/**
 * Factory which which serves {@link ModuleApi} implementations of the registered module types.
 */
@Component
public class ModuleApiFactory {

    private static Logger LOG = LoggerFactory.getLogger(ModuleApiFactory.class);

    @Autowired
    private List<ModuleEntryPoint> moduleEntryPoints;

    private HashMap<String, ModuleEntryPoint> moduleApiMap = new HashMap<String, ModuleEntryPoint>();

    @PostConstruct
    private void initModulesList() {
        TreeSet<String> moduleIds = new TreeSet<String>();
        for (ModuleEntryPoint entryPoint : moduleEntryPoints) {
            moduleApiMap.put(entryPoint.getModuleId(), entryPoint);
            moduleIds.add(entryPoint.getModuleId());
        }

        LOG.info("Module registry loaded with modules {}", moduleIds);
    }

    /**
     * Creates a {@link ModuleRestApi} for the given certificate type.
     * 
     * @throws ModuleNotFoundException
     */
    public ModuleApi getModuleApi(String type) throws ModuleNotFoundException {
        ModuleEntryPoint moduleEntryPoint = moduleApiMap.get(type);
        if (moduleEntryPoint != null) {
            return moduleEntryPoint.getModuleApi();
        }

        throw new ModuleNotFoundException("Could not find module " + type);
    }

    /**
     * Creates a {@link ModuleRestApi} for the given certificate type.
     * 
     * @throws ModuleNotFoundException
     */
    public ModuleApi getModuleApi(Utlatande utlatande) throws ModuleNotFoundException {
        return getModuleApi(utlatande.getTyp().getCode());
    }
}
