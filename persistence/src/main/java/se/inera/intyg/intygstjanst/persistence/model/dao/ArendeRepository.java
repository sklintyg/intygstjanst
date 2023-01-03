/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArendeRepository extends JpaRepository<Arende, Long> {

    /**
     * Should return a {@link Arende} matching the search criteria.
     */
    Arende findByMeddelandeId(String meddelandeId);

    /**
     * Find all messages for a single certificate.
     * @param certificateId Id of certificate
     * @return  List of messages
     */
    List<Arende> findByIntygsId(String certificateId);

    @Query("select a from Arende a where a.intygsId in :certificateIds")
    List<Arende> getArendenByCertificateIds(@Param("certificateIds") List<String> certificateIds);

    default int eraseArendenByCertificateIds(List<String> certificateIds) {
        final var arendeList = getArendenByCertificateIds(certificateIds);
        deleteAll(arendeList);
        return arendeList.size();
    }
}
