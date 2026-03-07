/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.service.bean;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.CertificateMetaData;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.web.service.converter.CertificateToSjukfallCertificateConverter;

/**
 * Handles transactional persistence for bootstrap data.
 * Extracted from IntygBootstrapBean so that @Transactional works
 * via Spring's proxy mechanism (intra-class calls bypass the proxy).
 */
@Service
public class IntygBootstrapPersister {

    private static final Logger LOG = LoggerFactory.getLogger(IntygBootstrapPersister.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final CertificateToSjukfallCertificateConverter certificateToSjukfallCertificateConverter;

    public IntygBootstrapPersister(CertificateToSjukfallCertificateConverter converter) {
        this.certificateToSjukfallCertificateConverter = converter;
    }

    @Transactional
    public void persistCertificate(Certificate certificate, OriginalCertificate originalCertificate,
        CertificateMetaData metaData, Utlatande utlatande) {
        if (entityManager.find(Certificate.class, certificate.getId()) != null) {
            LOG.info("Bootstrapping of certificate '{}' skipped. Already in database.", certificate.getId());
            return;
        }
        entityManager.persist(metaData);
        entityManager.persist(originalCertificate);
        entityManager.persist(certificate);

        if (isSjukfallsGrundandeIntyg(certificate.getType())) {
            persistSjukfallIfConvertable(certificate, utlatande);
        }
    }

    @Transactional
    public void persistLocalCertificate(Certificate certificate, OriginalCertificate originalCertificate,
        CertificateMetaData metaData) {
        if (entityManager.find(Certificate.class, certificate.getId()) != null) {
            LOG.info("Bootstrapping of certificate '{}' skipped. Already in database.", certificate.getId());
            return;
        }
        entityManager.persist(metaData);
        entityManager.persist(originalCertificate);
        entityManager.persist(certificate);
    }

    @Transactional
    public void persistSjukfall(Certificate certificate, Utlatande utlatande) {
        persistSjukfallIfConvertable(certificate, utlatande);
    }

    private void persistSjukfallIfConvertable(Certificate certificate, Utlatande utlatande) {
        if (certificateToSjukfallCertificateConverter.isConvertableFk7263(utlatande)) {
            SjukfallCertificate sjukfallCert = certificateToSjukfallCertificateConverter.convertFk7263(certificate, utlatande);
            if (entityManager.find(SjukfallCertificate.class, sjukfallCert.getId()) == null) {
                entityManager.persist(sjukfallCert);
            } else {
                LOG.info("Bootstrapping of sjukfall '{}' skipped. Already in database.", sjukfallCert.getId());
            }
        }
        if (certificateToSjukfallCertificateConverter.isConvertableLisjp(utlatande)) {
            SjukfallCertificate sjukfallCert = certificateToSjukfallCertificateConverter.convertLisjp(certificate, utlatande);
            if (entityManager.find(SjukfallCertificate.class, sjukfallCert.getId()) == null) {
                entityManager.persist(sjukfallCert);
            } else {
                LOG.info("Bootstrapping of sjukfall '{}' skipped. Already in database.", sjukfallCert.getId());
            }
        }
    }

    private boolean isSjukfallsGrundandeIntyg(String type) {
        return Fk7263EntryPoint.MODULE_ID.equalsIgnoreCase(type)
            || LisjpEntryPoint.MODULE_ID.equalsIgnoreCase(type);
    }
}