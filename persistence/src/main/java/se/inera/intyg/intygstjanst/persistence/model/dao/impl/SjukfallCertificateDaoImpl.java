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
 * Uses JPQL to query {@link SjukfallCertificate} a list of sjukfall related intyg.
 *
 * @author eriklupander
 */
@Repository
public class SjukfallCertificateDaoImpl implements SjukfallCertificateDao {

    private static final Logger LOG = LoggerFactory.getLogger(SjukfallCertificateDaoImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<SjukfallCertificate> findActiveSjukfallCertificateForCareUnits(List<String> careUnitHsaIds) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        // First, get personnummer for all patients having a currently ongoing intyg.
        List<String> personNummerList = entityManager.createQuery(
                "SELECT DISTINCT sc.civicRegistrationNumber FROM SjukfallCertificate sc JOIN "
                + "sc.sjukfallCertificateWorkCapacity scwc WHERE "
                + "    sc.careUnitId IN (:careUnitHsaId) "
                + "AND scwc.fromDate <= :today "
                + "AND scwc.toDate >= :today "
                + "AND sc.deleted = false "
                + "ORDER BY sc.civicRegistrationNumber", String.class)

                .setParameter("careUnitHsaId", careUnitHsaIds)
                .setParameter("today", today)
                .getResultList();

        // Remove this or change to debug later on.
        LOG.info("Get personnr with active intyg on enhet {} (with mottagningar) returned {} items.", careUnitHsaIds, personNummerList.size());

        // Then, fetch all SjukfallCertificates for these persons on the designated units.
        List<SjukfallCertificate> resultList = entityManager.createQuery(
                "SELECT DISTINCT sc FROM SjukfallCertificate sc "
                + "JOIN FETCH sc.sjukfallCertificateWorkCapacity scwc "
                + "WHERE sc.civicRegistrationNumber IN (:personNummerList) "
                + "AND sc.careUnitId IN (:careUnitHsaId) "
                + "AND sc.deleted = false "
                + "ORDER BY sc.civicRegistrationNumber",
                SjukfallCertificate.class)

                .setParameter("careUnitHsaId", careUnitHsaIds)
                .setParameter("personNummerList", personNummerList)
                .getResultList();


        LOG.info("Read {} SjukfallCertificate for belonging to unit {}",
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
            LOG.error("Could not mark SjukfallCert {} as deleted, not found.", id);
            return;
        }
        for (SjukfallCertificate sc : resultList) {
            sc.setDeleted(true);
            entityManager.merge(sc);
            LOG.debug("Successfully marked SjukfallCert {} as deleted", id);
        }
    }
}