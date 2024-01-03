/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.peristence.dao.util.DaoUtil;
import se.inera.intyg.intygstjanst.persistence.config.JpaConstants;
import se.inera.intyg.intygstjanst.persistence.exception.PersistenceException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateMetaData;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateType;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.schemas.contract.Personnummer;

/**
 * Implementation of {@link CertificateDao}.
 */
@Repository
public class CertificateDaoImpl implements CertificateDao {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateDaoImpl.class);
    private static final int MONTHS = -3;

    @PersistenceContext(unitName = JpaConstants.PERSISTANCE_UNIT_NAME)
    private EntityManager entityManager;

    @Override
    public List<Certificate> findCertificates(Personnummer civicRegistrationNumber, String[] units,
        LocalDateTime fromDate, LocalDateTime toDate, String orderBy, boolean orderAscending, Set<String> types, String doctorId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Certificate> query = criteriaBuilder.createQuery(Certificate.class);
        Root<Certificate> root = query.from(Certificate.class);

        List<Predicate> predicates = new ArrayList<>();

        if (civicRegistrationNumber != null) {
            predicates
                .add(criteriaBuilder.equal(root.get("civicRegistrationNumber"), DaoUtil.formatPnrForPersistence(civicRegistrationNumber)));
        }

        if (doctorId != null) {
            Join<Certificate, CertificateMetaData> certificateMetaData = root.join("certificateMetaData", JoinType.INNER);
            predicates.add(criteriaBuilder.equal(certificateMetaData.get("doctorId"), doctorId));
        }
        if (units != null && units.length > 0) {
            predicates.add(root.get("careUnitId").in((Object[]) units));
        } else {
            return Collections.emptyList();
        }
        if (types != null && !types.isEmpty()) {
            List<String> typesList = new ArrayList<String>(types);
            predicates.add(criteriaBuilder.lower(root.<String>get("type")).in(toLowerCase(typesList)));
        }
        if (toDate != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("signedDate"), toDate.plusDays(1)));
        } else {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("signedDate"), LocalDateTime.now()));
        }
        if (fromDate != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("signedDate"), fromDate));
        } else {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("signedDate"),
                LocalDateTime.now().plusMonths(MONTHS)));
        }

        query.where(predicates.toArray(new Predicate[predicates.size()]));

        if (!("status").equals(orderBy) && !("type").equals(orderBy)) {
            if (orderAscending) {
                query.orderBy(criteriaBuilder.asc(root.get(orderBy)), criteriaBuilder.desc(root.get("signedDate")));
            } else {
                query.orderBy(criteriaBuilder.desc(root.get(orderBy)), criteriaBuilder.desc(root.get("signedDate")));
            }
        } else {
            query.orderBy(criteriaBuilder.desc(root.get("signedDate")));
        }

        List<Certificate> tmpResult = entityManager.createQuery(query).getResultList();
        return filterDuplicates(tmpResult);
    }


    @Override
    public List<Certificate> findCertificate(Personnummer civicRegistrationNumber, List<String> types, LocalDate fromDate, LocalDate toDate,
        List<String> careUnits) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Certificate> query = criteriaBuilder.createQuery(Certificate.class);
        Root<Certificate> root = query.from(Certificate.class);

        root.fetch("states", JoinType.LEFT);

        if (civicRegistrationNumber == null) {
            return Collections.emptyList();
        }

        List<Predicate> predicates = new ArrayList<>();

        // meta data has to match civic registration number
        predicates
            .add(criteriaBuilder.equal(root.get("civicRegistrationNumber"), DaoUtil.formatPnrForPersistence(civicRegistrationNumber)));

        // filter by certificate types
        if (types != null && !types.isEmpty()) {
            predicates.add(criteriaBuilder.lower(root.<String>get("type")).in(toLowerCase(types)));
        }

        // filter by care unit
        if (careUnits != null && !careUnits.isEmpty()) {
            predicates.add(root.<String>get("careUnitId").in(careUnits));
        }

        if (fromDate != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("signedDate"), fromDate.atStartOfDay()));
        }

        if (toDate != null) {
            predicates.add(criteriaBuilder.lessThan(root.get("signedDate"), toDate.plusDays(1).atStartOfDay()));
        }

        query.where(predicates.toArray(new Predicate[predicates.size()]));

        // order by signed date
        query.orderBy(criteriaBuilder.asc(root.get("signedDate")));

        List<Certificate> tmpResult = entityManager.createQuery(query).getResultList();

        return filterDuplicates(tmpResult);
    }

    @Override
    public List<Certificate> findCertificate(List<String> careUnits, List<String> types, LocalDate fromDate, LocalDate toDate) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Certificate> query = criteriaBuilder.createQuery(Certificate.class);
        Root<Certificate> root = query.from(Certificate.class);

        root.fetch("states", JoinType.LEFT);

        if (careUnits == null || careUnits.isEmpty()) {
            return Collections.emptyList();
        }

        List<Predicate> predicates = new ArrayList<>();

        // filter by care unit
        predicates.add(root.<String>get("careUnitId").in(careUnits));

        // filter by certificate types
        if (types != null && !types.isEmpty()) {
            predicates.add(criteriaBuilder.lower(root.<String>get("type")).in(toLowerCase(types)));
        }

        if (fromDate != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("signedDate"), fromDate.atStartOfDay()));
        }

        if (toDate != null) {
            predicates.add(criteriaBuilder.lessThan(root.get("signedDate"), toDate.plusDays(1).atStartOfDay()));
        }

        query.where(predicates.toArray(new Predicate[0]));

        // order by signed date
        query.orderBy(criteriaBuilder.asc(root.get("signedDate")));

        List<Certificate> tmpResult = entityManager.createQuery(query).getResultList();

        return filterDuplicates(tmpResult);
    }

    @Override
    public List<Certificate> findCertificatesUsingMetaDataTable(List<String> careUnits, List<String> types, LocalDate fromDate,
        LocalDate toDate, List<String> doctorIds) {

        if (!listContainsValues(careUnits)) {
            return Collections.emptyList();
        }

        final var criteriaBuilder = entityManager.getCriteriaBuilder();
        final var query = criteriaBuilder.createQuery(Certificate.class);
        final var root = query.from(Certificate.class);
        final var certificateMetaData = root.join("certificateMetaData", JoinType.INNER);

        root.fetch("states", JoinType.LEFT);
        root.fetch("certificateMetaData", JoinType.INNER);
        root.fetch("originalCertificate", JoinType.INNER);

        final var listOfPredicates = new ArrayList<>();

        listOfPredicates.add(root.<String>get("careUnitId").in(careUnits));

        listOfPredicates.add(criteriaBuilder.equal(certificateMetaData.get("isRevoked"), false));

        if (listContainsValues(doctorIds)) {
            listOfPredicates.add(certificateMetaData.get("doctorId").in(doctorIds));
        }

        if (listContainsValues(types)) {
            listOfPredicates.add(root.<String>get("type").in(toLowerCase(types)));
        }

        if (isNotNull(fromDate)) {
            listOfPredicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("signedDate"), fromDate.atStartOfDay()));
        }

        if (isNotNull(toDate)) {
            listOfPredicates.add(criteriaBuilder.lessThan(root.get("signedDate"), toDate.plusDays(1).atStartOfDay()));
        }

        query.where(listOfPredicates.toArray(new Predicate[listOfPredicates.size()]));

        query.orderBy(criteriaBuilder.asc(root.get("signedDate")));

        return filterDuplicates(entityManager.createQuery(query).getResultList());
    }

    @Override
    public List<String> findDoctorIds(List<String> careUnits, List<String> types, LocalDate fromDate, LocalDate toDate) {

        if (!listContainsValues(careUnits)) {
            return Collections.emptyList();
        }

        final var jpqlBuffer = new StringBuilder(200);
        jpqlBuffer.append("SELECT DISTINCT cm.doctorId ");
        jpqlBuffer.append("FROM CertificateMetaData cm ");
        jpqlBuffer.append("JOIN cm.certificate c ");
        jpqlBuffer.append("WHERE c.careUnitId in (:careUnitIdValue) ");
        jpqlBuffer.append("AND cm.isRevoked = 0 ");

        if (listContainsValues(types)) {
            jpqlBuffer.append("AND c.type in (:typesValue) ");
        }

        if (isNotNull(fromDate)) {
            jpqlBuffer.append("AND c.signedDate >= :fromDateValue ");
        }

        if (isNotNull(toDate)) {
            jpqlBuffer.append("AND c.signedDate < :toDateValue ");
        }

        var query = entityManager
            .createQuery(jpqlBuffer.toString(), String.class)
            .setParameter("careUnitIdValue", careUnits);

        if (listContainsValues(types)) {
            query = query.setParameter("typesValue", types);
        }

        if (isNotNull(fromDate)) {
            query = query.setParameter("fromDateValue", fromDate.atStartOfDay());
        }

        if (isNotNull(toDate)) {
            query = query.setParameter("toDateValue", toDate.plusDays(1).atStartOfDay());
        }

        return query.getResultList();
    }

    private boolean isNotNull(Object object) {
        return object != null;
    }

    private boolean listContainsValues(List list) {
        return list != null && list.size() > 0;
    }

    @Override
    public Certificate getCertificate(Personnummer civicRegistrationNumber, String certificateId) throws PersistenceException {
        Certificate certificate = entityManager.find(Certificate.class, certificateId);

        if (certificate == null) {
            return null;
        }

        // if provided, the civic registration number has to match the certificate's civic registration number
        if (civicRegistrationNumber != null && !certificate.getCivicRegistrationNumber().equals(civicRegistrationNumber)) {

            LOG.warn(String.format("Trying to access certificate '%s' for user '%s' but certificate's user is '%s'.",
                certificateId,
                civicRegistrationNumber.getPersonnummerHash(),
                certificate.getCivicRegistrationNumber().getPersonnummerHash()));

            throw new PersistenceException(certificateId, civicRegistrationNumber);
        }

        return certificate;
    }

    @Override
    public void store(Certificate certificate) {
        entityManager.persist(certificate);
    }

    @Override
    public long storeOriginalCertificate(OriginalCertificate originalCertificate) {
        entityManager.persist(originalCertificate);
        return originalCertificate.getId();
    }

    @Override
    public void storeCertificateMetadata(CertificateMetaData metadata) {
        entityManager.persist(metadata);
    }

    @Override
    public void updateStatus(String id, Personnummer civicRegistrationNumber, CertificateState state, String target,
        LocalDateTime timestamp)
        throws PersistenceException {

        Certificate certificate = entityManager.find(Certificate.class, id);

        if (certificate == null || !certificate.getCivicRegistrationNumber().equals(civicRegistrationNumber)) {
            throw new PersistenceException(id, civicRegistrationNumber);
        }

        CertificateStateHistoryEntry historyEntry = new CertificateStateHistoryEntry(target, state, timestamp);

        certificate.addState(historyEntry);
    }

    @Override
    public void updateStatus(String id, CertificateState state, String target, LocalDateTime timestamp)
        throws PersistenceException {

        Certificate certificate = entityManager.find(Certificate.class, id);

        if (certificate == null) {
            throw new PersistenceException(id, null);
        }

        CertificateStateHistoryEntry historyEntry = new CertificateStateHistoryEntry(target, state, timestamp);

        certificate.addState(historyEntry);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method is executed in a new nested transaction. This ensures that any errors that might
     * occur while removing intygs doesn't affect the other changes that are done in the parent transaction.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeCertificatesDeletedByCareGiver(Personnummer civicRegistrationNumber) {
        List<Certificate> certificatesOfCitizen = findCertificate(civicRegistrationNumber, null, null, null, null);
        for (Certificate certificate : certificatesOfCitizen) {
            if (certificate.isDeletedByCareGiver()) {
                entityManager.remove(certificate);
                LOG.info("Removing intyg {} from database since it's flaged as deletedByCareGiver", certificate.getId());
            }
        }
    }

    @Override
    public List<Certificate> findTestCertificates(LocalDateTime from, LocalDateTime to) {
        final var criteriaBuilder = entityManager.getCriteriaBuilder();
        final var query = criteriaBuilder.createQuery(Certificate.class);
        final var queryRoot = query.from(Certificate.class);

        final var predicates = new ArrayList<Predicate>();

        predicates.add(
            criteriaBuilder.isTrue(
                queryRoot.get("testCertificate")
            )
        );

        if (from != null) {
            predicates.add(
                criteriaBuilder.greaterThanOrEqualTo(
                    queryRoot.get("signedDate"), from
                )
            );
        }

        if (to != null) {
            predicates.add(
                criteriaBuilder.lessThan(
                    queryRoot.get("signedDate"), to
                )
            );
        }

        query.where(predicates.toArray(new Predicate[predicates.size()]));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public void eraseTestCertificates(List<String> ids) {
        for (var id : ids) {
            try {
                final var certificate = getCertificate(null, id);
                entityManager.remove(certificate);
            } catch (PersistenceException ex) {
                LOG.warn(String.format("Couldn't find certificate with id %s when erasing test certificates", id), ex);
            }
        }
    }

    @Override
    @Transactional
    public int eraseCertificates(List<String> certificateIds, String careProviderId) {
        int erasedCertificatesCount = 0;
        for (final var certificateId : certificateIds) {
            final var certificate = entityManager.find(Certificate.class, certificateId);

            if (certificate == null) {
                LOG.error("Certificate with id {} from care provider {} was not found and could not be erased.", certificateId,
                    careProviderId);
                continue;
            }

            entityManager.remove(certificate);
            erasedCertificatesCount++;
            LOG.debug("Certificate with id {} from care provider {} was successfully erased.", certificateId, careProviderId);
        }

        return erasedCertificatesCount;
    }

    @Override
    public List<String> findCertificatesWithoutMetadata(int maxNumber) {
        String sql =
            "SELECT c.ID FROM CERTIFICATE c "
                + "WHERE c.ID NOT IN ("
                + "SELECT cm.CERTIFICATE_ID FROM CERTIFICATE_METADATA cm WHERE c.ID = cm.CERTIFICATE_ID "
                + "UNION ALL "
                + "SELECT pp.POPULATE_ID FROM POPULATE_PROCESSED pp WHERE c.ID = pp.POPULATE_ID AND pp.JOB_NAME = 'METADATA' "
                + "UNION ALL "
                + "SELECT pf.POPULATE_ID FROM POPULATE_FAILURES pf WHERE c.ID = pf.POPULATE_ID AND pf.JOB_NAME = 'METADATA' "
                + ") LIMIT " + maxNumber + ";";
        return (List<String>) entityManager.createNativeQuery(sql).getResultList();
    }

    @Override
    public List<CertificateType> getCertificateTypes() {
        var criteriaBuilder = entityManager.getCriteriaBuilder();
        var query = criteriaBuilder.createQuery(CertificateType.class);
        query.from(CertificateType.class);
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<Certificate> findCertificatesForPatient(String patientId) {
        final var criteriaBuilder = entityManager.getCriteriaBuilder();
        final var query = criteriaBuilder.createQuery(Certificate.class);
        final var queryRoot = query.from(Certificate.class);

        queryRoot.fetch("states", JoinType.LEFT);
        queryRoot.fetch("certificateMetaData", JoinType.INNER);
        queryRoot.fetch("originalCertificate", JoinType.INNER);

        final var predicates = new ArrayList<Predicate>();

        predicates.add(
            criteriaBuilder.isFalse(
                queryRoot.get("testCertificate")
            )
        );

        if (patientId != null) {
            predicates.add(
                criteriaBuilder.equal(
                    queryRoot.get("civicRegistrationNumber"), patientId
                )
            );
        }

        query.where(predicates.toArray(new Predicate[predicates.size()]));

        return filterDuplicates(entityManager.createQuery(query).getResultList());
    }

    private List<String> toLowerCase(List<String> list) {
        List<String> result = new ArrayList<>();
        for (String item : list) {
            result.add(item.toLowerCase());
        }
        return result;
    }

    private List<Certificate> filterDuplicates(List<Certificate> all) {
        Set<String> found = new HashSet<>();
        List<Certificate> filtered = new ArrayList<>(all.size()); // keep list sorted
        for (Certificate certificate : all) {
            if (!found.contains(certificate.getId())) {
                filtered.add(certificate);
                found.add(certificate.getId());
            }
        }
        return filtered;
    }
}

