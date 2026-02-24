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
package se.inera.intyg.intygstjanst.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
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
import se.inera.intyg.infra.pu.integration.api.model.Person;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.infra.pu.integration.api.services.PUService;
import se.inera.intyg.intygstjanst.logging.HashUtility;
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
import se.inera.intyg.intygstjanst.web.service.builder.RecipientBuilder;
import se.inera.intyg.schemas.contract.Personnummer;

@ExtendWith(MockitoExtension.class)
class CertificateServiceImplTest {

    private static final Personnummer PERSONNUMMER = Personnummer.createPersonnummer("191212121212").orElseThrow();
    private static final String CERTIFICATE_ID = "<certificate-id>";
    private static final String RECIPIENT_ID = "FKASSA";
    private static final String HSVARD_ID = "HSVARD";
    private static final String PERSONNUMMER_HASH = "personnummerHash";

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

    @Mock
    private PUService puService;

    @Spy
    private HashUtility hashUtility;

    @InjectMocks
    private CertificateServiceImpl certificateService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(hashUtility, "salt", "salt");
        final Person person = new Person(null, false, false, "", "", "", "", "", "", false);
        final PersonSvar personSvar = PersonSvar.found(person);
        lenient().when(puService.getPerson(any())).thenReturn(personSvar);

        lenient().when(recipientService.getPrimaryRecipientHsvard()).thenReturn(
            new RecipientBuilder().setLogicalAddress("TEST")
                .setName("Hälso- och sjukvården")
                .setId(HSVARD_ID)
                .setCertificateTypes("fk7263")
                .setActive(true)
                .setTrusted(true)
                .build());

