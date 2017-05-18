package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;

import java.util.List;
import java.util.Optional;

/**
 * Created by eriklupander on 2017-05-10.
 */
public interface RelationService {

    /**
     * Stores a single relation.
     *
     * @param relation
     *            The relation to store.
     */
    void storeRelation(Relation relation);

    /**
     * Builds a full relation graph. Parent relations will be linear while child relations may branch out.
     * Ordered by created date.
     *
     * @param intygsId
     *            intygsId of the certficate whose relations to query.
     * @return
     *         a full relation graph
     */
    List<Relation> getRelationGraph(String intygsId);

    /**
     * Returns the parent relation of the specified intygsId.
     *
     * @param intygsId
     *            intygsId of the certficate whose relations to query.
     * @return
     *         Returns null if no such relation exists.
     */
    Optional<Relation> getParentRelation(String intygsId);

    /**
     * Returns all child relations, non-recursively - e.g. only direct descendants will be returned.
     *
     * @param intygsId
     *            intygsId of the certficate whose relations to query.
     * @return
     *         List of direct descendants related to the specified intygsId.
     */
    List<Relation> getChildRelations(String intygsId);

}
