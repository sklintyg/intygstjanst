/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.service;

import java.util.List;
import java.util.Optional;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;

/**
 * Created by eriklupander on 2017-05-10.
 */
public interface RelationService {

    /**
     * Stores a single relation.
     *
     * @param relation The relation to store.
     */
    void storeRelation(Relation relation);

    /**
     * Builds a full relation graph. Parent relations will be linear while child relations may branch out.
     * Ordered by created date.
     *
     * @param intygsId intygsId of the certficate whose relations to query.
     * @return a full relation graph
     */
    List<Relation> getRelationGraph(String intygsId);

    /**
     * Returns the parent relation of the specified intygsId.
     *
     * @param intygsId intygsId of the certficate whose relations to query.
     * @return Returns null if no such relation exists.
     */
    Optional<Relation> getParentRelation(String intygsId);

    /**
     * Returns all child relations, non-recursively - e.g. only direct descendants will be returned.
     *
     * @param intygsId intygsId of the certficate whose relations to query.
     * @return List of direct descendants related to the specified intygsId.
     */
    List<Relation> getChildRelations(String intygsId);

}
