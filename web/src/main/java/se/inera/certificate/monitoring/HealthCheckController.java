package se.inera.certificate.monitoring;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.service.HealthCheckService;
import se.inera.certificate.service.impl.HealthCheckServiceImpl.Status;

/**
 * RESTinterface for checking the general health status of the application
 * 
 * @author erik
 * 
 */
public class HealthCheckController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckController.class);
 
    @Autowired
    private HealthCheckService healthCheck;

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
        Status status = healthCheck.getDbStatus();
        String xmlResponse = buildXMLResponse(status);
        return Response.ok(xmlResponse).build();
    }

    @GET
    @Path("/checkJMS")
    @Produces(MediaType.APPLICATION_XML)
    public Response checkJMS() {
        Status status = healthCheck.getJMSStatus();
        String xmlResponse = buildXMLResponse(status);
        return Response.ok(xmlResponse).build();
    }

    private String buildXMLResponse(Status status) {
        return buildXMLResponse(status.isOk(), status.getMeasurement());
    }
    
    private String buildXMLResponse(boolean ok, long time) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pingdom_http_custom_check>");
        sb.append("<status>" + (ok ? "OK" : "FAIL") + "</status>");
        sb.append("<response_time>" + time + "</response_time>");
        sb.append("</pingdom_http_custom_check>");
        return sb.toString();
    }
}
