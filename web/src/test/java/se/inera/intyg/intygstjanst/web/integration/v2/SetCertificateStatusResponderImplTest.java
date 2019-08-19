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
package se.inera.intyg.intygstjanst.web.integration.v2;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.intygstjanst.web.service.builder.RecipientBuilder;
import se.riv.clinicalprocess.healthcond.certificate.setCertificateStatus.v2.SetCertificateStatusResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.setCertificateStatus.v2.SetCertificateStatusResponseType;
import se.riv.clinicalprocess.healthcond.certificate.setCertificateStatus.v2.SetCertificateStatusType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Part;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Statuskod;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

@RunWith(MockitoJUnitRunner.class)
public class SetCertificateStatusResponderImplTest {

    private static final Recipient FKASSA =
        new RecipientBuilder()
            .setLogicalAddress("FKORG")
            .setName("Föräkringskassa")
            .setId("FKASSA")
            .setCertificateTypes("fk7263")
            .setActive(true)
            .setTrusted(true)
            .build();

    @Mock
    private CertificateService certificateService;

    @Mock
    private MonitoringLogService monitoringLogService;

    @Mock
    private RecipientService recipientService;

    @InjectMocks
    private SetCertificateStatusResponderInterface responder = new SetCertificateStatusResponderImpl();

    @Before
    public void setupRecipientService() throws RecipientUnknownException {
        when(recipientService.getRecipient(eq("FKASSA"))).thenReturn(FKASSA);
        when(recipientService.getRecipient(eq("part"))).thenThrow(new RecipientUnknownException("Unknown"));
    }

    @Test
    public void setCertificateStatusTest() throws Exception {
        final String intygId = "intygId";
        final LocalDateTime timestamp = LocalDateTime.now();
        SetCertificateStatusType request = createRequest(intygId, "FKASSA", "SENTTO", timestamp);
        SetCertificateStatusResponseType response = responder.setCertificateStatus(null, request);
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());

        verify(certificateService).setCertificateState(intygId, "FKASSA", CertificateState.SENT, timestamp);
        verify(monitoringLogService).logCertificateStatusChanged(intygId, "SENT");
    }

    @Test
    public void setCertificateStatusIllegalRecipientTest() throws Exception {
        final String intygId = "intygId";
        final LocalDateTime timestamp = LocalDateTime.now();
        SetCertificateStatusType request = createRequest(intygId, "part", "SENTTO", timestamp);
        SetCertificateStatusResponseType response = responder.setCertificateStatus(null, request);
        assertEquals(ResultCodeType.ERROR, response.getResult().getResultCode());

        verify(certificateService, never()).setCertificateState(anyString(), anyString(), any(CertificateState.class),
            any(LocalDateTime.class));
        verify(monitoringLogService, never()).logCertificateStatusChanged(anyString(), anyString());
    }

    @Test
    public void setCertificateStatusIllegalStatusTest() throws Exception {
        final String intygId = "intygId";
        final LocalDateTime timestamp = LocalDateTime.now();
        SetCertificateStatusType request = createRequest(intygId, "FKASSA", "SENT", timestamp);
        SetCertificateStatusResponseType response = responder.setCertificateStatus(null, request);
        assertEquals(ResultCodeType.ERROR, response.getResult().getResultCode());

        verify(certificateService, never()).setCertificateState(anyString(), anyString(), any(CertificateState.class),
            any(LocalDateTime.class));
        verify(monitoringLogService, never()).logCertificateStatusChanged(anyString(), anyString());
    }

    private SetCertificateStatusType createRequest(String intygId, String part, String status, LocalDateTime timestamp) {
        SetCertificateStatusType parameters = new SetCertificateStatusType();
        parameters.setIntygsId(new IntygId());
        parameters.getIntygsId().setExtension(intygId);
        parameters.setPart(new Part());
        parameters.getPart().setCode(part);
        parameters.setStatus(new Statuskod());
        parameters.getStatus().setCode(status);
        parameters.setTidpunkt(timestamp);
        return parameters;
    }
}
