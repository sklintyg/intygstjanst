/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.ifv.insuranceprocess.certificate.v1.StatusType;
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatus.rivtabp20.v1.SetCertificateStatusResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatusresponder.v1.SetCertificateStatusRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatusresponder.v1.SetCertificateStatusResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.logging.HashUtility;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateRecipientType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.schemas.contract.Personnummer;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class SetCertificateStatusResponderImplTest {

    private static final String CERTIFICATE_ID = "no5";
    private static final String RECIPIENT_FKASSA = "FKASSA";
    private static final String RECIPIENT_UNKNOWN = "UNKNOWN";

    @Mock
    private CertificateService certificateService;

    @Mock
    private MonitoringLogService monitoringLogService;

    @Mock
    private RecipientService recipientService;

    @Spy
    private HashUtility hashUtility;

    @InjectMocks
    private SetCertificateStatusResponderInterface responder = new SetCertificateStatusResponderImpl();

    @Before
    public void init() throws RecipientUnknownException {
        ReflectionTestUtils.setField(hashUtility, "salt", "salt");
        Recipient recipient = new Recipient("logicalAddress", "name", RECIPIENT_FKASSA, CertificateRecipientType.HUVUDMOTTAGARE.name(),
            "fk7263", true, true);
        when(recipientService.getPrimaryRecipientFkassa()).thenReturn(recipient);
        when(recipientService.getRecipient(RECIPIENT_FKASSA)).thenReturn(recipient);
        when(recipientService.getRecipient(RECIPIENT_UNKNOWN)).thenThrow(new RecipientUnknownException(""));
    }

    @Test
    public void testSetCertificateStatus() throws Exception {

        LocalDateTime timestamp = LocalDateTime.of(2013, 4, 26, 12, 0, 0);

        SetCertificateStatusRequestType request = new SetCertificateStatusRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        request.setNationalIdentityNumber("19001122-3344");
        request.setStatus(StatusType.CANCELLED);
        request.setTarget(RECIPIENT_FKASSA);
        request.setTimestamp(timestamp);

        SetCertificateStatusResponseType res = responder.setCertificateStatus(null, request);
        assertNotNull(res);
        assertEquals(ResultCodeEnum.OK, res.getResult().getResultCode());

        verify(certificateService).setCertificateState(createPnr("19001122-3344"), CERTIFICATE_ID, RECIPIENT_FKASSA,
            CertificateState.CANCELLED, timestamp);
        verify(monitoringLogService).logCertificateStatusChanged(CERTIFICATE_ID, "CANCELLED");
    }

    @Test
    public void testSetCertificateStatusLegacyTarget() throws Exception {

        LocalDateTime timestamp = LocalDateTime.of(2013, 4, 26, 12, 0, 0);

        SetCertificateStatusRequestType request = new SetCertificateStatusRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        request.setNationalIdentityNumber("19001122-3344");
        request.setStatus(StatusType.CANCELLED);
        request.setTarget("FK");
        request.setTimestamp(timestamp);

        SetCertificateStatusResponseType res = responder.setCertificateStatus(null, request);
        assertNotNull(res);
        assertEquals(ResultCodeEnum.OK, res.getResult().getResultCode());

        verify(certificateService).setCertificateState(createPnr("19001122-3344"), CERTIFICATE_ID, RECIPIENT_FKASSA,
            CertificateState.CANCELLED, timestamp);
        verify(monitoringLogService).logCertificateStatusChanged(CERTIFICATE_ID, "CANCELLED");
        verify(recipientService).getPrimaryRecipientFkassa();
    }

    @Test
    public void testSetCertificateStatusUnexpectedTarget() {
        LocalDateTime timestamp = LocalDateTime.of(2013, 4, 26, 12, 0, 0);

        SetCertificateStatusRequestType request = new SetCertificateStatusRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        request.setNationalIdentityNumber("19001122-3344");
        request.setStatus(StatusType.CANCELLED);
        request.setTarget(RECIPIENT_UNKNOWN);
        request.setTimestamp(timestamp);

        SetCertificateStatusResponseType res = responder.setCertificateStatus(null, request);

        assertNotNull(res);
        assertEquals(ResultCodeEnum.ERROR, res.getResult().getResultCode());

        verifyNoInteractions(certificateService);
        verifyNoInteractions(monitoringLogService);
    }

    @Test
    public void testSetCertificateStatusInvalidCertificate() throws Exception {
        final var timestamp = LocalDateTime.of(2013, 4, 26, 12, 0, 0);
        final var pnr = "19001122-3344";
        doThrow(new InvalidCertificateException(CERTIFICATE_ID, hashUtility.hash(createPnr(pnr).getPersonnummer())))
            .when(certificateService).setCertificateState(createPnr(pnr),
                CERTIFICATE_ID, RECIPIENT_FKASSA, CertificateState.CANCELLED, timestamp);

        SetCertificateStatusRequestType request = new SetCertificateStatusRequestType();
        request.setCertificateId(CERTIFICATE_ID);
        request.setNationalIdentityNumber(pnr);
        request.setStatus(StatusType.CANCELLED);
        request.setTarget(RECIPIENT_FKASSA);
        request.setTimestamp(timestamp);

        responder.setCertificateStatus(null, request);

        verify(certificateService).setCertificateState(createPnr(pnr), CERTIFICATE_ID, RECIPIENT_FKASSA,
            CertificateState.CANCELLED, timestamp);
        verify(monitoringLogService, never()).logCertificateStatusChanged(anyString(), anyString());
    }

    private Personnummer createPnr(String pnr) {
        return Personnummer.createPersonnummer(pnr)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer"));
    }

}
