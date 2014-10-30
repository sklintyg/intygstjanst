package se.inera.certificate.overvakning;

import java.sql.Time;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * RESTinterface for checking the general health status of the application
 * 
 * @author erik
 * 
 */
public class HealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheck.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    @Qualifier("jmsFactory")
    private ConnectionFactory connectionFactory;

    private static final String CURR_TIME_SQL = "SELECT CURRENT_TIME()";

    @GET
    @Path("/ping")
    @Produces(MediaType.APPLICATION_XML)
    public Response getPing() {
        boolean ok;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            ok = true;
        } catch (Exception e) {
            LOGGER.error("Ping failed with exception: " + e.getMessage());
            ok = false;
        }
        stopWatch.stop();
        String xmlResponse = buildXMLResponse(ok, stopWatch.getTime());
        LOGGER.info("pinged Intygstjänsten, got: " + xmlResponse);
        return Response.ok(xmlResponse).build();
    }

    @GET
    @Path("/checkDB")
    @Produces(MediaType.APPLICATION_XML)
    public Response checkDB() {
        boolean ok;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ok = checkTime();
        stopWatch.stop();
        String xmlResponse = buildXMLResponse(ok, stopWatch.getTime());
        LOGGER.info("checked DB connection in Intygstjänsten, got: " + xmlResponse);
        return Response.ok(xmlResponse).build();
    }

    @GET
    @Path("/checkJMS")
    @Produces(MediaType.APPLICATION_XML)
    public Response checkJMS() {
        boolean ok;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ok = checkJmsConnection();
        stopWatch.stop();
        String xmlResponse = buildXMLResponse(ok, stopWatch.getTime());
        LOGGER.info("checked JMS connection in Intygstjänsten, got: " + xmlResponse);
        return Response.ok(xmlResponse).build();
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

    private String buildXMLResponse(boolean ok, long time) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pingdom_http_custom_check>");
        sb.append("<status>" + (ok ? "OK" : "FAIL") + "</status>");
        sb.append("<response_time>" + time + "</response_time>");
        sb.append("</pingdom_http_custom_check>");
        return sb.toString();
    }

    private boolean checkTime() {
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
