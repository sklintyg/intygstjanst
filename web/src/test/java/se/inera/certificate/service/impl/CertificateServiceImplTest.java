package se.inera.certificate.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.integration.module.exception.CertificateRevokedException;
import se.inera.certificate.integration.module.exception.InvalidCertificateException;
import se.inera.certificate.integration.module.exception.MissingConsentException;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.builder.CertificateBuilder;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.model.dao.CertificateDao;
import se.inera.certificate.model.dao.CertificateStateHistoryEntry;
import se.inera.certificate.model.dao.OriginalCertificate;
import se.inera.certificate.modules.support.api.CertificateHolder;
import se.inera.certificate.service.CertificateSenderService;
import se.inera.certificate.service.ConsentService;
import se.inera.certificate.service.recipientservice.Recipient;

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
    private RecipientServiceImpl recipientService;

    @InjectMocks
    private CertificateServiceImpl certificateService = new CertificateServiceImpl();

    @Test
    public void certificateWithDeletedStatusHasMetaDeleted() throws Exception {
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
    public void certificateWithStatusRestoredNewerThanDeletedHasMetaNotDeleted() throws Exception {
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
    public void certificateWithStatusDeletedNewerThanRestoredHasMetaDeleted() throws Exception {
        Certificate certificate = createCertificate();
        certificate.addState(new CertificateStateHistoryEntry("", CertificateState.DELETED, new LocalDateTime(2)));
        certificate.addState(new CertificateStateHistoryEntry("", CertificateState.RESTORED, new LocalDateTime(1)));
        when(consentService.isConsent(anyString())).thenReturn(Boolean.TRUE);
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);

        Certificate found = certificateService.getCertificateForCitizen(PERSONNUMMER, CERTIFICATE_ID);
        assertTrue(found.getDeleted());

        verify(certificateDao).getCertificate(PERSONNUMMER, CERTIFICATE_ID);
    }

    @Test
    public void testStoreCertificateHappyCase() throws Exception {
        CertificateHolder certificateHolder = new CertificateHolder();
        certificateHolder.setId("id");
        certificateHolder.setOriginalCertificate("original");

        ArgumentCaptor<OriginalCertificate> originalCertificateCaptor = ArgumentCaptor
                .forClass(OriginalCertificate.class);
        when(certificateDao.storeOriginalCertificate(originalCertificateCaptor.capture())).thenReturn(1L);

        Certificate certificate = certificateService.storeCertificate(certificateHolder);

        assertEquals("id", certificate.getId());
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
        assertEquals("original", originalCertificate.getDocument());
        assertTrue(originalCertificate.getReceived().isAfter(aMinuteAgo));
        assertTrue(originalCertificate.getReceived().isBefore(inAMinute));
    }

    @Test
    public void sendCertificateCallsSenderAndSetsStatus() throws Exception {

        Certificate certificate = new CertificateBuilder(CERTIFICATE_ID).civicRegistrationNumber(PERSONNUMMER).build();

        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);
        when(recipientService.getRecipientForLogicalAddress(Mockito.any(String.class))).thenReturn(new Recipient("FKORG","Försäkringskassan", "fk"));

        certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "fk");

        verify(certificateDao).getCertificate(PERSONNUMMER, CERTIFICATE_ID);
        verify(certificateDao).updateStatus(CERTIFICATE_ID, PERSONNUMMER, CertificateState.SENT, "fk", null);
        verify(certificateSender).sendCertificate(certificate, "fk");
    }

    @Test(expected = InvalidCertificateException.class)
    public void testSendCertificateWitUnknownCertificate() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(null);

        certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "fk");
    }

    @Test(expected = CertificateRevokedException.class)
    public void testSendRevokedCertificate() throws Exception {
        Certificate revokedCertificate = new CertificateBuilder(CERTIFICATE_ID).state(CertificateState.CANCELLED, null)
                .build();

        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(revokedCertificate);

        certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "fk");
    }

    @Test(expected = MissingConsentException.class)
    public void testGetCertificateWithoutConsent() throws Exception {
        when(consentService.isConsent(PERSONNUMMER)).thenReturn(false);
        certificateService.getCertificateForCitizen(PERSONNUMMER, CERTIFICATE_ID);
    }

    @Test(expected = InvalidCertificateException.class)
    public void testGetCertificateNotFound() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER,CERTIFICATE_ID)).thenReturn(null);
        certificateService.getCertificateForCare(CERTIFICATE_ID);
    }

    @Test(expected = InvalidCertificateException.class)
    public void testGetCertificateRevoked() throws Exception {
        Certificate revokedCertificate = new CertificateBuilder(CERTIFICATE_ID).state(CertificateState.CANCELLED, null)
                .build();
        when(certificateDao.getCertificate(PERSONNUMMER,CERTIFICATE_ID)).thenReturn(revokedCertificate);
        certificateService.getCertificateForCare(CERTIFICATE_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCertificateWithoutConsentCheckForEmptyPersonnummer() throws Exception {
        Certificate certificate = createCertificate();
        when(certificateDao.getCertificate(null, CERTIFICATE_ID)).thenReturn(certificate);

        certificateService.getCertificateForCitizen(null, CERTIFICATE_ID);
    }
}
