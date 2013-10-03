/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Certificate (http://code.google.com/p/inera-certificate).
 *
 * Inera Certificate is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Certificate is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.certificate.model.dao.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import se.inera.certificate.exception.InvalidCertificateIdentifierException;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.model.dao.CertificateDao;
import se.inera.certificate.model.dao.CertificateStateHistoryEntry;
import se.inera.certificate.model.dao.OriginalCertificate;

/**
 * Implementation of {@link CertificateDao}.
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class CertificateDaoImpl implements CertificateDao {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateDaoImpl.class);

    /** Injected EntityManager object. */
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Certificate> findCertificate(String civicRegistrationNumber, List<String> types, LocalDate fromDate, LocalDate toDate) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Certificate> query = criteriaBuilder.createQuery(Certificate.class);
        Root<Certificate> root = query.from(Certificate.class);

        root.fetch("states", JoinType.LEFT);

        if (civicRegistrationNumber == null) {
            return Collections.emptyList();
        }

        List<Predicate> predicates = new ArrayList<>();

        // meta data has to match civic registration number
        predicates.add(criteriaBuilder.equal(root.get("civicRegistrationNumber"), civicRegistrationNumber));

        // filter by certificate types
        if (types != null && !types.isEmpty()) {
            predicates.add(criteriaBuilder.lower(root.<String>get("type")).in(toLowerCase(types)));
        }

        query.where(predicates.toArray(new Predicate[predicates.size()]));

        // order by signed date
        query.orderBy(criteriaBuilder.asc(root.get("signedDate")));

        List<Certificate> tmpResult = entityManager.createQuery(query).getResultList();
        List<Certificate> result = filterDuplicates(tmpResult);

        // expect a small number, so lets filter in memory
        return new DateFilter(result).filter(fromDate, toDate);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, noRollbackFor = { InvalidCertificateIdentifierException.class })
    public Certificate getCertificate(String civicRegistrationNumber, String certificateId) {
        Certificate certificate = entityManager.find(Certificate.class, certificateId);

        if (certificate == null) {
            return null;
        }

        // if provided, the civic registration number has to match the certificate's civic registration number
        if (civicRegistrationNumber != null && !certificate.getCivicRegistrationNumber().equals(civicRegistrationNumber)) {

            LOG.warn(String.format("Trying to access certificate '%s' for user '%s' but certificate's user is '%s'.",
                    certificateId, civicRegistrationNumber, certificate.getCivicRegistrationNumber()));
            throw new InvalidCertificateIdentifierException(certificateId, civicRegistrationNumber);
        }

        return certificate;
    }

    @Override
    public void store(Certificate certificate) {
        entityManager.persist(certificate);
    }

    @Override
    public void storeOriginalCertificate(OriginalCertificate originalCertificate) {
        entityManager.persist(originalCertificate);
    }

    @Override
    @Transactional(noRollbackFor = { InvalidCertificateIdentifierException.class })
    public void updateStatus(String id, String civicRegistrationNumber, CertificateState state, String target, LocalDateTime timestamp) {

        Certificate certificate = entityManager.find(Certificate.class, id);

        if (certificate == null || !certificate.getCivicRegistrationNumber().equals(civicRegistrationNumber)) {
            throw new InvalidCertificateIdentifierException(id, civicRegistrationNumber);
        }

        CertificateStateHistoryEntry historyEntry = new CertificateStateHistoryEntry(target, state, timestamp);

        certificate.addState(historyEntry);
    }

    private List<String> toLowerCase(List<String> list) {
        List<String> result = new ArrayList<>();
        for (String item: list) {
            result.add(item.toLowerCase());
        }
        return result;
    }
    
    private List<Certificate> filterDuplicates(List<Certificate> all) {
        Set<String> found = new HashSet<>();
        List<Certificate> filtered = new ArrayList<>(all.size());
        for (Certificate certificate: all) {
            if (!found.contains(certificate.getId())) {
                filtered.add(certificate);
                found.add(certificate.getId());
            }
        }
        return filtered;
    }
}
