/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.persistence.model.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, String> {

    @Query("select c from Certificate c where c.careGiverId = :careProviderId")
    Page<Certificate> findCertificatesForCareProvider(@Param("careProviderId") String careProviderId, Pageable pageable);

    @Query("select c.id from Certificate c where c.careGiverId = :careProviderId")
    Page<String> findCertificateIdsForCareProvider(@Param("careProviderId") String careProviderId, Pageable pageable);

    @Query("select count(c.id) FROM Certificate c "
        + "join CertificateMetaData cm on c.id = cm.certificateId "
        + "where c.careGiverId = :careProviderId and cm.isRevoked = true")
    long findTotalRevokedForCareProvider(@Param("careProviderId") String careProviderId);

    @Query("select count(c.id) from Certificate c where c.careGiverId = :careProviderId")
    Long getCertificateCountForCareProvider(@Param("careProviderId") String careProviderId);
}
