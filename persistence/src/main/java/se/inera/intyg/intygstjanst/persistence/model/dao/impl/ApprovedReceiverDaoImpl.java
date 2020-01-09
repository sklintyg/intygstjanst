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
package se.inera.intyg.intygstjanst.persistence.model.dao.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import se.inera.intyg.intygstjanst.persistence.config.JpaConstants;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiver;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiverDao;

/**
 * Relations.
 *
 * @author eriklupander
 */
@Repository
public class ApprovedReceiverDaoImpl implements ApprovedReceiverDao {

    @PersistenceContext(unitName = JpaConstants.PERSISTANCE_UNIT_NAME)
    private EntityManager entityManager;

    @Override
    public List<ApprovedReceiver> getApprovedReceiverIdsForCertificate(String intygsId) {
        return entityManager.createQuery("SELECT ar FROM ApprovedReceiver ar WHERE ar.certificateId = :intygsId", ApprovedReceiver.class)
            .setParameter("intygsId", intygsId)
            .getResultList();
    }

    @Override
    public void clearApprovedReceiversForCertificate(String intygsId) {
        List<ApprovedReceiver> resultList = entityManager
            .createQuery("SELECT ar FROM ApprovedReceiver ar WHERE ar.certificateId = :intygsId", ApprovedReceiver.class)
            .setParameter("intygsId", intygsId)
            .getResultList();
        for (ApprovedReceiver ar : resultList) {
            entityManager.remove(ar);
        }
    }

    @Override
    public void store(ApprovedReceiver approvedReceiver) {
        entityManager.persist(approvedReceiver);
    }
}
