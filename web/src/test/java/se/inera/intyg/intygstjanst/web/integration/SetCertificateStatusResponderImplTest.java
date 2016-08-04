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

package se.inera.intyg.intygstjanst.web.integration;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.ifv.insuranceprocess.certificate.v1.StatusType;
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatus.rivtabp20.v1.SetCertificateStatusResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatusresponder.v1.SetCertificateStatusRequestType;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class SetCertificateStatusResponderImplTest {

    private static final String CERTIFICATE_ID = "no5";

    @Mock
    private CertificateService certificateService;

    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private SetCertificateStatusResponderInterface responder = new SetCertificateStatusResponderImpl();

    @Test
    public void testSetCertificateStatus() throws Exception {

        LocalDateTime timestamp = new LocalDateTime(2013, 4, 26, 12, 0, 0);

        SetCertificateStatusRequestType request = new SetCertificateStatusRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        request.setNationalIdentityNumber("19001122-3344");
        request.setStatus(StatusType.CANCELLED);
        request.setTarget("försäkringskassan");
        request.setTimestamp(timestamp);

        responder.setCertificateStatus(null, request);

        verify(certificateService).setCertificateState(new Personnummer("19001122-3344"), CERTIFICATE_ID, "försäkringskassan", CertificateState.CANCELLED, timestamp);
        verify(monitoringLogService).logCertificateStatusChanged(CERTIFICATE_ID, "CANCELLED");
    }

    @Test
    public void testSetCertificateStatusInvalidCertificate() throws Exception {
        LocalDateTime timestamp = new LocalDateTime(2013, 4, 26, 12, 0, 0);
        doThrow(new InvalidCertificateException(CERTIFICATE_ID, new Personnummer("19001122-3344"))).when(certificateService).setCertificateState(new Personnummer("19001122-3344"), CERTIFICATE_ID, "försäkringskassan", CertificateState.CANCELLED, timestamp);

        SetCertificateStatusRequestType request = new SetCertificateStatusRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        request.setNationalIdentityNumber("19001122-3344");
        request.setStatus(StatusType.CANCELLED);
        request.setTarget("försäkringskassan");
        request.setTimestamp(timestamp);

        responder.setCertificateStatus(null, request);

        verify(certificateService).setCertificateState(new Personnummer("19001122-3344"), CERTIFICATE_ID, "försäkringskassan", CertificateState.CANCELLED, timestamp);
        verify(monitoringLogService, never()).logCertificateStatusChanged(anyString(), anyString());
    }
}
