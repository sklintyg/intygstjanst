/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.SjukfallCertificateService;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.intygstjanst.web.service.builder.RecipientBuilder;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

@RunWith(MockitoJUnitRunner.class)
public class RevokeCertificateResponderImplTest {

    @Mock
    private MonitoringLogService monitoringService;

    @Mock
    private RevokeCertificateResponderInterface revokeInterface;

    @Mock
    private CertificateService certificateService;

    @Mock
    private StatisticsService statisticsService;

    @Mock
    private SjukfallCertificateService sjukfallCertificateService;

    @Mock
    private RecipientService recipientService;

    @InjectMocks
    private RevokeCertificateResponderImpl revokeCertificateResponder;

    @Test
    public void testRevokeCertificate() throws Exception {
        final String certificateId = "certificateId";
        final String patientId = "19121212-1212";
        final String logicalAddress = "logicalAddress";

        final Recipient recipient = new RecipientBuilder()
            .setLogicalAddress(logicalAddress)
            .setName("name")
            .setId("id")
            .setCertificateTypes("types")
            .setActive(true)
            .setTrusted(true)
            .build();

        final Certificate certificate = createCertificate(
            certificateId,
            new CertificateStateHistoryEntry("target1", CertificateState.SENT, null),
            new CertificateStateHistoryEntry("target2", CertificateState.SENT, null),
            new CertificateStateHistoryEntry("target1", CertificateState.SENT, null));

        when(certificateService.revokeCertificate(any(), eq(certificateId))).thenReturn(certificate);
        when(recipientService.getRecipient(anyString())).thenReturn(recipient);

        RevokeCertificateType request = new RevokeCertificateType();
        request.setIntygsId(new IntygId());
        request.getIntygsId().setExtension(certificateId);
        request.setPatientPersonId(new PersonId());
        request.getPatientPersonId().setExtension(patientId);

        RevokeCertificateResponseType resp = revokeCertificateResponder.revokeCertificate("initial logical address", request);

        assertEquals(ResultCodeType.OK, resp.getResult().getResultCode());
        verify(certificateService, times(1)).revokeCertificate(any(), eq(certificateId));
        verify(certificateService, times(1)).revokeCertificateForStatistics(any());
        verify(sjukfallCertificateService, times(1)).revoked(any());
        verify(monitoringService, times(1)).logCertificateRevoked(eq(certificateId), or(isNull(), anyString()), any());
        verify(revokeInterface, times(2)).revokeCertificate(eq(logicalAddress), any());
    }

    @Test
    public void testRevokeCertificateNotSent() throws Exception {
        final String certificateId = "certificateId";
        final String patientId = "19121212-1212";
        final String logicalAddress = "logicalAddress";

        when(certificateService.revokeCertificate(any(), eq(certificateId))).thenReturn(createCertificate(certificateId));

        RevokeCertificateType request = new RevokeCertificateType();
        request.setIntygsId(new IntygId());
        request.getIntygsId().setExtension(certificateId);
        request.setPatientPersonId(new PersonId());
        request.getPatientPersonId().setExtension(patientId);

        RevokeCertificateResponseType resp = revokeCertificateResponder.revokeCertificate("initial logical address", request);

        assertEquals(ResultCodeType.OK, resp.getResult().getResultCode());
        verify(certificateService, times(1)).revokeCertificate(any(), eq(certificateId));
        verify(certificateService, times(1)).revokeCertificateForStatistics(any());
        verify(sjukfallCertificateService, times(1)).revoked(any());
        verify(monitoringService, times(1)).logCertificateRevoked(eq(certificateId), or(isNull(), anyString()), any());
        verify(revokeInterface, times(0)).revokeCertificate(eq(logicalAddress), any());
    }

    @Test
    public void testRevokeCertificateNotExisting() throws Exception {
        final String certificateId = "certificateId";
        final String patientId = "19121212-1212";
        final String logicalAddress = "logicalAddress";

        when(certificateService.revokeCertificate(any(), eq(certificateId)))
            .thenThrow(new InvalidCertificateException(certificateId, createPnr(patientId)));

        RevokeCertificateType request = new RevokeCertificateType();
        request.setIntygsId(new IntygId());
        request.getIntygsId().setExtension(certificateId);
        request.setPatientPersonId(new PersonId());
        request.getPatientPersonId().setExtension(patientId);

        RevokeCertificateResponseType resp = revokeCertificateResponder.revokeCertificate("initial logical address", request);

        assertEquals(ResultCodeType.ERROR, resp.getResult().getResultCode());
        assertEquals(ErrorIdType.APPLICATION_ERROR, resp.getResult().getErrorId());
        verify(certificateService, times(1)).revokeCertificate(any(), eq(certificateId));
        verify(certificateService, times(0)).revokeCertificateForStatistics(any());
        verify(sjukfallCertificateService, times(0)).revoked(any());
        verify(monitoringService, times(0)).logCertificateRevoked(eq(certificateId), or(isNull(), anyString()), any());
        verify(revokeInterface, times(0)).revokeCertificate(eq(logicalAddress), any());
    }

    @Test
    public void testRevokeCertificateAlreadyRevoked() throws Exception {
        final String certificateId = "certificateId";
        final String patientId = "19121212-1212";
        final String logicalAddress = "logicalAddress";

        when(certificateService.revokeCertificate(any(), eq(certificateId)))
            .thenThrow(new CertificateRevokedException(certificateId));

        RevokeCertificateType request = new RevokeCertificateType();
        request.setIntygsId(new IntygId());
        request.getIntygsId().setExtension(certificateId);
        request.setPatientPersonId(new PersonId());
        request.getPatientPersonId().setExtension(patientId);

        RevokeCertificateResponseType resp = revokeCertificateResponder.revokeCertificate("initial logical address", request);

        assertEquals(ResultCodeType.INFO, resp.getResult().getResultCode());
        assertNotNull(resp.getResult().getResultText());
        assertNotEquals("", resp.getResult().getResultText());
        verify(certificateService, times(1)).revokeCertificate(any(), eq(certificateId));
        verify(certificateService, times(0)).revokeCertificateForStatistics(any());
        verify(sjukfallCertificateService, times(0)).revoked(any());
        verify(monitoringService, times(0)).logCertificateRevoked(eq(certificateId), anyString(), any());
        verify(revokeInterface, times(0)).revokeCertificate(eq(logicalAddress), any());
    }

    private Certificate createCertificate(String certificateId, CertificateStateHistoryEntry... entries) {
        Certificate cert = new Certificate(certificateId);
        cert.setStates(Arrays.asList(entries));
        return cert;
    }

    private Personnummer createPnr(String pnr) {
        return Personnummer.createPersonnummer(pnr)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer"));
    }

}
