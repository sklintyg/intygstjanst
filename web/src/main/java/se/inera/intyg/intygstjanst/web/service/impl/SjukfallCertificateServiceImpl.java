/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.intygstjanst.web.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.model.dao.*;
import se.inera.intyg.intygstjanst.web.service.SjukfallCertificateService;
import se.inera.intyg.intygstjanst.web.service.converter.CertificateToSjukfallCertificateConverter;
import se.inera.intyg.intygstyper.fk7263.support.Fk7263EntryPoint;

/**
 * Created by eriklupander on 2016-02-03.
 */
@Service
public class SjukfallCertificateServiceImpl implements SjukfallCertificateService {

    private static final Logger LOG = LoggerFactory.getLogger(SjukfallCertificateServiceImpl.class);

    @Autowired
    private SjukfallCertificateDao sjukfallCertificateDao;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private CertificateToSjukfallCertificateConverter certificateToSjukfallCertificateConverter;

    @Override
    public boolean created(Certificate certificate) {
        if (!certificate.getType().equalsIgnoreCase(Fk7263EntryPoint.MODULE_ID)) {
            return false;
        }

        SjukfallCertificate sjukfallCert;
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getType());
            Utlatande utlatande = moduleApi.getUtlatandeFromXml(certificate.getOriginalCertificate().getDocument());

            if (!certificateToSjukfallCertificateConverter.isConvertableFk7263(utlatande)) {
                LOG.debug("Not storing {}, is smittskydd or does not have a diagnoseCode.", certificate.getId());
                return false;
            }

            sjukfallCert = certificateToSjukfallCertificateConverter.convertFk7263(certificate, utlatande);
            sjukfallCertificateDao.store(sjukfallCert);
            return true;
        } catch (ModuleNotFoundException e) {
            LOG.error("Could not construct sjukfall certificate from intyg, ModuleNotFoundException: {}", e.getMessage());
            return false;
        } catch (ModuleException e) {
            LOG.error("Could not construct sjukfall certificate from intyg, ModuleException: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean revoked(Certificate certificate) {
        sjukfallCertificateDao.revoke(certificate.getId());
        return true;
    }
}
