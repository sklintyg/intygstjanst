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
import java.util.Map;
import java.util.Optional;

/**
 * Created by eriklupander on 2016-02-02.
 */
public interface RelationDao {

    List<Relation> getChildren(String intygsId);

    List<Relation> getParent(String intygsId);

    Map<String, List<Relation>> getRelations(List<String> certificateIds, List<String> revokedCertificateIds);

    List<Relation> getGraph(String intygsId);

    void store(Relation relation);

    /**
     * A certificate cannot have more than zero or exactly one parent relation.
     *
     * @return The parent relation (e.g. relation that points at the Certificate the identified certificate originated from.
     */
    Optional<Relation> getParentRelation(String intygsId);

    /**
     * Erase any data related to test certificates passed as ids.
     * @param ids   Certificate ids.
     */
    void eraseTestCertificates(List<String> ids);

    /**
     * Erase any relations for certificates, passed as certificate ids, from specific care provider.
     * @param ids   Certificate ids.
     */
    void eraseCertificateRelations(List<String> ids, String careProvider);
}
