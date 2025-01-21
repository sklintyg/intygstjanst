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

package se.inera.intyg.intygstjanst.web.service.repo;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.RelationDao;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificateConverter;

@Repository
public class CitizenCertificatesRepositoryImpl implements CitizenCertificatesRepository {

    private static final List<String> certificateTypesToExclude = List.of(
        DbModuleEntryPoint.MODULE_ID,
        DoiModuleEntryPoint.MODULE_ID
    );

    private final RelationDao relationDao;
    private final CitizenCertificateConverter citizenCertificateConverter;
    private final CertificateDao certificateDao;

    public CitizenCertificatesRepositoryImpl(RelationDao relationDao,
        CitizenCertificateConverter citizenCertificateConverter,
        CertificateDao certificateDao) {
        this.relationDao = relationDao;
        this.citizenCertificateConverter = citizenCertificateConverter;
        this.certificateDao = certificateDao;
    }

    @Override
    public List<CitizenCertificate> getCertificatesForPatient(String patientId) {

        final var certificates = certificateDao.findCertificatesForPatient(patientId);

        if (certificates.isEmpty()) {
            return Collections.emptyList();
        }

        final var relations = relationDao.getRelations(
            getCertificateIds(certificates),
            getRevokedCertificateIds(certificates)
        );

        return certificates
            .stream()
            .filter(certificate -> !certificate.getCertificateMetaData().isRevoked())
            .filter(certificate -> isCertificateTypeIncluded(certificate.getType()))
            .map(certificate -> citizenCertificateConverter.convert(
                    certificate,
                    relations.get(certificate.getId())
                )
            )
            .collect(Collectors.toList());
    }

    private boolean isCertificateTypeIncluded(String type) {
        return !certificateTypesToExclude.contains(type);
    }

    private List<String> getCertificateIds(List<Certificate> certificates) {
        return certificates
            .stream()
            .filter(certificate -> !certificate.getCertificateMetaData().isRevoked())
            .map(Certificate::getId)
            .collect(Collectors.toList());
    }

    private List<String> getRevokedCertificateIds(List<Certificate> certificates) {
        return certificates
            .stream()
            .filter(certificate -> certificate.getCertificateMetaData().isRevoked())
            .map(Certificate::getId)
            .collect(Collectors.toList());
    }
}
