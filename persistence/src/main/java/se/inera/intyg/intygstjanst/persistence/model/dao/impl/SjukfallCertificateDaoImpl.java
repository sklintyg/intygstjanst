/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Uses JPQL to query {@link SjukfallCertificate} a list of sjukfall related intyg.
 *
 * @author eriklupander
 */
@Repository
public class SjukfallCertificateDaoImpl implements SjukfallCertificateDao {

    private static final Logger LOG = LoggerFactory.getLogger(SjukfallCertificateDaoImpl.class);

    private static final String BASE_QUERY = "SELECT sc.civicRegistrationNumber FROM SjukfallCertificate sc JOIN "
            + "sc.sjukfallCertificateWorkCapacity scwc WHERE "
            + "    sc.careUnitId IN (:careUnitHsaId) "
            + "AND scwc.fromDate <= :today "
            + "AND scwc.toDate >= :today "
            + "AND sc.deleted = FALSE";

    private static final String EXCLUDE_REPLACED_INTYG_QUERY = "SELECT TO_INTYG_ID FROM RELATION r "
            + "INNER JOIN SJUKFALL_CERT sc ON sc.ID = r.TO_INTYG_ID AND "
            + "r.RELATION_KOD IN "
            + "     ('" + RelationKod.ERSATT.value() + "','" + RelationKod.KOMPLT.value() + "') "
            + "WHERE sc.CIVIC_REGISTRATION_NUMBER IN (:pnrList)";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<SjukfallCertificate> findActiveSjukfallCertificateForCareUnits(List<String> careUnitHsaIds) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        // First, get personnummer for all patients having a currently ongoing intyg.
        List<String> personNummerList = entityManager.createQuery(
                BASE_QUERY,
                String.class)
                .setParameter("careUnitHsaId", careUnitHsaIds)
                .setParameter("today", today)
                .getResultList();

        return querySjukfallCertificatesForUnitsAndPersonnummer(careUnitHsaIds, personNummerList);
    }

    @Override
    public List<SjukfallCertificate> findActiveSjukfallCertificateForPersonOnCareUnits(List<String> careUnitHsaIds, String personnummer) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        List<String> personNummerList = entityManager.createQuery(
                BASE_QUERY + " AND sc.civicRegistrationNumber = :civicRegistrationNumber",
                String.class)
                .setParameter("careUnitHsaId", careUnitHsaIds)
                .setParameter("today", today)
                .setParameter("civicRegistrationNumber", personnummer)
                .getResultList();

        return querySjukfallCertificatesForUnitsAndPersonnummer(careUnitHsaIds, personNummerList);
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

        if (resultList.isEmpty()) {
            LOG.error("Could not mark SjukfallCert {} as deleted, not found.", id);
            return;
        }
        for (SjukfallCertificate sc : resultList) {
            sc.setDeleted(true);
            entityManager.merge(sc);
            LOG.debug("Successfully marked SjukfallCert {} as deleted", id);
        }
    }

    private List<SjukfallCertificate> querySjukfallCertificatesForUnitsAndPersonnummer(List<String> careUnitHsaIds,
            List<String> pnrList) {
        // Perform the DISTINCT and SORT programatically.
        List<String> personNummerList = pnrList.stream().distinct().sorted().collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Get personnr with active intyg on enhet {} (with mottagningar) returned {} items.", careUnitHsaIds,
                    personNummerList.size());
        }

        // if no personnummer found, return empty list
        if (personNummerList.isEmpty()) {
            return new ArrayList<>();
        }

        // Next, fetch a list of all replaced/complemented intygsId for the patients in the list
        List<String> replacedOrComplementedIntygsIdList = replacedOrComplementedIntygForPersonnummerList(personNummerList);

        // Then, fetch all SjukfallCertificates for these persons on the designated units, removing any sjukfall from the
        // replaced or complemented list.
        List<SjukfallCertificate> resultList = entityManager.createQuery(
                "SELECT DISTINCT sc FROM SjukfallCertificate sc "
                        + "JOIN FETCH sc.sjukfallCertificateWorkCapacity scwc "
                        + "WHERE sc.civicRegistrationNumber IN (:personNummerList) "
                        + "AND sc.careUnitId IN (:careUnitHsaIds) "
                        + "AND sc.id NOT IN (:replacedOrComplementedIntygsIdList) "
                        + "AND sc.deleted = FALSE",
                SjukfallCertificate.class)

                .setParameter("careUnitHsaIds", careUnitHsaIds)
                .setParameter("personNummerList", personNummerList)
                .setParameter("replacedOrComplementedIntygsIdList", replacedOrComplementedIntygsIdList)
                .getResultList();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Read {} SjukfallCertificate for belonging to unit {}",
                    resultList.size(), careUnitHsaIds);
        }
        return resultList.stream()
                .sorted(Comparator.comparing(SjukfallCertificate::getCivicRegistrationNumber))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private List<String> replacedOrComplementedIntygForPersonnummerList(List<String> pnrList) {

        // Note: This MUST be done using native SQL unless we upgrade to Hibernate 5.1+ or later where JOINs between
        // unmapped relations are possible.
        return entityManager.createNativeQuery(EXCLUDE_REPLACED_INTYG_QUERY)
                .setParameter("pnrList", pnrList)
                .getResultList();
    }

}
