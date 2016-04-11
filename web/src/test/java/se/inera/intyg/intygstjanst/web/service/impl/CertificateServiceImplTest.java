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

package se.inera.intyg.intygstjanst.web.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.inera.intyg.common.support.integration.module.exception.*;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.builder.CertificateBuilder;
import se.inera.intyg.intygstjanst.persistence.model.dao.*;
import se.inera.intyg.intygstjanst.web.service.CertificateSenderService;
import se.inera.intyg.intygstjanst.web.service.CertificateService.SendStatus;
import se.inera.intyg.intygstjanst.web.service.ConsentService;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateServiceImplTest {

    private static final Personnummer PERSONNUMMER = new Personnummer("<civicRegistrationNumber>");
    private static final String CERTIFICATE_ID = "<certificate-id>";

    private static final String RECIPIENT_ID = "FK";
    private static final String RECIPIENT_NAME = "Försäkringskassan";
    private static final String RECIPIENT_LOGICALADDRESS = "FKORG";
    private static final String RECIPIENT_CERTIFICATETYPES = "fk7263";

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

    private Certificate createCertificate() {
        Certificate certificate = new Certificate(CERTIFICATE_ID, "document");
        certificate.setCivicRegistrationNumber(PERSONNUMMER);
        return certificate;
    }

    private Recipient createRecipient() {
        return new Recipient(RECIPIENT_LOGICALADDRESS,
                             RECIPIENT_NAME,
                             RECIPIENT_ID,
                             RECIPIENT_CERTIFICATETYPES);

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
        assertEquals("HV", certificate.getStates().get(0).getTarget());

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
        when(recipientService.getRecipientForLogicalAddress(Mockito.any(String.class))).thenReturn(createRecipient());

        SendStatus res = certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, RECIPIENT_ID);

        assertEquals(SendStatus.OK, res);
        verify(certificateDao).getCertificate(PERSONNUMMER, CERTIFICATE_ID);
        verify(certificateDao).updateStatus(CERTIFICATE_ID, PERSONNUMMER, CertificateState.SENT, RECIPIENT_ID, null);
        verify(certificateSender).sendCertificate(certificate, RECIPIENT_ID);
    }

    @Test
    public void sendCertificateAlreadySentCertificate() throws Exception {

        Certificate certificate = new CertificateBuilder(CERTIFICATE_ID).civicRegistrationNumber(PERSONNUMMER).build();
        List<CertificateStateHistoryEntry> states = new ArrayList<>(certificate.getStates());
        states.add(new CertificateStateHistoryEntry(RECIPIENT_ID, CertificateState.SENT, LocalDateTime.now()));
        certificate.setStates(states);

        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);
        when(recipientService.getRecipientForLogicalAddress(Mockito.any(String.class))).thenReturn(createRecipient());

        SendStatus res = certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, RECIPIENT_ID);

        assertEquals(SendStatus.ALREADY_SENT, res);
        verify(certificateDao).getCertificate(PERSONNUMMER, CERTIFICATE_ID);
        verify(certificateDao, times(0)).updateStatus(CERTIFICATE_ID, PERSONNUMMER, CertificateState.SENT, RECIPIENT_ID, null);
        verify(certificateSender, times(0)).sendCertificate(certificate, RECIPIENT_ID);
    }
    @Test(expected = InvalidCertificateException.class)
    public void testSendCertificateWitUnknownCertificate() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(null);
        certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "fk");
    }

    @Test(expected = CertificateRevokedException.class)
    public void testSendRevokedCertificate() throws Exception {
        Certificate revokedCertificate = new CertificateBuilder(CERTIFICATE_ID).state(CertificateState.CANCELLED, null).build();
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
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(null);
        certificateService.getCertificateForCare(CERTIFICATE_ID);
    }

    @Test(expected = InvalidCertificateException.class)
    public void testGetCertificateRevoked() throws Exception {
        Certificate revokedCertificate = new CertificateBuilder(CERTIFICATE_ID).state(CertificateState.CANCELLED, null).build();
        when(certificateDao.getCertificate(PERSONNUMMER,CERTIFICATE_ID)).thenReturn(revokedCertificate);
        certificateService.getCertificateForCare(CERTIFICATE_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCertificateWithoutConsentCheckForNullPersonnummer() throws Exception {
        Certificate certificate = createCertificate();
        when(certificateDao.getCertificate(null, CERTIFICATE_ID)).thenReturn(certificate);
        certificateService.getCertificateForCitizen(null, CERTIFICATE_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCertificateWithoutConsentCheckForEmptyPersonnummer() throws Exception {
        Certificate certificate = createCertificate();
        when(certificateDao.getCertificate(null, CERTIFICATE_ID)).thenReturn(certificate);
        certificateService.getCertificateForCitizen(new Personnummer(null), CERTIFICATE_ID);
    }

}
