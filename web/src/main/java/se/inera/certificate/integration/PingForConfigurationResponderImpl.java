package se.inera.certificate.integration;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.jws.WebParam;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import se.inera.certificate.service.HealthCheckService;
import se.inera.certificate.service.impl.HealthCheckServiceImpl.Status;
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface;
import se.riv.itintegration.monitoring.v1.ConfigurationType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;

/**
 * @author johannesc
 */
public class PingForConfigurationResponderImpl implements PingForConfigurationResponderInterface {

    @Value("${project.version}")
    private String projectVersion;
    
    @Value("${buildNumber}")
    private String buildNumberString;

    @Value("${buildTime}")
    private String buildTimeString;
    
    @Autowired
    private HealthCheckService healthCheck;

    @Override
    public PingForConfigurationResponseType pingForConfiguration(@WebParam(partName = "LogicalAddress", name = "LogicalAddress", targetNamespace = "urn:riv:itintegration:registry:1", header = true) String logicalAddress, @WebParam(partName = "parameters", name = "PingForConfiguration", targetNamespace = "urn:riv:itintegration:monitoring:PingForConfigurationResponder:1") PingForConfigurationType parameters) {
        PingForConfigurationResponseType response = new PingForConfigurationResponseType();
        response.setPingDateTime(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        response.setVersion(projectVersion);

        Status db = healthCheck.getDbStatus();
        Status jms = healthCheck.getJMSStatus();
        Status uptime = healthCheck.getUptime();

        addConfiguration(response, "buildNumber", buildNumberString);
        addConfiguration(response, "buildTime", buildTimeString);
        addConfiguration(response, "systemUptime", DurationFormatUtils.formatDurationWords(uptime.getMeasurement(), true, true));
        addConfiguration(response, "dbStatus", db.isOk() ? "ok" : "error");
        addConfiguration(response, "jmsStatus", jms.isOk() ? "ok" : "error");
        
        return response;
    }

    private void addConfiguration(PingForConfigurationResponseType response, String name, String value) {
        ConfigurationType conf = new ConfigurationType();
        conf.setName(name);
        conf.setValue(value);
        response.getConfiguration().add(conf);
    }

    @PostConstruct
    public void init() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }
}
