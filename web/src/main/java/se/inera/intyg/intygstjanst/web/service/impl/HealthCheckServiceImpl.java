package se.inera.intyg.intygstjanst.web.service.impl;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.HealthCheckService;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.sql.Time;

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

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    @Qualifier("jmsFactory")
    private ConnectionFactory connectionFactory;

    private static final String CURR_TIME_SQL = "SELECT CURRENT_TIME()";

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
        ok = checkTimeFromDb();
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

    private boolean checkTimeFromDb() {
        Time timestamp;
        try {
            Query query = entityManager.createNativeQuery(CURR_TIME_SQL);
            timestamp = (Time) query.getSingleResult();
        } catch (Exception e) {
            LOGGER.error("checkDB failed with exception: " + e.getMessage());
            return false;
        }
        return timestamp != null;

    }

    private void logStatus(String operation, Status status) {
        String result = status.isOk() ? "OK" : "FAIL";
        LOGGER.info("Operation {} completed with result {} in {} ms", operation, result, status.getMeasurement());
    }

    private Status createStatus(boolean ok, StopWatch stopWatch) {
        return new Status(stopWatch.getTime(), ok);
    }

    public final class Status {
        private final long measurement;
        private final boolean ok;

        private Status(long measurement, boolean ok) {
            this.measurement = measurement;
            this.ok = ok;
        }

        public boolean isOk() {
            return ok;
        }

        public long getMeasurement() {
            return measurement;
        }
    }
}
