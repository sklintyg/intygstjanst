package se.inera.intyg.intygstjanst.web.service.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.service.SjukfallCertificateService;
import se.inera.intyg.intygstjanst.web.service.converter.CertificateToSjukfallCertificateConverter;

/**
 * Created by eriklupander on 2016-02-03.
 */
@Service
public class SjukfallCertificateServiceImpl implements SjukfallCertificateService {

    private static final Logger log = LoggerFactory.getLogger(SjukfallCertificateServiceImpl.class);

    @Autowired
    SjukfallCertificateDao sjukfallCertificateDao;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private CertificateToSjukfallCertificateConverter certificateToSjukfallCertificateConverter;

    @Override
    public boolean created(Certificate certificate) {
        if (!certificate.getType().equalsIgnoreCase("fk7263")) {
            return false;
        }

        SjukfallCertificate sjukfallCert = null;
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getType());
            Utlatande utlatande = moduleApi.getUtlatandeFromJson(certificate.getDocument());
            sjukfallCert = certificateToSjukfallCertificateConverter.convertFk7263(certificate, utlatande);
            sjukfallCertificateDao.store(sjukfallCert);
            return true;
        } catch (ModuleNotFoundException e) {
            log.error("Could not construct sjukfall certificate from intyg, ModuleNotFoundException: {0}", e.getMessage());
            return false;
        } catch (IOException e) {
            log.error("Could not construct sjukfall certificate from intyg, IOException: {0}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean revoked(Certificate certificate) {
        sjukfallCertificateDao.revoke(certificate.getId());
        return true;
    }
}