        lenient().doNothing().when(relationService).storeRelation(any(Relation.class));
    }

    @Test
    void testLogCertificateRetrieved() {
        certificateService.logCertificateRetrieved("id", "type", "enhethsaid", "part");
        verify(monitoringLogService)
            .logCertificateRetrieved("id", "type", "enhethsaid", "part");
    }

    @Test
    void testLogCertificateRetrievedNoPart() {
        certificateService.logCertificateRetrieved("id", "type", "enhethsaid", null);
        verify(monitoringLogService)
            .logCertificateRetrieved("id", "type", "enhethsaid", "N/A");
    }

    @Test
    void testStoreCertificateHappyCase() throws Exception {
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
        assertEquals(CertificateState.RECEIVED, certificate.getStates().getFirst().getState());
        assertEquals("HSVARD", certificate.getStates().getFirst().getTarget());

        LocalDateTime aMinuteAgo = LocalDateTime.now().minusMinutes(1);
        LocalDateTime inAMinute = LocalDateTime.now().plusMinutes(1);
        assertTrue(certificate.getStates().getFirst().getTimestamp().isAfter(aMinuteAgo));
        assertTrue(certificate.getStates().getFirst().getTimestamp().isBefore(inAMinute));

        verify(certificateDao).store(certificate);
        verify(relationService).storeRelation(any(Relation.class));

        OriginalCertificate originalCertificate = originalCertificateCaptor.getValue();
        assertEquals(certificate, originalCertificate.getCertificate());
        assertEquals("original", originalCertificate.getDocument());
        assertTrue(originalCertificate.getReceived().isAfter(aMinuteAgo));
        assertTrue(originalCertificate.getReceived().isBefore(inAMinute));
    }

    @Test
    void testStoreCertificateInvalid() throws Exception {
        CertificateHolder certificateHolder = new CertificateHolder();
        certificateHolder.setId("id");
        certificateHolder.setOriginalCertificate("original");

        when(certificateDao.getCertificate(
            or(isNull(), any(Personnummer.class)),
            or(isNull(), anyString()))).thenThrow((new PersistenceException(CERTIFICATE_ID, null)));

        assertThrows(InvalidCertificateException.class, () -> certificateService.storeCertificate(certificateHolder));
    }

    @Test
    void testStoreCertificateAlreadyExists() throws Exception {
        CertificateHolder certificateHolder = new CertificateHolder();
        certificateHolder.setId("id");
        certificateHolder.setOriginalCertificate("original");
        when(certificateDao.getCertificate(
            or(isNull(), any(Personnummer.class)),
            or(isNull(), anyString()))).thenReturn(new Certificate());

        assertThrows(CertificateAlreadyExistsException.class, () -> certificateService.storeCertificate(certificateHolder));
    }

    @Test
    void sendCertificateCallsSenderAndSetsStatus() throws Exception {

        Certificate certificate = new Certificate(CERTIFICATE_ID);
        certificate.setCivicRegistrationNumber(PERSONNUMMER);

        when(certificateDao.getCertificate(any(), any())).thenReturn(certificate);

        SendStatus res = certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, RECIPIENT_ID);

        assertEquals(SendStatus.OK, res);
        verify(certificateDao).getCertificate(PERSONNUMMER, CERTIFICATE_ID);
        verify(certificateDao).updateStatus(CERTIFICATE_ID, PERSONNUMMER, CertificateState.SENT, RECIPIENT_ID, null);
        verify(certificateSender).sendCertificate(certificate, RECIPIENT_ID);
    }

    @Test
    void sendCertificateAlreadySentCertificate() throws Exception {

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

    @Test
    void testSendCertificateInvalidCertificate() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenThrow(new PersistenceException(CERTIFICATE_ID,
            PERSONNUMMER_HASH));
        assertThrows(InvalidCertificateException.class, () -> certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "fkassa"));
    }

    @Test
    void testSendCertificateWithUnknownCertificate() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(null);
        assertThrows(InvalidCertificateException.class, () -> certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "fkassa"));
    }

    @Test
    void testSendRevokedCertificate() throws Exception {
        Certificate revokedCertificate = new Certificate(CERTIFICATE_ID);
        revokedCertificate
            .setStates(List.of(new CertificateStateHistoryEntry("target", CertificateState.CANCELLED, LocalDateTime.now())));
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(revokedCertificate);
        assertThrows(CertificateRevokedException.class, () -> certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "fkassa"));
    }

    @Test
    void testGetCertificateForCare() throws Exception {
        when(certificateDao.getCertificate(null, CERTIFICATE_ID)).thenReturn(new Certificate());
        assertNotNull(certificateService.getCertificateForCare(CERTIFICATE_ID));
    }

    @Test
    void testGetCertificateForCareInvalidCertificate() throws Exception {
        when(certificateDao.getCertificate(null, CERTIFICATE_ID)).thenThrow(new PersistenceException(CERTIFICATE_ID, null));
        assertThrows(InvalidCertificateException.class, () -> certificateService.getCertificateForCare(CERTIFICATE_ID));
    }

    @Test
    void testGetCertificateForCareNotFound() throws Exception {
        when(certificateDao.getCertificate(null, CERTIFICATE_ID)).thenReturn(null);
        assertThrows(InvalidCertificateException.class, () -> certificateService.getCertificateForCare(CERTIFICATE_ID));
    }

    @Test
    void testGetCertificateForCareRevoked() throws Exception {
        Certificate revokedCertificate = new Certificate(CERTIFICATE_ID);
        revokedCertificate.addState(new CertificateStateHistoryEntry("target", CertificateState.CANCELLED, LocalDateTime.now()));
        when(certificateDao.getCertificate(null, CERTIFICATE_ID)).thenReturn(revokedCertificate);
        assertNotNull(certificateService.getCertificateForCare(CERTIFICATE_ID));
    }

    @Test
    void testGetCertificateCheckForNullPersonnummer() {
        assertThrows(IllegalArgumentException.class, () -> certificateService.getCertificateForCitizen(null, CERTIFICATE_ID));
    }

    @Test
    void testRevokeCertificate() throws Exception {
        final Personnummer civicRegistrationNumber = createPnr("191212121212");
        when(certificateDao.getCertificate(any(), any())).thenReturn(new Certificate(CERTIFICATE_ID));
        Certificate revokeCertificate = certificateService.revokeCertificate(civicRegistrationNumber, CERTIFICATE_ID);
        assertEquals(CERTIFICATE_ID, revokeCertificate.getId());

        // verify status CANCELLED is set
        verify(certificateDao).updateStatus(CERTIFICATE_ID, civicRegistrationNumber, CertificateState.CANCELLED, "HSVARD",
            null);
    }

    @Test
    void testRevokeCertificateGetThrowsPersistenceException() throws Exception {
        final Personnummer civicRegistrationNumber = createPnr("191212121212");
        when(certificateDao.getCertificate(civicRegistrationNumber, CERTIFICATE_ID))
            .thenThrow(new PersistenceException(CERTIFICATE_ID, PERSONNUMMER_HASH));
        assertThrows(InvalidCertificateException.class,
            () -> certificateService.revokeCertificate(civicRegistrationNumber, CERTIFICATE_ID));
    }

    @Test
    void testRevokeCertificateUpdateStatusThrowsPersistenceException() throws Exception {
        final Personnummer civicRegistrationNumber = createPnr("191212121212");
        when(certificateDao.getCertificate(any(), any())).thenReturn(new Certificate(CERTIFICATE_ID));
        doThrow(new PersistenceException(CERTIFICATE_ID, PERSONNUMMER_HASH)).when(certificateDao).updateStatus(CERTIFICATE_ID,
            civicRegistrationNumber, CertificateState.CANCELLED, "HSVARD", null);
        assertThrows(InvalidCertificateException.class,
            () -> certificateService.revokeCertificate(civicRegistrationNumber, CERTIFICATE_ID));
    }

    @Test
    void testRevokeCertificateNullAnswer() throws Exception {
        final Personnummer civicRegistrationNumber = createPnr("191212121212");
        when(certificateDao.getCertificate(civicRegistrationNumber, CERTIFICATE_ID)).thenReturn(null);
        assertThrows(InvalidCertificateException.class,
            () -> certificateService.revokeCertificate(civicRegistrationNumber, CERTIFICATE_ID));
    }

    @Test
    void testRevokeCertificateAlreadyRevoked() throws Exception {
        final Personnummer civicRegistrationNumber = createPnr("191212121212");
        Certificate certificate = new Certificate(CERTIFICATE_ID);
        certificate.addState(new CertificateStateHistoryEntry("HSVARD", CertificateState.CANCELLED, LocalDateTime.now()));
        when(certificateDao.getCertificate(civicRegistrationNumber, CERTIFICATE_ID)).thenReturn(certificate);
        assertThrows(CertificateRevokedException.class,
            () -> certificateService.revokeCertificate(civicRegistrationNumber, CERTIFICATE_ID));
    }

    @Test
    void testListCertificatesForCitizen() {
        final List<String> certificateTypes = List.of("fk7263");
        final LocalDate fromDate = LocalDate.now();
        final LocalDate toDate = fromDate.plusDays(2);
        certificateService.listCertificatesForCitizen(PERSONNUMMER, certificateTypes, fromDate, toDate);
        verify(certificateDao).findCertificate(PERSONNUMMER, certificateTypes, fromDate, toDate, null);
    }

    @Test
    void testListCertificatesForCare() {
        final List<String> careUnits = List.of("enhet-1");
        certificateService.listCertificatesForCare(PERSONNUMMER, careUnits);
        verify(certificateDao).findCertificate(PERSONNUMMER, null, null, null, careUnits);
    }

    @Test
    void testGetCertificateForCitizen() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(new Certificate());
        assertNotNull(certificateService.getCertificateForCitizen(PERSONNUMMER, CERTIFICATE_ID));
        verify(certificateDao).getCertificate(PERSONNUMMER, CERTIFICATE_ID);
    }

    @Test
    void testGetCertificateForCitizenInvalidCertificate() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenThrow(new PersistenceException(CERTIFICATE_ID,
            PERSONNUMMER_HASH));
        assertThrows(InvalidCertificateException.class, () -> certificateService.getCertificateForCitizen(PERSONNUMMER, CERTIFICATE_ID));
    }

    @Test
    void testGetCertificateForCitizenCertificateNull() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(null);
        assertThrows(InvalidCertificateException.class, () -> certificateService.getCertificateForCitizen(PERSONNUMMER, CERTIFICATE_ID));
    }

    @Test
    void testGetCertificateForCitizenCertificateRevoked() throws Exception {
        Certificate revokedCertificate = new Certificate();
        revokedCertificate.addState(new CertificateStateHistoryEntry("HSVARD", CertificateState.CANCELLED, LocalDateTime.now()));
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(revokedCertificate);
        assertThrows(CertificateRevokedException.class, () -> certificateService.getCertificateForCitizen(PERSONNUMMER, CERTIFICATE_ID));
    }

    @Test
    void testSetCertificateStateWithCivicRegistrationNumber() throws Exception {
        final String target = "FKASSA";
        final CertificateState state = CertificateState.SENT;
        final LocalDateTime timestamp = LocalDateTime.now();
        final Certificate certificate = new Certificate();
        when(certificateDao.getCertificate(any(), any())).thenReturn(certificate);
        certificateService.setCertificateState(PERSONNUMMER, CERTIFICATE_ID, target, state, timestamp);
        verify(certificateDao).updateStatus(CERTIFICATE_ID, PERSONNUMMER, state, target, timestamp);
    }

    @Test
    void testSetCertificateStateWithCivicRegistrationNumberInvalidCertificate() throws Exception {
        final String target = "FKASSA";
        final CertificateState state = CertificateState.SENT;
        final LocalDateTime timestamp = LocalDateTime.now();
        final Certificate certificate = new Certificate();
        when(certificateDao.getCertificate(any(), any())).thenReturn(certificate);
        doThrow(new PersistenceException(CERTIFICATE_ID, PERSONNUMMER_HASH)).when(certificateDao).updateStatus(CERTIFICATE_ID, PERSONNUMMER,
            state, target,
            timestamp);
        assertThrows(InvalidCertificateException.class,
            () -> certificateService.setCertificateState(PERSONNUMMER, CERTIFICATE_ID, target, state, timestamp));
    }

    @Test
    void testSetCertificateState() throws Exception {
        final String target = "FKASSA";
        final CertificateState state = CertificateState.SENT;
        final LocalDateTime timestamp = LocalDateTime.now();
        Certificate certificate = new Certificate();
        when(certificateDao.getCertificate(any(), any())).thenReturn(certificate);
        certificateService.setCertificateState(CERTIFICATE_ID, target, state, timestamp);
        verify(certificateDao).updateStatus(CERTIFICATE_ID, state, target, timestamp);
    }

    @Test
    void testSetCertificateStateInvalidCertificate() throws Exception {
        final String target = "FKASSA";
        final CertificateState state = CertificateState.SENT;
        final LocalDateTime timestamp = LocalDateTime.now();
        final Certificate certificate = new Certificate();
        when(certificateDao.getCertificate(any(), any())).thenReturn(certificate);
        doThrow(new PersistenceException(CERTIFICATE_ID, null)).when(certificateDao).updateStatus(CERTIFICATE_ID, state, target, timestamp);
        assertThrows(InvalidCertificateException.class,
            () -> certificateService.setCertificateState(CERTIFICATE_ID, target, state, timestamp));
    }

    @Test
    void testCertificateReceived() throws Exception {
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
    void testCertificateReceivedModuleNotFound() throws Exception {
        final String certificateType = "luse";
        final String certificateTypeVersion = "1.0";
        CertificateHolder certificateHolder = new CertificateHolder();
        certificateHolder.setId(CERTIFICATE_ID);
        certificateHolder.setType(certificateType);
        certificateHolder.setTypeVersion(certificateTypeVersion);
        certificateHolder.setCareUnitId("enhet-1");
        certificateHolder.setOriginalCertificate("original");
        when(moduleRegistry.getModuleApi(certificateType, certificateTypeVersion)).thenThrow(new ModuleNotFoundException());

        Exception e = assertThrows(Exception.class, () -> certificateService.certificateReceived(certificateHolder));
        assertInstanceOf(ModuleNotFoundException.class, e.getCause());
        verify(certificateDao).store(any(Certificate.class));
        verify(monitoringLogService).logCertificateRegistered(CERTIFICATE_ID, certificateType, "enhet-1");
        verifyNoInteractions(moduleApi);
        verifyNoInteractions(statisticsService);
        verifyNoInteractions(sjukfallCertificateService);
    }

    @Test
    void testCertificateReceivedModuleException() throws Exception {
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

        Exception e = assertThrows(Exception.class, () -> certificateService.certificateReceived(certificateHolder));
        assertInstanceOf(ModuleException.class, e.getCause());
        verify(certificateDao).store(any(Certificate.class));
        verify(monitoringLogService).logCertificateRegistered(CERTIFICATE_ID, certificateType, "enhet-1");
        verifyNoInteractions(statisticsService);
        verifyNoInteractions(sjukfallCertificateService);
    }

    @Test
    void testGetCertificate() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(new Certificate());
        assertNotNull(certificateService.getCertificate(CERTIFICATE_ID, PERSONNUMMER, false));
    }

    @Test
    void testGetCertificateCheckConsent() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(new Certificate());
        assertNotNull(certificateService.getCertificate(CERTIFICATE_ID, PERSONNUMMER, true));
    }

    @Test
    void testGetCertificateInvalidCertificate() throws Exception {
        when(certificateDao.getCertificate(null, CERTIFICATE_ID)).thenThrow(new PersistenceException(CERTIFICATE_ID, null));
        assertThrows(InvalidCertificateException.class, () -> certificateService.getCertificate(CERTIFICATE_ID, null, true));
    }

    @Test
    void testGetCertificateNotFound() throws Exception {
        when(certificateDao.getCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(null);
        assertThrows(InvalidCertificateException.class, () -> certificateService.getCertificate(CERTIFICATE_ID, PERSONNUMMER, false));
    }

    @Test
    void testGetCertificateRevoked() throws Exception {
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
