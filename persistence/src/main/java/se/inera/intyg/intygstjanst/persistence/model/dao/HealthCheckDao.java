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
package se.inera.intyg.intygstjanst.persistence.model.dao;

/**
 * Data Access Object for obtaining statistics and performance indicators.
 */
public interface HealthCheckDao {

    /**
     * Retrieves a {@link CertificateStatsInTimeWindow} representing the number of received and sent certificates in the
     * last 5 minutes.
     *
     * @return Number of received and sent certificates.
     */
    CertificateStatsInTimeWindow getNoOfSentAndReceivedCertsInTimeWindow();

    /**
     * Returns true if it was possible to retrieve the time from the DB.
     *
     * @return true if the DB connection succeeded.
     */
    boolean checkTimeFromDb();

    /**
     * Number of received and sent certificates.
     */
    interface CertificateStatsInTimeWindow {

        Long getNoOfReceived();

        Long getNoOfSent();
    }

}