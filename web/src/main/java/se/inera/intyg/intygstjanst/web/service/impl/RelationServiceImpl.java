/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
