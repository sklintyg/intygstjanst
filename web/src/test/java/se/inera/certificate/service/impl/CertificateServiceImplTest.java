package se.inera.certificate.service.impl;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import se.inera.certificate.exception.CertificateRevokedException;
import se.inera.certificate.exception.InvalidCertificateException;
import se.inera.certificate.exception.MissingConsentException;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.integration.module.ModuleApiFactory;
import se.inera.certificate.integration.module.exception.ModuleNotFoundException;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.builder.CertificateBuilder;
import se.inera.certificate.model.common.MinimalUtlatande;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.model.dao.CertificateDao;
import se.inera.certificate.model.dao.CertificateStateHistoryEntry;
import se.inera.certificate.model.dao.OriginalCertificate;
import se.inera.certificate.modules.support.ModuleEntryPoint;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.ExternalModelResponse;
import se.inera.certificate.modules.support.api.dto.TransportModelHolder;
import se.inera.certificate.modules.support.api.exception.ModuleSystemException;
import se.inera.certificate.service.CertificateSenderService;
import se.inera.certificate.service.ConsentService;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateServiceImplTest {

    private static final String PERSONNUMMER = "<civicRegistrationNumber>";
    private static final String CERTIFICATE_ID = "<certificate-id>";

    @Mock
    private CertificateDao certificateDao;

    @Mock
    private ConsentService consentService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CertificateSenderService certificateSender;

    @Mock
    ModuleApiFactory moduleApiFactory;

    @Mock
    ModuleEntryPoint moduleEntryPoint;

    @Mock
    ModuleApi moduleApi;

    @InjectMocks
    private CertificateServiceImpl certificateService = new CertificateServiceImpl();

    @Test
    public void certificateWithDeletedStatusHasMetaDeleted() {
        Certificate certificate = createCertificate();
        certificate.addState(new CertificateStateHistoryEntry("", CertificateState.DELETED, new LocalDateTime(1)));
        when(consentService.isConsent(anyString())).thenReturn(Boolean.TRUE);
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);

        Certificate found = certificateService.getCertificate(PERSONNUMMER, CERTIFICATE_ID);

        assertTrue(found.getDeleted());

        verify(certificateDao).getCertificate(PERSONNUMMER, CERTIFICATE_ID);
    }

    private Certificate createCertificate() {
        Certificate certificate = new Certificate(CERTIFICATE_ID, "document");
        certificate.setCivicRegistrationNumber(PERSONNUMMER);
        return certificate;
    }

    @Test
    public void certificateWithStatusRestoredNewerThanDeletedHasMetaNotDeleted() {
        Certificate certificate = createCertificate();
        certificate.addState(new CertificateStateHistoryEntry("", CertificateState.RESTORED, new LocalDateTime(2)));
        certificate.addState(new CertificateStateHistoryEntry("", CertificateState.DELETED, new LocalDateTime(1)));
        when(consentService.isConsent(anyString())).thenReturn(Boolean.TRUE);
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);

        Certificate found = certificateService.getCertificate(PERSONNUMMER, CERTIFICATE_ID);
        assertFalse(found.getDeleted());

        verify(certificateDao).getCertificate(PERSONNUMMER, CERTIFICATE_ID);
    }

    @Test
    public void certificateWithStatusDeletedNewerThanRestoredHasMetaDeleted() {
        Certificate certificate = createCertificate();
        certificate.addState(new CertificateStateHistoryEntry("", CertificateState.DELETED, new LocalDateTime(2)));
        certificate.addState(new CertificateStateHistoryEntry("", CertificateState.RESTORED, new LocalDateTime(1)));
        when(consentService.isConsent(anyString())).thenReturn(Boolean.TRUE);
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);

        Certificate found = certificateService.getCertificate(PERSONNUMMER, CERTIFICATE_ID);
        assertTrue(found.getDeleted());

        verify(certificateDao).getCertificate(PERSONNUMMER, CERTIFICATE_ID);
    }

    private String utlatandeXml() throws IOException {
        return FileUtils.readFileToString(new ClassPathResource("CertificateServiceImplTest/fk7263.xml").getFile());
    }

    private String utlatandeJson() throws IOException {
        return FileUtils.readFileToString(new ClassPathResource(
                "CertificateServiceImplTest/lakarutlatande_external_format.json").getFile());
    }

    private MinimalUtlatande utlatande() throws IOException {
        return new CustomObjectMapper().readValue(new ClassPathResource(
                "CertificateServiceImplTest/lakarutlatande_external_format.json").getFile(), MinimalUtlatande.class);
    }

    @Test
    public void testStoreCertificateHappyCase() throws Exception {

        when(moduleApiFactory.getModuleEntryPoint("fk7263")).thenReturn(moduleEntryPoint);
        when(moduleEntryPoint.getModuleApi()).thenReturn(moduleApi);
        ExternalModelResponse unmarshallResponse = new ExternalModelResponse(utlatandeJson(), utlatande());
        when(moduleApi.unmarshall(any(TransportModelHolder.class))).thenReturn(unmarshallResponse);

        when(objectMapper.readValue(utlatandeJson(), MinimalUtlatande.class)).thenReturn(utlatande());

        ArgumentCaptor<OriginalCertificate> originalCertificateCaptor = ArgumentCaptor
                .forClass(OriginalCertificate.class);
        when(certificateDao.storeOriginalCertificate(originalCertificateCaptor.capture())).thenReturn(1L);

        Certificate certificate = certificateService.storeCertificate(utlatandeXml(), "fk7263");

        assertEquals("1", certificate.getId());
        assertEquals("fk7263", certificate.getType());
        assertNotNull(certificate.getDocument());
        assertEquals("Hans Rosling", certificate.getSigningDoctorName());
        assertEquals("vardenhets-id", certificate.getCareUnitId());
        assertEquals("Vårdcentrum i väst", certificate.getCareUnitName());
        assertEquals("19001122-3344", certificate.getCivicRegistrationNumber());
        assertEquals(new LocalDateTime("2013-05-31T09:51:38.570"), certificate.getSignedDate());
        assertEquals("2013-06-01", certificate.getValidFromDate());
        assertEquals("2013-06-12", certificate.getValidToDate());

        assertEquals(utlatandeJson(), certificate.getDocument());

        assertEquals(1, certificate.getStates().size());
        assertEquals(CertificateState.RECEIVED, certificate.getStates().get(0).getState());
        assertEquals("MI", certificate.getStates().get(0).getTarget());

        LocalDateTime aMinuteAgo = new LocalDateTime().minusMinutes(1);
        LocalDateTime inAMinute = new LocalDateTime().plusMinutes(1);
        assertTrue(certificate.getStates().get(0).getTimestamp().isAfter(aMinuteAgo));
        assertTrue(certificate.getStates().get(0).getTimestamp().isBefore(inAMinute));

        verify(certificateDao).store(certificate);

        OriginalCertificate originalCertificate = originalCertificateCaptor.getValue();
        assertEquals(certificate, originalCertificate.getCertificate());
        assertEquals(utlatandeXml(), originalCertificate.getDocument());
        assertTrue(originalCertificate.getReceived().isAfter(aMinuteAgo));
        assertTrue(originalCertificate.getReceived().isBefore(inAMinute));
    }

    @Test
    public void testModuleNotFound() throws Exception {

        when(moduleApiFactory.getModuleEntryPoint("fk7263")).thenThrow(new ModuleNotFoundException());

        try {
            certificateService.storeCertificate(utlatandeJson(), "fk7263");
            fail("Expected RuntimeException");
        } catch (RuntimeException ignore) {
        }
    }

    @Test
    public void testModuleError() throws Exception {

        when(moduleApiFactory.getModuleEntryPoint("fk7263")).thenReturn(moduleEntryPoint);
        when(moduleEntryPoint.getModuleApi()).thenReturn(moduleApi);
        when(moduleApi.unmarshall(any(TransportModelHolder.class))).thenThrow(new ModuleSystemException());

        try {
            certificateService.storeCertificate(utlatandeJson(), "fk7263");
            fail("Expected RuntimeException");
        } catch (RuntimeException ignore) {
        }
    }

    @Test
    public void sendCertificateCallsSenderAndSetsStatus() throws IOException {

        Certificate certificate = new CertificateBuilder(CERTIFICATE_ID).civicRegistrationNumber(PERSONNUMMER).build();

        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);

        certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "fk");

        verify(certificateDao).getCertificate(PERSONNUMMER, CERTIFICATE_ID);
        verify(certificateDao).updateStatus(CERTIFICATE_ID, PERSONNUMMER, CertificateState.SENT, "fk", null);
        verify(certificateSender).sendCertificate(certificate, "fk");
    }

    @Test(expected = InvalidCertificateException.class)
    public void testSendCertificateWitUnknownCertificate() {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(null);

        certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "fk");
    }

    @Test(expected = CertificateRevokedException.class)
    public void testSendRevokedCertificate() {
        Certificate revokedCertificate = new CertificateBuilder(CERTIFICATE_ID).state(CertificateState.CANCELLED, null)
                .build();

        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(revokedCertificate);

        certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "fk");
    }

    @Test(expected = MissingConsentException.class)
    public void testGetCertificateWithoutConsent() {
        when(consentService.isConsent(PERSONNUMMER)).thenReturn(false);
        certificateService.getCertificate(PERSONNUMMER, CERTIFICATE_ID);
    }
    
    @Test(expected = InvalidCertificateException.class)
    public void testGetCertificateNotFound() {
        when(certificateDao.getCertificate(PERSONNUMMER,CERTIFICATE_ID)).thenReturn(null);
        certificateService.getCertificate(CERTIFICATE_ID);
    }
    
    @Test(expected = InvalidCertificateException.class)
    public void testGetCertificateRevoked() {
        Certificate revokedCertificate = new CertificateBuilder(CERTIFICATE_ID).state(CertificateState.CANCELLED, null)
                .build();
        when(certificateDao.getCertificate(PERSONNUMMER,CERTIFICATE_ID)).thenReturn(revokedCertificate);
        certificateService.getCertificate(CERTIFICATE_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCertificateWithoutConsentCheckForEmptyPersonnummer() {
        Certificate certificate = createCertificate();
        when(certificateDao.getCertificate(null, CERTIFICATE_ID)).thenReturn(certificate);

        certificateService.getCertificate(null, CERTIFICATE_ID);
    }
}
