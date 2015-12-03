package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.web.service.impl.HealthCheckServiceImpl.Status;

/**
 * Service for checking the general health status of the application.
 *
 * @author erik
 */
public interface HealthCheckService {

    Status getDbStatus();

    Status getJMSStatus();

    Status getUptime();
}
