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

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.persistence.config.JpaConstants;
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

    private static final String BASE_QUERY = "SELECT sc.civicRegistrationNumber "
        + "FROM SjukfallCertificate sc "
        + "JOIN sc.sjukfallCertificateWorkCapacity scwc "
        + "WHERE sc.careGiverId = :careGiverHsaId "
        + "AND sc.careUnitId IN (:careUnitHsaId) "
        + "AND ((scwc.fromDate <= :today AND scwc.toDate >= :today) " // active today or...
        + "  OR (scwc.toDate < :today AND scwc.toDate >= :recentlyClosed)) " // recently closed
        + "AND sc.deleted = FALSE";

    // An intyg is excluded from being part of a Sjukfall if there is a relation KOMPLT or ERSATT to it,
    // unless the intyg replacing or complementing it (e.g. the "from") has been revoked.
    private static final String EXCLUDE_REPLACED_INTYG_QUERY = "SELECT TO_INTYG_ID FROM RELATION r "
        + "INNER JOIN SJUKFALL_CERT sc ON sc.ID = r.TO_INTYG_ID AND "
        + "r.RELATION_KOD IN "
        + "     ('" + RelationKod.ERSATT.value() + "','" + RelationKod.KOMPLT.value() + "') "
        + "JOIN SJUKFALL_CERT sc2 ON sc2.ID = r.FROM_INTYG_ID " // Makes sure the intyg that replaces/complemements
        + " AND sc2.deleted = FALSE "                           // an intyg hasn't been revoked.
        + "WHERE sc.CIVIC_REGISTRATION_NUMBER IN (:pnrList)";

    @PersistenceContext(unitName = JpaConstants.PERSISTANCE_UNIT_NAME)
    private EntityManager entityManager;

    @Override
    public List<SjukfallCertificate> findActiveSjukfallCertificate(String careGiverId, List<String> unitIds, List<String> doctorIds,
        LocalDate activeDate, LocalDate recentlyClosed) {

        final var queryStrBuilder = new StringBuilder();
        queryStrBuilder.append("SELECT DISTINCT sc FROM SjukfallCertificate sc ");
        queryStrBuilder.append("JOIN FETCH sc.sjukfallCertificateWorkCapacity scwc ");
        queryStrBuilder.append("WHERE sc.careGiverId = :careGiverHsaId ");
        queryStrBuilder.append("AND sc.careUnitId IN (:careUnitHsaId) ");
        if (doctorIds != null && !doctorIds.isEmpty()) {
            queryStrBuilder.append("AND sc.signingDoctorId IN (:doctorId) ");
        }
        queryStrBuilder.append("AND ((scwc.fromDate <= :today AND scwc.toDate >= :today) ");
        if (recentlyClosed != null) {
            queryStrBuilder.append("OR (scwc.toDate < :today AND scwc.toDate >= :recentlyClosed)) ");
        } else {
            queryStrBuilder.append(")");
        }
        queryStrBuilder.append("AND sc.deleted = FALSE ");
        queryStrBuilder.append("AND sc.testCertificate = FALSE ");

        final var sjukfallCertificateTypedQuery = entityManager.createQuery(queryStrBuilder.toString(), SjukfallCertificate.class)
            .setParameter("careGiverHsaId", careGiverId)
            .setParameter("careUnitHsaId", unitIds)
            .setParameter("today", activeDate.format(DateTimeFormatter.ISO_DATE));

        if (doctorIds != null && !doctorIds.isEmpty()) {
            sjukfallCertificateTypedQuery.setParameter("doctorId", doctorIds);
        }

        if (recentlyClosed != null) {
            sjukfallCertificateTypedQuery.setParameter("recentlyClosed", recentlyClosed.format(DateTimeFormatter.ISO_DATE));
        }

        return sjukfallCertificateTypedQuery.getResultList();
    }

    @Override
    public List<SjukfallCertificate> findAllSjukfallCertificate(String careGiverId, List<String> unitIds, List<String> patientIds) {
        return querySjukfallCertificatesForUnitsAndPersonnummer(careGiverId, unitIds, patientIds, null, false);
    }

    @Override
    public List<SjukfallCertificate> findActiveSjukfallCertificateForCareUnits(
        String careGiverHsaId, List<String> careUnitHsaIds, int maxDagarSedanAvslut) {

        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String recentlyClosed = LocalDate.now().minusDays(maxDagarSedanAvslut).format(DateTimeFormatter.ISO_DATE);

        // First, get personnummer for all patients having a currently ongoing intyg.
        List<String> personNummerList = entityManager
            .createQuery(BASE_QUERY + "", String.class)
            .setParameter("careGiverHsaId", careGiverHsaId)
            .setParameter("careUnitHsaId", careUnitHsaIds)
            .setParameter("today", today)
            .setParameter("recentlyClosed", recentlyClosed)
            .getResultList();

        return querySjukfallCertificatesForUnitsAndPersonnummer(careGiverHsaId, careUnitHsaIds, personNummerList, recentlyClosed, false);
    }

    @Override
    public List<SjukfallCertificate> findActiveSjukfallCertificateForCareUnits(String careGiverHsaId, List<String> careUnitHsaIds,
        int maxDagarSedanAvslut, boolean onlyActiveSickLeaves) {

        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String recentlyClosed = LocalDate.now().minusDays(maxDagarSedanAvslut).format(DateTimeFormatter.ISO_DATE);

        // First, get personnummer for all patients having a currently ongoing intyg.
        List<String> personNummerList = entityManager
            .createQuery(BASE_QUERY + "", String.class)
            .setParameter("careGiverHsaId", careGiverHsaId)
            .setParameter("careUnitHsaId", careUnitHsaIds)
            .setParameter("today", today)
            .setParameter("recentlyClosed", recentlyClosed)
            .getResultList();

        return querySjukfallCertificatesForUnitsAndPersonnummer(careGiverHsaId, careUnitHsaIds, personNummerList, recentlyClosed,
            onlyActiveSickLeaves);
    }

    @Override
    public List<SjukfallCertificate> findActiveSjukfallCertificateForPersonOnCareUnits(
        String careGiverHsaId, List<String> careUnitHsaIds, String personnummer, int maxDagarSedanAvslut) {

        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String recentlyClosed = LocalDate.now().minusDays(maxDagarSedanAvslut).format(DateTimeFormatter.ISO_DATE);
        String query = BASE_QUERY
            + " AND sc.civicRegistrationNumber = :civicRegistrationNumber";

        List<String> personNummerList = entityManager
            .createQuery(query, String.class)
            .setParameter("careGiverHsaId", careGiverHsaId)
            .setParameter("careUnitHsaId", careUnitHsaIds)
            .setParameter("today", today)
            .setParameter("recentlyClosed", recentlyClosed)
            .setParameter("civicRegistrationNumber", personnummer)
            .getResultList();

        return querySjukfallCertificatesForUnitsAndPersonnummer(careGiverHsaId, careUnitHsaIds, personNummerList, recentlyClosed, false);
    }

    @Override
    public List<SjukfallCertificate> findSjukfallCertificateForPerson(String personnummer) {
        return querySjukfallCertificatesForPersonnummer(personnummer);
    }

    @Override
    public void store(SjukfallCertificate sjukfallCert) {
        entityManager.persist(sjukfallCert);
    }

    @Override
    public void revoke(String id) {

        List<SjukfallCertificate> resultList = entityManager
            .createQuery("SELECT sc FROM SjukfallCertificate sc "
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

    @Override
    public void eraseTestCertificates(List<String> ids) {
        final var sjukfallCertificateList = entityManager
            .createQuery("SELECT sc FROM SjukfallCertificate sc "
                + "WHERE sc.id IN (:ids)", SjukfallCertificate.class)
            .setParameter("ids", ids)
            .getResultList();

        for (var sjukfallCertificate : sjukfallCertificateList) {
            entityManager.remove(sjukfallCertificate);
        }
    }

    @Override
    @Transactional
    public int eraseCertificates(List<String> certificateIds, String careProviderId) {
        int erasedCertificatesCount = 0;

        if (certificateIds.isEmpty()) {
            return erasedCertificatesCount;
        }

        final var sjukfallCertificates = entityManager
            .createQuery("SELECT sc FROM SjukfallCertificate sc WHERE sc.id in :certificateIds", SjukfallCertificate.class)
            .setParameter("certificateIds", certificateIds)
            .getResultList();

        for (var sjukfallCertificate : sjukfallCertificates) {
            entityManager.remove(sjukfallCertificate);
            erasedCertificatesCount++;
            LOG.debug("SjukfallCertificate with id {} from care provider {} was successfully erased.", sjukfallCertificate.getId(),
                careProviderId);
        }

        return erasedCertificatesCount;
    }

    private List<SjukfallCertificate> querySjukfallCertificatesForPersonnummer(String personnummer) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Get active intyg on all care givers and their units for a personnummer.");
        }

        // if no personnummer found, return empty list
        if (Strings.isNullOrEmpty(personnummer)) {
            return new ArrayList<>();
        }

        // Next, fetch a list of all replaced/complemented intygsId for the patients in the list
        List<String> replacedOrComplementedIntygsIdList = replacedOrComplementedIntygForPersonnummerList(Arrays.asList(personnummer));

        // Prepare jpql query
        String jpql = "SELECT DISTINCT sc "
            + "FROM SjukfallCertificate sc "
            + "JOIN FETCH sc.sjukfallCertificateWorkCapacity scwc "
            + "WHERE sc.deleted = FALSE "
            + "AND sc.civicRegistrationNumber = :personnummer ";

        // Only add the "is replaced"-stuff if there's entries to possibly exclude.
        if (isNotEmpty(replacedOrComplementedIntygsIdList)) {
            jpql += "AND sc.id NOT IN (:replacedOrComplementedIntygsIdList)";
        }

        TypedQuery<SjukfallCertificate> query = entityManager
            .createQuery(jpql, SjukfallCertificate.class)
            .setParameter("personnummer", personnummer);

        if (isNotEmpty(replacedOrComplementedIntygsIdList)) {
            query = query.setParameter("replacedOrComplementedIntygsIdList", replacedOrComplementedIntygsIdList);
        }

        // Finally, fetch all SjukfallCertificates removing any sjukfall from
        // the replaced or complemented list.
        List<SjukfallCertificate> resultList = query.getResultList();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Read {} SjukfallCertificate", resultList.size());
        }

        return resultList.stream()
            .sorted(Comparator.comparing(SjukfallCertificate::getCivicRegistrationNumber))
            .collect(Collectors.toList());
    }

    private List<SjukfallCertificate> querySjukfallCertificatesForUnitsAndPersonnummer(
        String careGiverHsaId, List<String> careUnitHsaIds, List<String> pnrList, String recentlyClosed, boolean onlyActiveSickLeaves) {

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

        // Prepare the final query
        String jpql = "SELECT DISTINCT sc FROM SjukfallCertificate sc "
            + "JOIN FETCH sc.sjukfallCertificateWorkCapacity scwc "
            + "WHERE sc.civicRegistrationNumber IN (:personNummerList) "
            + "AND sc.careUnitId IN (:careUnitHsaIds) "
            + "AND sc.careGiverId = :careGiverHsaId "
            + "AND sc.deleted = FALSE ";
        // TODO: Add testCertificate = false

        // Only add the "is replaced"-stuff if there's entries to possibly exclude.
        if (isNotEmpty(replacedOrComplementedIntygsIdList)) {
            jpql += "AND sc.id NOT IN (:replacedOrComplementedIntygsIdList)";
        }
        if (onlyActiveSickLeaves) {
            jpql += "AND ((scwc.fromDate <= :today AND scwc.toDate >= :today)";
            jpql += " OR (scwc.toDate < :today AND scwc.toDate >= :recentlyClosed)) ";
        }

        TypedQuery<SjukfallCertificate> query = entityManager.createQuery(jpql, SjukfallCertificate.class)
            .setParameter("careGiverHsaId", careGiverHsaId)
            .setParameter("careUnitHsaIds", careUnitHsaIds)
            .setParameter("personNummerList", personNummerList);

        if (isNotEmpty(replacedOrComplementedIntygsIdList)) {
            query = query.setParameter("replacedOrComplementedIntygsIdList", replacedOrComplementedIntygsIdList);
        }
        if (onlyActiveSickLeaves) {
            String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            query = query.setParameter("today", today);
            query = query.setParameter("recentlyClosed", recentlyClosed);
        }

        // Finally, fetch all SjukfallCertificates for these persons on the designated units, removing any sjukfall from
        // the replaced or complemented list.
        List<SjukfallCertificate> resultList = query.getResultList();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Read {} SjukfallCertificate for belonging to unit {}",
                resultList.size(), careUnitHsaIds);
        }

        return resultList.stream()
            .sorted(Comparator.comparing(SjukfallCertificate::getCivicRegistrationNumber))
            .collect(Collectors.toList());
    }

    private boolean isNotEmpty(List<String> replacedOrComplementedIntygsIdList) {
        return replacedOrComplementedIntygsIdList != null && replacedOrComplementedIntygsIdList.size() > 0;
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
