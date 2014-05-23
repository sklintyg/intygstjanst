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
import se.inera.certificate.exception.ClientException;
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
    public void certificateWithDeletedStatusHasMetaDeleted() throws ClientException {
        Certificate certificate = createCertificate();
        certificate.addState(new CertificateStateHistoryEntry("", CertificateState.DELETED, new LocalDateTime(1)));
        when(consentService.isConsent(anyString())).thenReturn(Boolean.TRUE);
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);

        Certificate found = certificateService.getCertificateForCitizen(PERSONNUMMER, CERTIFICATE_ID);

        assertTrue(found.getDeleted());

        verify(certificateDao).getCertificate(PERSONNUMMER, CERTIFICATE_ID);
    }

    private Certificate createCertificate() {
        Certificate certificate = new Certificate(CERTIFICATE_ID, "document");
        certificate.setCivicRegistrationNumber(PERSONNUMMER);
        return certificate;
    }

    @Test
    public void certificateWithStatusRestoredNewerThanDeletedHasMetaNotDeleted() throws ClientException {
        Certificate certificate = createCertificate();
        certificate.addState(new CertificateStateHistoryEntry("", CertificateState.RESTORED, new LocalDateTime(2)));
        certificate.addState(new CertificateStateHistoryEntry("", CertificateState.DELETED, new LocalDateTime(1)));
        when(consentService.isConsent(anyString())).thenReturn(Boolean.TRUE);
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);

        Certificate found = certificateService.getCertificateForCitizen(PERSONNUMMER, CERTIFICATE_ID);
        assertFalse(found.getDeleted());

        verify(certificateDao).getCertificate(PERSONNUMMER, CERTIFICATE_ID);
    }

    @Test
    public void certificateWithStatusDeletedNewerThanRestoredHasMetaDeleted() throws ClientException {
        Certificate certificate = createCertificate();
        certificate.addState(new CertificateStateHistoryEntry("", CertificateState.DELETED, new LocalDateTime(2)));
        certificate.addState(new CertificateStateHistoryEntry("", CertificateState.RESTORED, new LocalDateTime(1)));
        when(consentService.isConsent(anyString())).thenReturn(Boolean.TRUE);
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);

        Certificate found = certificateService.getCertificateForCitizen(PERSONNUMMER, CERTIFICATE_ID);
        assertTrue(found.getDeleted());

        verify(certificateDao).getCertificate(PERSONNUMMER, CERTIFICATE_ID);
    }

    private String utlatandeXml() throws IOException {
        return utlatandeXml("fk7263");
    }
    private String utlatandeXml(String name) throws IOException {
        return FileUtils.readFileToString(new ClassPathResource("CertificateServiceImplTest/" + name + ".xml").getFile());
    }

    private String utlatandeJson() throws IOException {
        return utlatandeJson("lakarutlatande_external_format");
    }

    private String utlatandeJson(String name) throws IOException {
        return FileUtils.readFileToString(new ClassPathResource(
                "CertificateServiceImplTest/" + name + ".json").getFile());
    }

    private MinimalUtlatande utlatande() throws IOException {
        return utlatande("lakarutlatande_external_format");
    }

    private MinimalUtlatande utlatande(String name) throws IOException {
        return new CustomObjectMapper().readValue(new ClassPathResource(
                "CertificateServiceImplTest/" + name + ".json").getFile(), MinimalUtlatande.class);
    }

    @Test
    public void testStoreCertificateHappyCase() throws Exception {
        String utlatandeJson = utlatandeJson();
        String utlatandeXml = utlatandeXml();
        MinimalUtlatande utlatande = utlatande();

        when(moduleApiFactory.getModuleEntryPoint("fk7263")).thenReturn(moduleEntryPoint);
        when(moduleEntryPoint.getModuleApi()).thenReturn(moduleApi);
        ExternalModelResponse unmarshallResponse = new ExternalModelResponse(utlatandeJson, utlatande);
        when(moduleApi.unmarshall(any(TransportModelHolder.class))).thenReturn(unmarshallResponse);

        when(objectMapper.readValue(utlatandeJson, MinimalUtlatande.class)).thenReturn(utlatande);

        ArgumentCaptor<OriginalCertificate> originalCertificateCaptor = ArgumentCaptor
                .forClass(OriginalCertificate.class);
        when(certificateDao.storeOriginalCertificate(originalCertificateCaptor.capture())).thenReturn(1L);

        Certificate certificate = certificateService.storeCertificate(utlatandeXml, "fk7263", false);

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

        assertEquals(utlatandeJson, certificate.getDocument());

        assertEquals(false, certificate.getWiretapped());
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
        assertEquals(utlatandeXml, originalCertificate.getDocument());
        assertTrue(originalCertificate.getReceived().isAfter(aMinuteAgo));
        assertTrue(originalCertificate.getReceived().isBefore(inAMinute));
    }

    @Test
    public void testStoreCertificateIdAsExtensionHappyCase() throws Exception {
        String utlatandeJson = utlatandeJson("ts-diabetes_external_format");
        String utlatandeXml = utlatandeXml("ts-diabetes");
        MinimalUtlatande utlatande = utlatande("ts-diabetes_external_format");

        when(moduleApiFactory.getModuleEntryPoint("ts-diabetes")).thenReturn(moduleEntryPoint);
        when(moduleEntryPoint.getModuleApi()).thenReturn(moduleApi);
        ExternalModelResponse unmarshallResponse = new ExternalModelResponse(utlatandeJson, utlatande);
        when(moduleApi.unmarshall(any(TransportModelHolder.class))).thenReturn(unmarshallResponse);

        ArgumentCaptor<OriginalCertificate> originalCertificateCaptor = ArgumentCaptor
                .forClass(OriginalCertificate.class);
        when(certificateDao.storeOriginalCertificate(originalCertificateCaptor.capture())).thenReturn(1L);

        Certificate certificate = certificateService.storeCertificate(utlatandeXml, "ts-diabetes", false);

        assertEquals("2", certificate.getId());
        assertEquals("ts-diabetes", certificate.getType());
        assertNotNull(certificate.getDocument());
        assertEquals("Hans Rosling", certificate.getSigningDoctorName());
        assertEquals("vardenhets-id", certificate.getCareUnitId());
        assertEquals("Vårdcentrum i väst", certificate.getCareUnitName());
        assertEquals("19001122-3344", certificate.getCivicRegistrationNumber());
        assertEquals(new LocalDateTime("2013-05-31T09:51:38.570"), certificate.getSignedDate());

        assertEquals(utlatandeJson, certificate.getDocument());

        assertEquals(false, certificate.getWiretapped());
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
        assertEquals(utlatandeXml, originalCertificate.getDocument());
        assertTrue(originalCertificate.getReceived().isAfter(aMinuteAgo));
        assertTrue(originalCertificate.getReceived().isBefore(inAMinute));
    }

    public void testStoreWireTappedCertificate() throws Exception {

        when(moduleApiFactory.getModuleEntryPoint("fk7263")).thenReturn(moduleEntryPoint);
        when(moduleEntryPoint.getModuleApi()).thenReturn(moduleApi);
        ExternalModelResponse unmarshallResponse = new ExternalModelResponse(utlatandeJson(), utlatande());
        when(moduleApi.unmarshall(any(TransportModelHolder.class))).thenReturn(unmarshallResponse);

        when(objectMapper.readValue(utlatandeJson(), MinimalUtlatande.class)).thenReturn(utlatande());

        ArgumentCaptor<OriginalCertificate> originalCertificateCaptor = ArgumentCaptor
                .forClass(OriginalCertificate.class);
        when(certificateDao.storeOriginalCertificate(originalCertificateCaptor.capture())).thenReturn(1L);

        when(certificateDao.storeOriginalCertificate(any(OriginalCertificate.class))).thenReturn(1L);

        Certificate certificate = certificateService.storeCertificate(utlatandeXml(), "fk7263", true);

        assertEquals(true, certificate.getWiretapped());
        assertEquals(2, certificate.getStates().size());
        assertEquals(CertificateState.RECEIVED, certificate.getStates().get(0).getState());
        assertEquals("MI", certificate.getStates().get(0).getTarget());
        assertEquals(CertificateState.SENT, certificate.getStates().get(1).getState());
        assertEquals("FK", certificate.getStates().get(1).getTarget());
        assertEquals(certificate.getStates().get(0).getTimestamp(), certificate.getStates().get(1).getTimestamp());
    }

    @Test
    public void testModuleNotFound() throws Exception {

        when(moduleApiFactory.getModuleEntryPoint("fk7263")).thenThrow(new ModuleNotFoundException());

        try {
            certificateService.storeCertificate(utlatandeJson(), "fk7263", false);
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
            certificateService.storeCertificate(utlatandeJson(), "fk7263", false);
            fail("Expected RuntimeException");
        } catch (RuntimeException ignore) {
        }
    }

    @Test
    public void sendCertificateCallsSenderAndSetsStatus() throws ClientException, IOException {

        Certificate certificate = new CertificateBuilder(CERTIFICATE_ID).civicRegistrationNumber(PERSONNUMMER).build();

        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);

        certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "fk");

        verify(certificateDao).getCertificate(PERSONNUMMER, CERTIFICATE_ID);
        verify(certificateDao).updateStatus(CERTIFICATE_ID, PERSONNUMMER, CertificateState.SENT, "fk", null);
        verify(certificateSender).sendCertificate(certificate, "fk");
    }

    @Test(expected = InvalidCertificateException.class)
    public void testSendCertificateWitUnknownCertificate() throws ClientException {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(null);

        certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "fk");
    }

    @Test(expected = CertificateRevokedException.class)
    public void testSendRevokedCertificate() throws ClientException {
        Certificate revokedCertificate = new CertificateBuilder(CERTIFICATE_ID).state(CertificateState.CANCELLED, null)
                .build();

        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(revokedCertificate);

        certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "fk");
    }

    @Test(expected = MissingConsentException.class)
    public void testGetCertificateWithoutConsent() throws ClientException {
        when(consentService.isConsent(PERSONNUMMER)).thenReturn(false);
        certificateService.getCertificateForCitizen(PERSONNUMMER, CERTIFICATE_ID);
    }
    
    @Test(expected = InvalidCertificateException.class)
    public void testGetCertificateNotFound() throws ClientException {
        when(certificateDao.getCertificate(PERSONNUMMER,CERTIFICATE_ID)).thenReturn(null);
        certificateService.getCertificateForCare(CERTIFICATE_ID);
    }
    
    @Test(expected = InvalidCertificateException.class)
    public void testGetCertificateRevoked() throws ClientException {
        Certificate revokedCertificate = new CertificateBuilder(CERTIFICATE_ID).state(CertificateState.CANCELLED, null)
                .build();
        when(certificateDao.getCertificate(PERSONNUMMER,CERTIFICATE_ID)).thenReturn(revokedCertificate);
        certificateService.getCertificateForCare(CERTIFICATE_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCertificateWithoutConsentCheckForEmptyPersonnummer() throws ClientException {
        Certificate certificate = createCertificate();
        when(certificateDao.getCertificate(null, CERTIFICATE_ID)).thenReturn(certificate);

        certificateService.getCertificateForCitizen(null, CERTIFICATE_ID);
    }
}
