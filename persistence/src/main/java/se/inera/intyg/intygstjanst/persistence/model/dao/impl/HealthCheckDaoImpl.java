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

import java.sql.Time;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.persistence.model.dao.HealthCheckDao;

/**
 * Implementation of {@link HealthCheckDao}.
 */
@Repository
public class HealthCheckDaoImpl implements HealthCheckDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckDaoImpl.class);

    private static final Integer WINDOW_SIZE = 5; // Minutes
    private static final String RECEIVED_ALIAS = "ReceivedCertificates";
    private static final String SENT_ALIAS = "SentCertificates";
    private static final String CURR_TIME_SQL = "SELECT CURRENT_TIME()";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public CertificateStatsInTimeWindow getNoOfSentAndReceivedCertsInTimeWindow() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Certificate> cert = query.from(Certificate.class);
        Join<Certificate, CertificateStateHistoryEntry> states = cert.join("states");
        LocalDateTime now = LocalDateTime.now();
        query.where(
                cb.and(cb.between(states.get("timestamp"), now.minusMinutes(WINDOW_SIZE), now),
                        cb.or(cb.equal(states.get("state"), CertificateState.RECEIVED),
                                cb.equal(states.get("state"), CertificateState.SENT))));
        query.multiselect(cb.sum(cb.<Long> selectCase().when(cb.equal(states.get("state"), CertificateState.RECEIVED), 1L).otherwise(0L))
                .alias(RECEIVED_ALIAS),
                cb.sum(cb.<Long> selectCase().when(cb.equal(states.get("state"), CertificateState.SENT), 1L).otherwise(0L))
                        .alias(SENT_ALIAS));
        Tuple result = entityManager.createQuery(query).getSingleResult();
        Long receivedCerts = result.get(RECEIVED_ALIAS, Long.class) != null ? result.get(RECEIVED_ALIAS, Long.class) : 0L;
        Long sentCerts = result.get(SENT_ALIAS, Long.class) != null ? result.get(SENT_ALIAS, Long.class) : 0L;
        return new CertificateStatsInTimeWindowImpl(receivedCerts, sentCerts);
    }

    @Override
    public boolean checkTimeFromDb() {
        Time timestamp;
        try {
            Query query = entityManager.createNativeQuery(CURR_TIME_SQL);
            timestamp = (Time) query.getSingleResult();
        } catch (Exception e) {
            LOGGER.error("checkTimeFromDb failed with exception: " + e.getMessage());
            return false;
        }
        return timestamp != null;

    }

    private static final class CertificateStatsInTimeWindowImpl implements CertificateStatsInTimeWindow {

        private Long received;
        private Long sent;

        CertificateStatsInTimeWindowImpl(Long received, Long sent) {
            this.received = received;
            this.sent = sent;
        }

        @Override
        public Long getNoOfReceived() {
            return received;
        }

        @Override
        public Long getNoOfSent() {
            return sent;
        }
    }
}