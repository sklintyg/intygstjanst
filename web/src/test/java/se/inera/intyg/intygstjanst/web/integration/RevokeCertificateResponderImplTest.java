package se.inera.intyg.intygstjanst.web.integration;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.web.service.*;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v1.*;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v2.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;

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

        when(certificateService.revokeCertificate(any(), eq(certificateId)))
                .thenReturn(createCertificate(certificateId, new CertificateStateHistoryEntry("target1", CertificateState.SENT, null),
                        new CertificateStateHistoryEntry("target2", CertificateState.SENT, null),
                        new CertificateStateHistoryEntry("target1", CertificateState.SENT, null)));
        when(recipientService.getRecipient(anyString())).thenReturn(new Recipient(logicalAddress, "name", "id", "types"));

        RevokeCertificateType request = new RevokeCertificateType();
        request.setIntygsId(new IntygId());
        request.getIntygsId().setExtension(certificateId);
        request.setPatientPersonId(new PersonId());
        request.getPatientPersonId().setExtension(patientId);

        RevokeCertificateResponseType resp = revokeCertificateResponder.revokeCertificate("initial logical address", request);

        assertEquals(ResultCodeType.OK, resp.getResult().getResultCode());
        verify(certificateService, times(1)).revokeCertificate(any(), eq(certificateId));
        verify(statisticsService, times(1)).revoked(any());
        verify(sjukfallCertificateService, times(1)).revoked(any());
        verify(monitoringService, times(1)).logCertificateRevoked(eq(certificateId), anyString(), any());
        verify(revokeInterface, times(2)).revokeCertificate(eq(logicalAddress), any());
    }

    @Test
    public void testRevokeCertificateNotSent() throws Exception {
        final String certificateId = "certificateId";
        final String patientId = "19121212-1212";
        final String logicalAddress = "logicalAddress";

        when(certificateService.revokeCertificate(any(), eq(certificateId))).thenReturn(createCertificate(certificateId));
        when(recipientService.getRecipient(anyString())).thenReturn(new Recipient(logicalAddress, "name", "id", "types"));

        RevokeCertificateType request = new RevokeCertificateType();
        request.setIntygsId(new IntygId());
        request.getIntygsId().setExtension(certificateId);
        request.setPatientPersonId(new PersonId());
        request.getPatientPersonId().setExtension(patientId);

        RevokeCertificateResponseType resp = revokeCertificateResponder.revokeCertificate("initial logical address", request);

        assertEquals(ResultCodeType.OK, resp.getResult().getResultCode());
        verify(certificateService, times(1)).revokeCertificate(any(), eq(certificateId));
        verify(statisticsService, times(1)).revoked(any());
        verify(sjukfallCertificateService, times(1)).revoked(any());
        verify(monitoringService, times(1)).logCertificateRevoked(eq(certificateId), anyString(), any());
        verify(revokeInterface, times(0)).revokeCertificate(eq(logicalAddress), any());
    }

    @Test
    public void testRevokeCertificateNotExisting() throws Exception {
        final String certificateId = "certificateId";
        final String patientId = "19121212-1212";
        final String logicalAddress = "logicalAddress";

        when(certificateService.revokeCertificate(any(), eq(certificateId)))
                .thenThrow(new InvalidCertificateException(certificateId, new Personnummer(patientId)));

        RevokeCertificateType request = new RevokeCertificateType();
        request.setIntygsId(new IntygId());
        request.getIntygsId().setExtension(certificateId);
        request.setPatientPersonId(new PersonId());
        request.getPatientPersonId().setExtension(patientId);

        RevokeCertificateResponseType resp = revokeCertificateResponder.revokeCertificate("initial logical address", request);

        assertEquals(ResultCodeType.ERROR, resp.getResult().getResultCode());
        assertEquals(ErrorIdType.APPLICATION_ERROR, resp.getResult().getErrorId());
        verify(certificateService, times(1)).revokeCertificate(any(), eq(certificateId));
        verify(statisticsService, times(0)).revoked(any());
        verify(sjukfallCertificateService, times(0)).revoked(any());
        verify(monitoringService, times(0)).logCertificateRevoked(eq(certificateId), anyString(), any());
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
        verify(statisticsService, times(0)).revoked(any());
        verify(sjukfallCertificateService, times(0)).revoked(any());
        verify(monitoringService, times(0)).logCertificateRevoked(eq(certificateId), anyString(), any());
        verify(revokeInterface, times(0)).revokeCertificate(eq(logicalAddress), any());
    }

    private Certificate createCertificate(String certificateId, CertificateStateHistoryEntry... entries) {
        Certificate cert = new Certificate(certificateId, null);
        cert.setStates(Arrays.asList(entries));
        return cert;
    }

}
