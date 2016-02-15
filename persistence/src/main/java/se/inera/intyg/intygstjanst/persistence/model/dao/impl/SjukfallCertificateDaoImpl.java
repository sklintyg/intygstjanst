/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;

/**
 * @author andreaskaltenbach
 */
@Repository
public class SjukfallCertificateDaoImpl implements SjukfallCertificateDao {

    private static final Logger LOG = LoggerFactory.getLogger(SjukfallCertificateDaoImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<SjukfallCertificate> findActiveSjukfallCertificateForCareUnits(List<String> careUnitHsaIds) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

//        List<String> personNummerList = entityManager.createQuery(
//                "SELECT sc.civicRegistrationNumber FROM SjukfallCertificate sc JOIN "
//                + "sc.sjukfallCertificateWorkCapacity scwc WHERE "
//                + "    sc.careUnitId IN (:careUnitHsaId) "
//                + "AND scwc.fromDate <= :today "
//                + "AND scwc.toDate >= :today "
//                + "AND sc.deleted = false "
//                + "ORDER BY sc.civicRegistrationNumber", String.class)
//
//                .setParameter("careUnitHsaId", careUnitHsaIds)
//                .setParameter("today", today)
//                .getResultList();
//
//        // Remove this or change to debug later on.
//        log.info("Get personnr with active intyg on enhet {0} (with mottagningar) returned {1} items.", careUnitHsaIds, personNummerList.size());
//
//
//        List<SjukfallCertificate> resultList = entityManager.createQuery(
//                "SELECT DISTINCT sc FROM SjukfallCertificate sc "
//                + "JOIN FETCH sc.sjukfallCertificateWorkCapacity scwc "
//                + "WHERE sc.civicRegistrationNumber IN (:personNummerList) "
//                + "AND sc.careGiverId = :careGiverId "
//                + "AND sc.deleted = false "
//                + "ORDER BY sc.civicRegistrationNumber",
//                SjukfallCertificate.class)
//
//                .setParameter("careGiverId", parentCareGiverId)
//                .setParameter("personNummerList", personNummerList)
//                .getResultList();
//
//        log.info("Read {0} SjukfallCertificate for {1} patients belonging to one of units {2} organized under care giver {3}",
//                resultList.size(), personNummerList.size(), careUnitHsaIds, parentCareGiverId);

        List<SjukfallCertificate> resultList = entityManager.createQuery(
                "SELECT DISTINCT sc FROM SjukfallCertificate sc JOIN FETCH "
                        + "sc.sjukfallCertificateWorkCapacity scwc WHERE "
                        + "    sc.careUnitId IN (:careUnitHsaId) "
                        + "AND scwc.fromDate <= :today "
                        + "AND scwc.toDate >= :today "
                        + "AND sc.deleted = false "
                        + "ORDER BY sc.civicRegistrationNumber", SjukfallCertificate.class)

                .setParameter("careUnitHsaId", careUnitHsaIds)
                .setParameter("today", today)
                .getResultList();


        LOG.info("Read {0} SjukfallCertificate for belonging to unit {1}",
                resultList.size(), careUnitHsaIds);

        return resultList;
    }

    @Override
    public void store(SjukfallCertificate sjukfallCert) {
        entityManager.persist(sjukfallCert);
    }

    @Override
    public void revoke(String id) {

        List<SjukfallCertificate> resultList = entityManager.createQuery("SELECT sc FROM SjukfallCertificate sc "
                + "WHERE sc.id=:id", SjukfallCertificate.class)
                .setParameter("id", id)
                .getResultList();

        if (resultList.size() == 0) {
            LOG.error("Could not mark SjukfallCert {0} as deleted, not found.", id);
            return;
        }
        for (SjukfallCertificate sc : resultList) {
            sc.setDeleted(true);
            entityManager.merge(sc);
            LOG.debug("Successfully marked SjukfallCert {0} as deleted", id);
        }
    }
}
