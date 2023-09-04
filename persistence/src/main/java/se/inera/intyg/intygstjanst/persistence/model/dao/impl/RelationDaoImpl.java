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
package se.inera.intyg.intygstjanst.persistence.model.dao.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.intygstjanst.persistence.config.JpaConstants;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.persistence.model.dao.RelationDao;

/**
 * Relations.
 *
 * @author eriklupander
 */
@Repository
public class RelationDaoImpl implements RelationDao {

    private static final Logger LOG = LoggerFactory.getLogger(RelationDaoImpl.class);

    @PersistenceContext(unitName = JpaConstants.PERSISTANCE_UNIT_NAME)
    private EntityManager entityManager;

    @Override
    public List<Relation> getChildren(String intygsId) {
        return entityManager.createQuery("SELECT r FROM Relation r WHERE r.toIntygsId = :intygsId", Relation.class)
            .setParameter("intygsId", intygsId)
            .getResultList();
    }

    @Override
    public List<Relation> getParent(String intygsId) {
        return entityManager.createQuery("SELECT r FROM Relation r WHERE r.fromIntygsId = :intygsId", Relation.class)
            .setParameter("intygsId", intygsId)
            .getResultList();
    }

    @Override
    public List<Relation> getRelations(List<String> certificateIds, List<String> relationCodes) {
        final var query = entityManager.createQuery(
                "SELECT r FROM Relation r WHERE r.toIntygsId IN :certificateIds OR r.fromIntygsId IN :certificateIds AND r.relationKod IN :relationCodes", Relation.class
                )
                .setParameter("certificateIds", certificateIds);

        if (relationCodes != null && !relationCodes.isEmpty()) {
            query.setParameter("relationCodes", relationCodes);
        }

        return query.getResultList();
    }

    @Override
    public List<Relation> getGraph(String intygsId) {
        List<Relation> graph = new ArrayList<>();
        buildChildGraph(intygsId, graph);
        buildParentGraph(intygsId, graph);

        return graph.stream()
            .sorted(Comparator.comparing(Relation::getCreated))
            .collect(Collectors.toList());
    }

    @Override
    public void store(Relation relation) {
        entityManager.persist(relation);
    }

    @Override
    public Optional<Relation> getParentRelation(String intygsId) {
        List<Relation> parentRelations = getParent(intygsId);
        if (parentRelations.size() >= 1) {
            return Optional.of(parentRelations.get(0));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void eraseTestCertificates(List<String> ids) {
        for (var id: ids) {
            final var relationList = getGraph(id);
            for (var relation: relationList) {
                entityManager.remove(relation);
            }
        }
    }

    @Override
    @Transactional
    public void eraseCertificateRelations(List<String> certificateIds, String careProviderId) {
        for (final var certificateId : certificateIds) {
            final var relationList = new ArrayList<Relation>();
            relationList.addAll(getParent(certificateId));
            relationList.addAll(getChildren(certificateId));
            for (var relation : relationList) {
                entityManager.remove(relation);
                LOG.debug("Relation with id {} for certificate id {} from care provider {} was successfully erased.", relation.getId(),
                    certificateId, careProviderId);
            }
        }
    }

    /**
     * Starting with a given intyg, builds a graph of descendants.
     */
    private void buildChildGraph(String intygsId, List<Relation> graph) {

        List<Relation> children = getChildren(intygsId);
        for (Relation child : children) {
            graph.add(child);
            buildChildGraph(child.getFromIntygsId(), graph);
        }
    }

    /**
     * Builds a graph of ancestors direction.
     */
    private void buildParentGraph(String intygsId, List<Relation> graph) {

        List<Relation> parents = getParent(intygsId);
        for (Relation parent : parents) {
            graph.add(parent);
            buildParentGraph(parent.getToIntygsId(), graph);
        }
    }
}
