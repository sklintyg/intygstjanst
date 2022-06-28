/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.persistence.model.dao.RelationDao;

/**
 * DAO test, uses @ContextConfiguration e.g. real DB.
 *
 * @author eriklupander
 */
public class RelationDaoImplTest extends TestSupport {

    // Försöker ha intygens ID-nummer i kronologisk ordning, dvs. intyg-2 skall i testet peka på intyg-1
    private static final String START_INTYG = "intyg-2";
    private static final String PARENT_INTYG_1 = "intyg-1";
    public static final String INTYG_0 = "intyg-0";
    public static final String INTYG_1 = "intyg-1";
    public static final String INTYG_2 = "intyg-2";
    public static final String INTYG_3 = "intyg-3";
    public static final String INTYG_0_1 = "intyg-0-1";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private RelationDao relationDao;

    /*
     * Due to the overhead of creating/rollbacking each test, all tests goes into the same method.
     */
    @Test
    public void testBuildGraph() {
        buildRelationTree();

        List<Relation> graph = relationDao.getGraph(INTYG_2);
        assertEquals(3, graph.size());
        assertEquals(INTYG_1, graph.get(0).getFromIntygsId());
        assertEquals(INTYG_2, graph.get(1).getFromIntygsId());
        assertEquals(INTYG_3, graph.get(2).getFromIntygsId());

        graph = relationDao.getGraph(INTYG_0);
        assertEquals(4, graph.size());

        graph = relationDao.getGraph(INTYG_3);
        assertEquals(3, graph.size());

        graph = relationDao.getChildren(INTYG_0);
        assertEquals(2, graph.size());
        graph = relationDao.getChildren(INTYG_1);
        assertEquals(1, graph.size());
        graph = relationDao.getChildren(INTYG_3);
        assertEquals(0, graph.size());
        graph = relationDao.getParent(INTYG_0);
        assertEquals(0, graph.size());
        graph = relationDao.getParent(INTYG_0_1);
        assertEquals(1, graph.size());
    }

    @Test
    public void shouldEraseRelationsWhereToOrFromFieldHasCertificateForRemoval() {
        buildRelationTree();

        relationDao.eraseCertificateRelations(List.of(INTYG_1), "5678");

        final var relations = entityManager.createQuery("SELECT r.id From Relation r", Long.class).getResultList();
        assertEquals(2, relations.size());
    }

    private void buildRelationTree() {
        Relation r0 = new Relation(INTYG_1, INTYG_0, RelationKod.FRLANG.value(), LocalDateTime.now().minusDays(30));
        Relation r1 = new Relation(INTYG_2, INTYG_1, RelationKod.ERSATT.value(), LocalDateTime.now().minusDays(20));
        Relation r2 = new Relation(INTYG_3, INTYG_2, RelationKod.FRLANG.value(), LocalDateTime.now().minusDays(10));
        Relation r01 = new Relation(INTYG_0_1, INTYG_0, RelationKod.KOMPLT.value(), LocalDateTime.now().minusDays(25));

        relationDao.store(r0);
        relationDao.store(r1);
        relationDao.store(r2);
        relationDao.store(r01);
    }

}
