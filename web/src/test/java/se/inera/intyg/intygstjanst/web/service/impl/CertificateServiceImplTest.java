/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.integration.module.exception.CertificateAlreadyExistsException;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateRelation;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.exception.PersistenceException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.service.CertificateSenderService;
import se.inera.intyg.intygstjanst.web.service.CertificateService.SendStatus;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RelationService;
import se.inera.intyg.intygstjanst.web.service.SjukfallCertificateService;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.intygstjanst.web.service.builder.RecipientBuilder;
import se.inera.intyg.schemas.contract.Personnummer;

@RunWith(MockitoJUnitRunner.class)
public class CertificateServiceImplTest {

    private static final Personnummer PERSONNUMMER = Personnummer.createPersonnummer("191212121212").get();
    private static final String CERTIFICATE_ID = "<certificate-id>";

    private static final String RECIPIENT_ID = "FKASSA";
    private static final String RECIPIENT_NAME = "Försäkringskassan";
    private static final String RECIPIENT_LOGICALADDRESS = "FKORG";
    private static final String RECIPIENT_CERTIFICATETYPES = "fk7263";

    private static final String HSVARD_ID = "HSVARD";

    @Mock
    private CertificateDao certificateDao;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CertificateSenderService certificateSender;

    @Mock
    private RecipientServiceImpl recipientService;

    @Mock
    private RelationService relationService;

    @Mock
    private MonitoringLogService monitoringLogService;

    @Mock
    private IntygModuleRegistryImpl moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @Mock
    private StatisticsService statisticsService;

    @Mock
    private SjukfallCertificateService sjukfallCertificateService;

    @InjectMocks
    private CertificateServiceImpl certificateService = new CertificateServiceImpl();

    private Certificate createCertificate() {
        Certificate certificate = new Certificate(CERTIFICATE_ID);
        certificate.setCivicRegistrationNumber(PERSONNUMMER);
        return certificate;
    }

    private Recipient createRecipient() {
        return new RecipientBuilder()
            .setLogicalAddress(RECIPIENT_LOGICALADDRESS)
            .setName(RECIPIENT_NAME)
            .setId(RECIPIENT_ID)
            .setCertificateTypes(RECIPIENT_CERTIFICATETYPES)
            .setActive(true)
            .setTrusted(true)
            .build();
    }

    @Before
    public void setup() {
        when(recipientService.getPrimaryRecipientHsvard()).thenReturn(
            new RecipientBuilder().setLogicalAddress("TEST")
                .setName("Hälso- och sjukvården")
                .setId(HSVARD_ID)
                .setCertificateTypes("fk7263")
                .setActive(true)
                .setTrusted(true)
                .build());

        doNothing().when(relationService).storeRelation(any(Relation.class));
    }

    @Test
    public void testLogCertificateRetrieved() {
        certificateService.logCertificateRetrieved("id", "type", "enhethsaid", "part");
        verify(monitoringLogService)
            .logCertificateRetrieved("id", "type", "enhethsaid", "part");
    }

    @Test
    public void testLogCertificateRetrievedNoPart() {
        certificateService.logCertificateRetrieved("id", "type", "enhethsaid", null);
        verify(monitoringLogService)
            .logCertificateRetrieved("id", "type", "enhethsaid", "N/A");
    }

    @Test
    public void testStoreCertificateHappyCase() throws Exception {
        CertificateHolder certificateHolder = new CertificateHolder();
        certificateHolder.setId("id");
        certificateHolder.setOriginalCertificate("original");
        certificateHolder.setCertificateRelation(new CertificateRelation("id", "id-0", RelationKod.ERSATT, LocalDateTime.now()));

        ArgumentCaptor<OriginalCertificate> originalCertificateCaptor = ArgumentCaptor
            .forClass(OriginalCertificate.class);
        when(certificateDao.storeOriginalCertificate(originalCertificateCaptor.capture())).thenReturn(1L);

        Certificate certificate = certificateService.storeCertificate(certificateHolder);

        assertEquals("id", certificate.getId());
        assertEquals(1, certificate.getStates().size());
        assertEquals(CertificateState.RECEIVED, certificate.getStates().get(0).getState());
        assertEquals("HSVARD", certificate.getStates().get(0).getTarget());

        LocalDateTime aMinuteAgo = LocalDateTime.now().minusMinutes(1);
        LocalDateTime inAMinute = LocalDateTime.now().plusMinutes(1);
        assertTrue(certificate.getStates().get(0).getTimestamp().isAfter(aMinuteAgo));
        assertTrue(certificate.getStates().get(0).getTimestamp().isBefore(inAMinute));

        verify(certificateDao).store(certificate);
        verify(relationService).storeRelation(any(Relation.class));

        OriginalCertificate originalCertificate = originalCertificateCaptor.getValue();
        assertEquals(certificate, originalCertificate.getCertificate());
        assertEquals("original", originalCertificate.getDocument());
        assertTrue(originalCertificate.getReceived().isAfter(aMinuteAgo));
        assertTrue(originalCertificate.getReceived().isBefore(inAMinute));
    }

