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
package se.inera.intyg.intygstjanst.persistence.model.dao.impl;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiver;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiverDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * DAO test, uses @ContextConfiguration e.g. real DB.
 *
 * @author eriklupander
 */
public class ApprovedReceiverDaoImplTest extends TestSupport {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ApprovedReceiverDao approvedReceiverDao;

    /*
     * Due to the overhead of creating/rollbacking each test, all tests goes into the same method.
     */
    @Test
    public void testPersistAndRead() {
        String intygsId = UUID.randomUUID().toString();
        String intygsId2 = UUID.randomUUID().toString();

        ApprovedReceiver ar = new ApprovedReceiver();
        ar.setCertificateId(intygsId);
        ar.setReceiverId("FK");
        ar.setApproved(false);

        ApprovedReceiver ar2 = new ApprovedReceiver();
        ar2.setCertificateId(intygsId);
        ar2.setReceiverId("SK");
        ar2.setApproved(false);

        ApprovedReceiver ar3 = new ApprovedReceiver();
        ar3.setCertificateId(intygsId2);
        ar3.setReceiverId("FK");
        ar3.setApproved(true);
        approvedReceiverDao.store(ar);
        approvedReceiverDao.store(ar2);
        approvedReceiverDao.store(ar3);

        List<ApprovedReceiver> allowedRecipientIds = approvedReceiverDao.getApprovedReceiverIdsForCertificate(intygsId);
        assertEquals(2, allowedRecipientIds.size());

        List<ApprovedReceiver> allowedRecipientIds2 = approvedReceiverDao.getApprovedReceiverIdsForCertificate(intygsId2);
        assertEquals(1, allowedRecipientIds2.size());
    }

}