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
package se.inera.intyg.intygstjanst.web.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.intygstjanst.persistence.model.dao.HealthCheckDao;
import se.inera.intyg.intygstjanst.web.service.impl.HealthCheckServiceImpl.Status;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckServiceImplTest {

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private HealthCheckDao healthCheckDao;

    @InjectMocks
    private HealthCheckServiceImpl service;

    @Test
    public void testGetDbStatus() {
        when(healthCheckDao.checkTimeFromDb()).thenReturn(true);
        Status res = service.getDbStatus();

        assertNotNull(res);
        assertTrue(res.isOk());
        assertNotNull(res.getMeasurement());
    }

    @Test
    public void testGetDbStatusFail() {
        when(healthCheckDao.checkTimeFromDb()).thenReturn(false);
        Status res = service.getDbStatus();

        assertNotNull(res);
        assertFalse(res.isOk());
        assertNotNull(res.getMeasurement());
    }

    @Test
    public void testGetJMSStatus() throws Exception {
        when(connectionFactory.createConnection()).thenReturn(mock(Connection.class));
        Status res = service.getJMSStatus();

        assertNotNull(res);
        assertTrue(res.isOk());
        assertNotNull(res.getMeasurement());
    }

    @Test
    public void testGetJMSStatusFail() throws Exception {
        when(connectionFactory.createConnection()).thenThrow(new JMSException(""));
        Status res = service.getJMSStatus();

        assertNotNull(res);
        assertFalse(res.isOk());
        assertNotNull(res.getMeasurement());
    }

    @Test
    public void testGetUptime() throws Exception {
        Status res = service.getUptime();

        assertNotNull(res);
        assertTrue(res.isOk());
        assertNotNull(res.getMeasurement());
    }
}