    @Test(expected = InvalidCertificateException.class)
    public void testStoreCertificateInvalid() throws Exception {
        CertificateHolder certificateHolder = new CertificateHolder();
        certificateHolder.setId("id");
        certificateHolder.setOriginalCertificate("original");

        when(certificateDao.getCertificate(
            or(isNull(), any(Personnummer.class)),
            or(isNull(), anyString()))).thenThrow((new PersistenceException(CERTIFICATE_ID, null)));

        certificateService.storeCertificate(certificateHolder);
    }

    @Test(expected = CertificateAlreadyExistsException.class)
    public void testStoreCertificateAlreadyExists() throws Exception {
        CertificateHolder certificateHolder = new CertificateHolder();
        certificateHolder.setId("id");
        certificateHolder.setOriginalCertificate("original");
        when(certificateDao.getCertificate(
            or(isNull(), any(Personnummer.class)),
            or(isNull(), anyString()))).thenReturn(new Certificate());

        certificateService.storeCertificate(certificateHolder);
    }

    @Test
    public void sendCertificateCallsSenderAndSetsStatus() throws Exception {

        Certificate certificate = new Certificate(CERTIFICATE_ID);
        certificate.setCivicRegistrationNumber(PERSONNUMMER);

        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);

        SendStatus res = certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, RECIPIENT_ID);

        assertEquals(SendStatus.OK, res);
        verify(certificateDao).getCertificate(PERSONNUMMER, CERTIFICATE_ID);
        verify(certificateDao).updateStatus(CERTIFICATE_ID, PERSONNUMMER, CertificateState.SENT, RECIPIENT_ID, null);
        verify(certificateSender).sendCertificate(certificate, RECIPIENT_ID);
    }

    @Test
    public void sendCertificateAlreadySentCertificate() throws Exception {

        Certificate certificate = new Certificate(CERTIFICATE_ID);
        certificate.setCivicRegistrationNumber(PERSONNUMMER);
        List<CertificateStateHistoryEntry> states = new ArrayList<>(certificate.getStates());
        states.add(new CertificateStateHistoryEntry(RECIPIENT_ID, CertificateState.SENT, LocalDateTime.now()));
        certificate.setStates(states);

        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);

        SendStatus res = certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, RECIPIENT_ID);

        assertEquals(SendStatus.ALREADY_SENT, res);
        verify(certificateDao).getCertificate(PERSONNUMMER, CERTIFICATE_ID);
        verify(certificateDao, times(0)).updateStatus(CERTIFICATE_ID, PERSONNUMMER, CertificateState.SENT, RECIPIENT_ID, null);
        verify(certificateSender, times(0)).sendCertificate(certificate, RECIPIENT_ID);
    }

    @Test(expected = InvalidCertificateException.class)
    public void testSendCertificateInvalidCertificate() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenThrow(new PersistenceException(CERTIFICATE_ID, PERSONNUMMER));
        certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "fkassa");
    }

    @Test(expected = InvalidCertificateException.class)
    public void testSendCertificateWithUnknownCertificate() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(null);
        certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "fkassa");
    }

    @Test(expected = CertificateRevokedException.class)
    public void testSendRevokedCertificate() throws Exception {
        Certificate revokedCertificate = new Certificate(CERTIFICATE_ID);
        revokedCertificate
            .setStates(Arrays.asList(new CertificateStateHistoryEntry("target", CertificateState.CANCELLED, LocalDateTime.now())));
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(revokedCertificate);
        certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "fkassa");
    }

    @Test
    public void testGetCertificateForCare() throws Exception {
        when(certificateDao.getCertificate(null, CERTIFICATE_ID)).thenReturn(new Certificate());
        assertNotNull(certificateService.getCertificateForCare(CERTIFICATE_ID));
    }

    @Test(expected = InvalidCertificateException.class)
    public void testGetCertificateForCareInvalidCertificate() throws Exception {
        when(certificateDao.getCertificate(null, CERTIFICATE_ID)).thenThrow(new PersistenceException(CERTIFICATE_ID, null));
        certificateService.getCertificateForCare(CERTIFICATE_ID);
    }

    @Test(expected = InvalidCertificateException.class)
    public void testGetCertificateForCareNotFound() throws Exception {
        when(certificateDao.getCertificate(null, CERTIFICATE_ID)).thenReturn(null);
        certificateService.getCertificateForCare(CERTIFICATE_ID);
    }

    @Test
    public void testGetCertificateForCareRevoked() throws Exception {
        Certificate revokedCertificate = new Certificate(CERTIFICATE_ID);
        revokedCertificate.addState(new CertificateStateHistoryEntry("target", CertificateState.CANCELLED, LocalDateTime.now()));
        when(certificateDao.getCertificate(null, CERTIFICATE_ID)).thenReturn(revokedCertificate);
        assertNotNull(certificateService.getCertificateForCare(CERTIFICATE_ID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCertificateCheckForNullPersonnummer() throws Exception {
        certificateService.getCertificateForCitizen(null, CERTIFICATE_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCertificateCheckForEmptyPersonnummer() throws Exception {
        certificateService.getCertificateForCitizen(createPnr(null), CERTIFICATE_ID);
    }

    @Test
    public void testRevokeCertificate() throws Exception {
        final Personnummer civicRegistrationNumber = createPnr("191212121212");
        when(certificateDao.getCertificate(civicRegistrationNumber, CERTIFICATE_ID)).thenReturn(new Certificate(CERTIFICATE_ID));
        Certificate revokeCertificate = certificateService.revokeCertificate(civicRegistrationNumber, CERTIFICATE_ID);
        assertEquals(CERTIFICATE_ID, revokeCertificate.getId());

        // verify status CANCELLED is set
        verify(certificateDao).updateStatus(CERTIFICATE_ID, civicRegistrationNumber, CertificateState.CANCELLED, "HSVARD",
            null);
    }

    @Test(expected = InvalidCertificateException.class)
    public void testRevokeCertificateGetThrowsPersistenceException() throws Exception {
        final Personnummer civicRegistrationNumber = createPnr("191212121212");
        when(certificateDao.getCertificate(civicRegistrationNumber, CERTIFICATE_ID))
            .thenThrow(new PersistenceException(CERTIFICATE_ID, civicRegistrationNumber));
        certificateService.revokeCertificate(civicRegistrationNumber, CERTIFICATE_ID);
    }

    @Test(expected = InvalidCertificateException.class)
    public void testRevokeCertificateUpdateStatusThrowsPersistenceException() throws Exception {
        final Personnummer civicRegistrationNumber = createPnr("191212121212");
        when(certificateDao.getCertificate(civicRegistrationNumber, CERTIFICATE_ID)).thenReturn(new Certificate(CERTIFICATE_ID));
        doThrow(new PersistenceException(CERTIFICATE_ID, civicRegistrationNumber)).when(certificateDao).updateStatus(CERTIFICATE_ID,
            civicRegistrationNumber, CertificateState.CANCELLED, "HSVARD", null);
        certificateService.revokeCertificate(civicRegistrationNumber, CERTIFICATE_ID);
    }

    @Test(expected = InvalidCertificateException.class)
    public void testRevokeCertificateNullAnswer() throws Exception {
        final Personnummer civicRegistrationNumber = createPnr("191212121212");
        when(certificateDao.getCertificate(civicRegistrationNumber, CERTIFICATE_ID)).thenReturn(null);
        certificateService.revokeCertificate(civicRegistrationNumber, CERTIFICATE_ID);
    }

    @Test(expected = CertificateRevokedException.class)
    public void testRevokeCertificateAlreadyRevoked() throws Exception {
        final Personnummer civicRegistrationNumber = createPnr("191212121212");
        Certificate certificate = new Certificate(CERTIFICATE_ID);
        certificate.addState(new CertificateStateHistoryEntry("HSVARD", CertificateState.CANCELLED, LocalDateTime.now()));
        when(certificateDao.getCertificate(civicRegistrationNumber, CERTIFICATE_ID)).thenReturn(certificate);
        certificateService.revokeCertificate(civicRegistrationNumber, CERTIFICATE_ID);
    }

    @Test
    public void testListCertificatesForCitizen() {
        final List<String> certificateTypes = Arrays.asList("fk7263");
        final LocalDate fromDate = LocalDate.now();
        final LocalDate toDate = fromDate.plusDays(2);
        certificateService.listCertificatesForCitizen(PERSONNUMMER, certificateTypes, fromDate, toDate);
        verify(certificateDao).findCertificate(PERSONNUMMER, certificateTypes, fromDate, toDate, null);
    }

    @Test
    public void testListCertificatesForCare() {
        final List<String> careUnits = Arrays.asList("enhet-1");
        certificateService.listCertificatesForCare(PERSONNUMMER, careUnits);
        verify(certificateDao).findCertificate(PERSONNUMMER, null, null, null, careUnits);
    }

    @Test
    public void testGetCertificateForCitizen() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(new Certificate());
        assertNotNull(certificateService.getCertificateForCitizen(PERSONNUMMER, CERTIFICATE_ID));
        verify(certificateDao).getCertificate(PERSONNUMMER, CERTIFICATE_ID);
    }

    @Test(expected = InvalidCertificateException.class)
    public void testGetCertificateForCitizenInvalidCertificate() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenThrow(new PersistenceException(CERTIFICATE_ID, PERSONNUMMER));
        certificateService.getCertificateForCitizen(PERSONNUMMER, CERTIFICATE_ID);
    }

    @Test(expected = InvalidCertificateException.class)
    public void testGetCertificateForCitizenCertificateNull() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(null);
        certificateService.getCertificateForCitizen(PERSONNUMMER, CERTIFICATE_ID);
    }

    @Test(expected = CertificateRevokedException.class)
    public void testGetCertificateForCitizenCertificateRevoked() throws Exception {
        Certificate revokedCertificate = new Certificate();
        revokedCertificate.addState(new CertificateStateHistoryEntry("HSVARD", CertificateState.CANCELLED, LocalDateTime.now()));
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(revokedCertificate);
        certificateService.getCertificateForCitizen(PERSONNUMMER, CERTIFICATE_ID);
    }

    @Test
    public void testSetCertificateStateWithCivicRegistrationNumber() throws Exception {
        final String target = "FKASSA";
        final CertificateState state = CertificateState.SENT;
        final LocalDateTime timestamp = LocalDateTime.now();
        certificateService.setCertificateState(PERSONNUMMER, CERTIFICATE_ID, target, state, timestamp);
        verify(certificateDao).updateStatus(CERTIFICATE_ID, PERSONNUMMER, state, target, timestamp);
    }

    @Test(expected = InvalidCertificateException.class)
    public void testSetCertificateStateWithCivicRegistrationNumberInvalidCertificate() throws Exception {
        final String target = "FKASSA";
        final CertificateState state = CertificateState.SENT;
        final LocalDateTime timestamp = LocalDateTime.now();
        doThrow(new PersistenceException(CERTIFICATE_ID, PERSONNUMMER)).when(certificateDao).updateStatus(CERTIFICATE_ID, PERSONNUMMER,
            state, target,
            timestamp);
        certificateService.setCertificateState(PERSONNUMMER, CERTIFICATE_ID, target, state, timestamp);
    }

    @Test
    public void testSetCertificateState() throws Exception {
        final String target = "FKASSA";
        final CertificateState state = CertificateState.SENT;
        final LocalDateTime timestamp = LocalDateTime.now();
        certificateService.setCertificateState(CERTIFICATE_ID, target, state, timestamp);
        verify(certificateDao).updateStatus(CERTIFICATE_ID, state, target, timestamp);
    }

    @Test(expected = InvalidCertificateException.class)
    public void testSetCertificateStateInvalidCertificate() throws Exception {
        final String target = "FKASSA";
        final CertificateState state = CertificateState.SENT;
        final LocalDateTime timestamp = LocalDateTime.now();
        doThrow(new PersistenceException(CERTIFICATE_ID, null)).when(certificateDao).updateStatus(CERTIFICATE_ID, state, target, timestamp);
        certificateService.setCertificateState(CERTIFICATE_ID, target, state, timestamp);
    }

    @Test
    public void testCertificateReceived() throws Exception {
        final String certificateType = "luse";
        final String certificateTypeVersion = "1.0";
        final String careUnitId = "enhet-1";
        final String originalXml = "original";
        final String transformedXml = "transformedXml";
        CertificateHolder certificateHolder = new CertificateHolder();
        certificateHolder.setId(CERTIFICATE_ID);
        certificateHolder.setType(certificateType);
        certificateHolder.setTypeVersion(certificateTypeVersion);
        certificateHolder.setCareUnitId(careUnitId);
        certificateHolder.setOriginalCertificate(originalXml);
        when(moduleRegistry.getModuleApi(certificateType, certificateTypeVersion)).thenReturn(moduleApi);
        when(moduleApi.transformToStatisticsService(originalXml)).thenReturn(transformedXml);

        certificateService.certificateReceived(certificateHolder);
        verify(certificateDao).store(any(Certificate.class));
        verify(monitoringLogService).logCertificateRegistered(CERTIFICATE_ID, certificateType, careUnitId);
        verify(statisticsService).created(transformedXml, CERTIFICATE_ID, certificateType, careUnitId);
        verify(sjukfallCertificateService).created(any(Certificate.class));
    }

    @Test
    public void testCertificateReceivedModuleNotFound() throws Exception {
        final String certificateType = "luse";
        final String certificateTypeVersion = "1.0";
        CertificateHolder certificateHolder = new CertificateHolder();
        certificateHolder.setId(CERTIFICATE_ID);
        certificateHolder.setType(certificateType);
        certificateHolder.setTypeVersion(certificateTypeVersion);
        certificateHolder.setCareUnitId("enhet-1");
        certificateHolder.setOriginalCertificate("original");
        when(moduleRegistry.getModuleApi(certificateType, certificateTypeVersion)).thenThrow(new ModuleNotFoundException());

        try {
            certificateService.certificateReceived(certificateHolder);
            fail("should throw");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ModuleNotFoundException);
            verify(certificateDao).store(any(Certificate.class));
            verify(monitoringLogService).logCertificateRegistered(CERTIFICATE_ID, certificateType, "enhet-1");
            verifyZeroInteractions(moduleApi);
            verifyZeroInteractions(statisticsService);
            verifyZeroInteractions(sjukfallCertificateService);
        }
    }

    @Test
    public void testCertificateReceivedModuleException() throws Exception {
        final String certificateType = "luse";
        final String certificateTypeVersion = "1.0";
        final String originalXml = "original";
        CertificateHolder certificateHolder = new CertificateHolder();
        certificateHolder.setId(CERTIFICATE_ID);
        certificateHolder.setType(certificateType);
        certificateHolder.setTypeVersion(certificateTypeVersion);
        certificateHolder.setCareUnitId("enhet-1");
        certificateHolder.setOriginalCertificate(originalXml);
        when(moduleRegistry.getModuleApi(certificateType, certificateTypeVersion)).thenReturn(moduleApi);
        when(moduleApi.transformToStatisticsService(originalXml)).thenThrow(new ModuleException());

        try {
            certificateService.certificateReceived(certificateHolder);
            fail("should throw");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ModuleException);
            verify(certificateDao).store(any(Certificate.class));
            verify(monitoringLogService).logCertificateRegistered(CERTIFICATE_ID, certificateType, "enhet-1");
            verifyZeroInteractions(statisticsService);
            verifyZeroInteractions(sjukfallCertificateService);
        }
    }

    @Test
    public void testGetCertificate() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(new Certificate());
        assertNotNull(certificateService.getCertificate(CERTIFICATE_ID, PERSONNUMMER, false));
    }

    @Test
    public void testGetCertificateCheckConsent() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(new Certificate());
        assertNotNull(certificateService.getCertificate(CERTIFICATE_ID, PERSONNUMMER, true));
    }

    @Test(expected = InvalidCertificateException.class)
    public void testGetCertificateInvalidCertificate() throws Exception {
        when(certificateDao.getCertificate(null, CERTIFICATE_ID)).thenThrow(new PersistenceException(CERTIFICATE_ID, null));
        certificateService.getCertificate(CERTIFICATE_ID, null, true);
    }

    @Test(expected = InvalidCertificateException.class)
    public void testGetCertificateNotFound() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(null);
        certificateService.getCertificate(CERTIFICATE_ID, PERSONNUMMER, false);
    }

    @Test
    public void testGetCertificateRevoked() throws Exception {
        Certificate revokedCertificate = new Certificate(CERTIFICATE_ID);
        revokedCertificate.addState(new CertificateStateHistoryEntry("target", CertificateState.CANCELLED, LocalDateTime.now()));
        when(certificateDao.getCertificate(null, CERTIFICATE_ID)).thenReturn(revokedCertificate);
        assertNotNull(certificateService.getCertificate(CERTIFICATE_ID, null, false));
    }

    private Personnummer createPnr(String pnr) {
        return Personnummer.createPersonnummer(pnr)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer"));
    }

}
