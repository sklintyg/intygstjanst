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
package se.inera.intyg.intygstjanst.persistence.model.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import se.inera.intyg.intygstjanst.persistence.config.JpaConstans;
import se.inera.intyg.intygstjanst.persistence.model.dao.HealthCheckDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.sql.Time;

/**
 * Implementation of {@link HealthCheckDao}.
 */
@Repository
public class HealthCheckRepository implements HealthCheckDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckRepository.class);

    private static final String CURR_TIME_SQL = "SELECT CURRENT_TIME()";

    @PersistenceContext(unitName = JpaConstans.PERSISTANCE_UNIT_NAME)
    private EntityManager entityManager;

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

}