/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.intygstjanst.persistence.model.dao.HealthCheckDao;
import se.inera.intyg.intygstjanst.web.service.HealthCheckService;
import se.inera.intyg.intygstjanst.web.service.impl.HealthCheckServiceImpl;
import se.inera.intyg.intygstjanst.web.service.impl.HealthCheckServiceImpl.Status;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckControllerTest {

    @Mock
    private HealthCheckDao healthCheckDao;

    @InjectMocks
    private HealthCheckServiceImpl statusCreator;

    @Mock
    private HealthCheckService healthCheck;

    @InjectMocks
    private HealthCheckController controller;

    @Test
    public void testGetPing() {
        Response res = controller.getPing();

        assertNotNull(res);
        assertEquals(200, res.getStatus());
        String entity = (String) res.getEntity();
        assertTrue(entity.startsWith("<pingdom_http_custom_check>"));
        assertTrue(entity.contains("<status>OK</status>"));
        assertTrue(entity.contains("<response_time>"));
        assertTrue(entity.endsWith("</pingdom_http_custom_check>"));
        assertFalse(entity.contains("<additional_data>"));

        verifyZeroInteractions(healthCheck);
    }

    @Test
    public void testCheckDB() {
        when(healthCheck.getDbStatus()).thenReturn(okStatus());
        Response res = controller.checkDB();

        assertNotNull(res);
        assertEquals(200, res.getStatus());
        String entity = (String) res.getEntity();
        assertTrue(entity.startsWith("<pingdom_http_custom_check>"));
        assertTrue(entity.contains("<status>OK</status>"));
        assertTrue(entity.contains("<response_time>"));
        assertTrue(entity.endsWith("</pingdom_http_custom_check>"));
    }

    @Test
    public void testCheckDBFail() {
        Status nonOk = nonOkStatus();
        when(healthCheck.getDbStatus()).thenReturn(nonOk);
        Response res = controller.checkDB();

        assertNotNull(res);
        assertEquals(200, res.getStatus());
        String entity = (String) res.getEntity();
        assertTrue(entity.startsWith("<pingdom_http_custom_check>"));
        assertTrue(entity.contains("<status>FAIL</status>"));
        assertTrue(entity.contains("<response_time>"));
        assertTrue(entity.endsWith("</pingdom_http_custom_check>"));
    }

    @Test
    public void testCheckJMS() {
        when(healthCheck.getJMSStatus()).thenReturn(okStatus());
        Response res = controller.checkJMS();

        assertNotNull(res);
        assertEquals(200, res.getStatus());
        String entity = (String) res.getEntity();
        assertTrue(entity.startsWith("<pingdom_http_custom_check>"));
        assertTrue(entity.contains("<status>OK</status>"));
        assertTrue(entity.contains("<response_time>"));
        assertTrue(entity.endsWith("</pingdom_http_custom_check>"));
    }

    @Test
    public void testCheckJMSFail() {
        Status nonOk = nonOkStatus();
        when(healthCheck.getJMSStatus()).thenReturn(nonOk);
        Response res = controller.checkJMS();

        assertNotNull(res);
        assertEquals(200, res.getStatus());
        String entity = (String) res.getEntity();
        assertTrue(entity.startsWith("<pingdom_http_custom_check>"));
        assertTrue(entity.contains("<status>FAIL</status>"));
        assertTrue(entity.contains("<response_time>"));
        assertTrue(entity.endsWith("</pingdom_http_custom_check>"));
    }

    @Test
    public void testCheckUptime() {
        when(healthCheck.getUptime()).thenReturn(okStatus());
        Response res = controller.checkUptime();

        assertNotNull(res);
        assertEquals(200, res.getStatus());
        String entity = (String) res.getEntity();
        assertTrue(entity.startsWith("<pingdom_http_custom_check>"));
        assertTrue(entity.contains("<status>OK</status>"));
        assertTrue(entity.contains("<response_time>"));
        assertTrue(entity.endsWith("</pingdom_http_custom_check>"));
    }

    @Test
    public void testCheckUptimeFail() {
        Status nonOk = nonOkStatus();
        when(healthCheck.getUptime()).thenReturn(nonOk);
        Response res = controller.checkUptime();

        assertNotNull(res);
        assertEquals(200, res.getStatus());
        String entity = (String) res.getEntity();
        assertTrue(entity.startsWith("<pingdom_http_custom_check>"));
        assertTrue(entity.contains("<status>FAIL</status>"));
        assertTrue(entity.contains("<response_time>"));
        assertTrue(entity.endsWith("</pingdom_http_custom_check>"));
    }

    private Status okStatus() {
        return statusCreator.getUptime();
    }

    private Status nonOkStatus() {
        when(healthCheckDao.checkTimeFromDb()).thenReturn(false);
        return statusCreator.getDbStatus();
    }
}
