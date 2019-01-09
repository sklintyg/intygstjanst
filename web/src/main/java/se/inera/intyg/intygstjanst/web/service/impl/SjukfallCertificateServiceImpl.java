/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.service.SjukfallCertificateService;
import se.inera.intyg.intygstjanst.web.service.converter.CertificateToSjukfallCertificateConverter;

import java.util.Arrays;
import java.util.List;

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

    private final List<String> allowedIntygsTyper = Arrays.asList(Fk7263EntryPoint.MODULE_ID, LisjpEntryPoint.MODULE_ID);

    @Override
    public boolean created(Certificate certificate) {
        if (!allowedIntygsTyper.contains(certificate.getType())) {
            return false;
        }

        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getType(), certificate.getTypeVersion());
            Utlatande utlatande = moduleApi.getUtlatandeFromXml(certificate.getOriginalCertificate().getDocument());
            //NOTE: See INTYG-7275 - Code below doesn't automatically handle intyg versions as it's not part of the ModuleApi framework.
            switch (certificate.getType()) {
                case Fk7263EntryPoint.MODULE_ID:
                    if (certificateToSjukfallCertificateConverter.isConvertableFk7263(utlatande)) {
                        sjukfallCertificateDao.store(certificateToSjukfallCertificateConverter.convertFk7263(certificate, utlatande));
                        return true;
                    }
                    break;
                case LisjpEntryPoint.MODULE_ID:
                    if (certificateToSjukfallCertificateConverter.isConvertableLisjp(utlatande)) {
                        sjukfallCertificateDao.store(certificateToSjukfallCertificateConverter.convertLisjp(certificate, utlatande));
                        return true;
                    }
                    break;
                default:
                    return false;
            }
            return false;
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
        if (!allowedIntygsTyper.contains(certificate.getType())) {
            return false;
        }

        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getType(), certificate.getTypeVersion());
            Utlatande utlatande = moduleApi.getUtlatandeFromXml(certificate.getOriginalCertificate().getDocument()
            );

            if (certificateToSjukfallCertificateConverter.isConvertableFk7263(utlatande)) {
                sjukfallCertificateDao.revoke(certificate.getId());
                return true;

            }
            if (certificateToSjukfallCertificateConverter.isConvertableLisjp(utlatande)) {
                sjukfallCertificateDao.revoke(certificate.getId());
                return true;
            }

            LOG.debug("Will not mark SjukfallCert {} as deleted. Is of unsupported intygstyp, is smittskydd or does not"
                    + " have a diagnoseCode.",
                    certificate.getId());
            return false;

        } catch (ModuleNotFoundException | ModuleException e) {
            LOG.error("Could not mark SjukfallCert as deleted: {}", e.getMessage());
            return false;
        }
    }
}
