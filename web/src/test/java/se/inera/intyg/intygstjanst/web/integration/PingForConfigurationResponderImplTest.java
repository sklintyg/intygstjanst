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
package se.inera.intyg.intygstjanst.web.integration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.intygstjanst.persistence.model.dao.HealthCheckDao;
import se.inera.intyg.intygstjanst.web.service.HealthCheckService;
import se.inera.intyg.intygstjanst.web.service.impl.HealthCheckServiceImpl;
import se.inera.intyg.intygstjanst.web.service.impl.HealthCheckServiceImpl.Status;
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;

@RunWith(MockitoJUnitRunner.class)
public class PingForConfigurationResponderImplTest {

    private static final String LOGICAL_ADDRESS = "logicalAddress";

    @Mock
    private HealthCheckDao healthCheckDao;

    @InjectMocks
    private HealthCheckServiceImpl statusCreator;

    @Mock
    private HealthCheckService healthCheck;

    @InjectMocks
    private PingForConfigurationResponderInterface responder = new PingForConfigurationResponderImpl();

    @Test
    public void pingForConfigurationTest() {
        when(healthCheck.getDbStatus()).thenReturn(okStatus());
        when(healthCheck.getUptime()).thenReturn(okStatus());
        when(healthCheck.getJMSStatus()).thenReturn(okStatus());
        PingForConfigurationResponseType res = responder.pingForConfiguration(LOGICAL_ADDRESS, new PingForConfigurationType());

        assertNotNull(res);
        assertNotNull(res.getPingDateTime());
        // verify format of pingdatetime
        assertNotNull(LocalDateTime.parse(res.getPingDateTime(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        assertNotNull(res.getConfiguration());

        assertTrue(res.getConfiguration().stream().filter(r -> "buildNumber".equals(r.getName())).findAny().isPresent());
        assertTrue(res.getConfiguration().stream().filter(r -> "buildTime".equals(r.getName())).findAny().isPresent());
        assertNotNull(res.getConfiguration().stream().filter(r -> "systemUptime".equals(r.getName())).findAny().get().getValue());
        assertEquals("ok", res.getConfiguration().stream().filter(r -> "dbStatus".equals(r.getName())).findAny().get().getValue());
        assertEquals("ok", res.getConfiguration().stream().filter(r -> "jmsStatus".equals(r.getName())).findAny().get().getValue());
        verify(healthCheck).getDbStatus();
        verify(healthCheck).getUptime();
        verify(healthCheck).getJMSStatus();
    }

    @Test
    public void pingForConfigurationDbNotOkTest() {
        Status nonOkStatus = nonOkStatus();
        when(healthCheck.getDbStatus()).thenReturn(nonOkStatus);
        when(healthCheck.getUptime()).thenReturn(okStatus());
        when(healthCheck.getJMSStatus()).thenReturn(okStatus());
        PingForConfigurationResponseType res = responder.pingForConfiguration(LOGICAL_ADDRESS, new PingForConfigurationType());

        assertNotNull(res);
        assertEquals("error", res.getConfiguration().stream().filter(r -> "dbStatus".equals(r.getName())).findAny().get().getValue());
        assertEquals("ok", res.getConfiguration().stream().filter(r -> "jmsStatus".equals(r.getName())).findAny().get().getValue());
        verify(healthCheck).getDbStatus();
        verify(healthCheck).getUptime();
        verify(healthCheck).getJMSStatus();
    }

    @Test
    public void pingForConfigurationJmsNotOkTest() {
        Status nonOkStatus = nonOkStatus();
        when(healthCheck.getDbStatus()).thenReturn(okStatus());
        when(healthCheck.getUptime()).thenReturn(okStatus());
        when(healthCheck.getJMSStatus()).thenReturn(nonOkStatus);
        PingForConfigurationResponseType res = responder.pingForConfiguration(LOGICAL_ADDRESS, new PingForConfigurationType());

        assertNotNull(res);
        assertEquals("ok", res.getConfiguration().stream().filter(r -> "dbStatus".equals(r.getName())).findAny().get().getValue());
        assertEquals("error", res.getConfiguration().stream().filter(r -> "jmsStatus".equals(r.getName())).findAny().get().getValue());
        verify(healthCheck).getDbStatus();
        verify(healthCheck).getUptime();
        verify(healthCheck).getJMSStatus();
    }

    private Status okStatus() {
        return statusCreator.getUptime();
    }

    private Status nonOkStatus() {
        when(healthCheckDao.checkTimeFromDb()).thenReturn(false);
        return statusCreator.getDbStatus();
    }
}
