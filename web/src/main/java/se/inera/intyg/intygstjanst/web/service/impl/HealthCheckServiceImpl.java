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

package se.inera.intyg.intygstjanst.web.service.impl;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import se.inera.intyg.intygstjanst.persistence.model.dao.HealthCheckDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.HealthCheckDao.CertificateStatsInTimeWindow;
import se.inera.intyg.intygstjanst.web.service.HealthCheckService;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for checking the general health status of the application.
 *
 * @author erik
 *
 */
@Service
public class HealthCheckServiceImpl implements HealthCheckService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckServiceImpl.class);

    private static final long SYSTEM_START_TIME = System.currentTimeMillis();

    @Autowired
    @Qualifier("jmsFactory")
    private ConnectionFactory connectionFactory;

    @Autowired
    private HealthCheckDao healthCheckDao;

    private static final String RECEIVED_PARAM_NAME = "ReceivedCertificates";
    private static final String SENT_PARAM_NAME = "SentCertificates";

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.intygstjanst.web.service.impl.HealthCheckService#getDbStatus()
     */
    @Override
    public Status getDbStatus() {
        boolean ok;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ok = healthCheckDao.checkTimeFromDb();
        stopWatch.stop();
        Status status = createStatus(ok, stopWatch);
        logStatus("getDbStatus", status);
        return status;
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.intygstjanst.web.service.impl.HealthCheckService#getJMSStatus()
     */
    @Override
    public Status getJMSStatus() {
        boolean ok;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ok = checkJmsConnection();
        stopWatch.stop();
        Status status = createStatus(ok, stopWatch);
        logStatus("getJMSStatus", status);
        return status;
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.intygstjanst.web.service.impl.HealthCheckService#getUptime()
     */
    @Override
    public Status getUptime() {
        long uptime = System.currentTimeMillis() - SYSTEM_START_TIME;
        LOGGER.info("Current system uptime is {}", DurationFormatUtils.formatDurationWords(uptime, true, true));
        return new Status(uptime, true);
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.intygstjanst.web.service.impl.HealthCheckService#getCertificateFlow()
     */
    @Override
    public Status getCertificateFlow() {
        boolean ok;
        Map<String, String> values = null;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            CertificateStatsInTimeWindow stats = healthCheckDao.getNoOfSentAndReceivedCertsInTimeWindow();
            values = new HashMap<String, String>();
            values.put(SENT_PARAM_NAME, String.valueOf(stats.getNoOfSent()));
            values.put(RECEIVED_PARAM_NAME, String.valueOf(stats.getNoOfReceived()));
            ok = true;
        } catch (Exception e) {
            LOGGER.error("getNoOfSentAndReceivedCertsInTimeWindow() failed with exception: " + e.getMessage());
            ok = false;
        }
        stopWatch.stop();
        Status status = createStatus(ok, stopWatch, values);
        logStatus("getCertificateFlow", status);
        return status;
    }

    private boolean checkJmsConnection() {
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            connection.close();
        } catch (JMSException e) {
            LOGGER.error("checkJMS failed with exception: " + e.getMessage());
            return false;
        }
        return true;
    }

    private void logStatus(String operation, Status status) {
        String result = status.isOk() ? "OK" : "FAIL";
        LOGGER.info("Operation {} completed with result {} in {} ms", operation, result, status.getMeasurement());
    }

    private Status createStatus(boolean ok, StopWatch stopWatch) {
        return new Status(stopWatch.getTime(), ok);
    }

    private Status createStatus(boolean ok, StopWatch stopWatch, Map<String, String> additionalValues) {
        return new Status(stopWatch.getTime(), ok, additionalValues);
    }

    public final class Status {
        private final long measurement;
        private final boolean ok;
        private Map<String, String> additionalValues = null;

        private Status(long measurement, boolean ok) {
            this.measurement = measurement;
            this.ok = ok;
        }

        private Status(long measurement, boolean ok, Map<String, String> additionalValues) {
            this(measurement, ok);
            this.additionalValues = additionalValues;
        }

        public boolean isOk() {
            return ok;
        }

        public long getMeasurement() {
            return measurement;
        }

        public Map<String, String> getAdditionalValues() {
            return additionalValues;
        }
    }
}