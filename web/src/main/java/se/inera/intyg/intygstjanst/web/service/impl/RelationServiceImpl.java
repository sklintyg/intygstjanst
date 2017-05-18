package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.persistence.model.dao.RelationDao;
import se.inera.intyg.intygstjanst.web.service.RelationService;

import java.util.List;
import java.util.Optional;

/**
 * Created by eriklupander on 2017-05-10.
 */
@Service
public class RelationServiceImpl implements RelationService {

    @Autowired
    private RelationDao relationDao;

    @Override
    public void storeRelation(Relation relation) {
        relationDao.store(relation);
    }

    @Override
    public List<Relation> getRelationGraph(String intygsId) {
        return relationDao.getGraph(intygsId);
    }

    @Override
    public Optional<Relation> getParentRelation(String intygsId) {
        return relationDao.getParentRelation(intygsId);
    }

    @Override
    public List<Relation> getChildRelations(String intygsId) {
        return relationDao.getChildren(intygsId);
    }
}
