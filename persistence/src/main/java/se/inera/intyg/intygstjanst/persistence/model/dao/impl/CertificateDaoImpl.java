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
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
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
    public List<Certificate> findCertificates(Personnummer civicRegistrationNumber, String careUnit,
        LocalDateTime fromDate, LocalDateTime toDate, String orderBy, boolean orderAscending) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Certificate> query = criteriaBuilder.createQuery(Certificate.class);
        Root<Certificate> root = query.from(Certificate.class);

        List<Predicate> predicates = new ArrayList<>();

        if (civicRegistrationNumber != null) {
            predicates
            .add(criteriaBuilder.equal(root.get("civicRegistrationNumber"), DaoUtil.formatPnrForPersistence(civicRegistrationNumber)));
        }

        if (careUnit != null && !careUnit.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("careUnitId"), careUnit));
        } else {
            return Collections.emptyList();
        }
        if (toDate != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("signedDate"), toDate));
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
                query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
            } else {
                query.orderBy(criteriaBuilder.desc(root.get(orderBy)));
            }
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
        for (var id: ids) {
            try {
                final var certificate = getCertificate(null, id);
                entityManager.remove(certificate);
            } catch (PersistenceException ex) {
                LOG.warn(String.format("Couldn't find certificate with id %s when erasing test certificates", id), ex);
            }
        }
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
