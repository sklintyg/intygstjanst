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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEvent.Source;
import se.inera.intyg.infra.intyginfo.dto.IntygInfoEventType;
import se.inera.intyg.infra.intyginfo.dto.ItIntygInfo;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateRepository;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.RelationService;

@ExtendWith(MockitoExtension.class)
class IntygInfoServiceImplTest {

    @Mock
    private CertificateService certificateService;
    @Mock
    private RecipientService recipientService;
    @Mock
    private IntygModuleRegistry moduleRegistry;
    @Mock
    private RelationService relationService;
    @Mock
    private ModuleApi moduleApi;
    @Mock
    private ModuleEntryPoint moduleEntryPoint;
    @Mock
    private CertificateRepository certificateRepository;

    @InjectMocks
    private IntygInfoServiceImpl testee;

    private static final String HSA_ID = "HSA_ID";
    private static final Long CERTIFICATE_COUNT = 333L;

    @BeforeEach
    void setup() throws ModuleNotFoundException {
        lenient().when(moduleRegistry.getModuleEntryPoint(anyString())).thenReturn(moduleEntryPoint);
        lenient().when(moduleEntryPoint.getDefaultRecipient()).thenReturn(LisjpEntryPoint.DEFAULT_RECIPIENT_ID);
        lenient().when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
    }

    @Test
    void notFound() throws InvalidCertificateException {
        when(certificateService.getCertificateForCare(anyString())).thenThrow(InvalidCertificateException.class);

        Optional<ItIntygInfo> intygInfo = testee.getIntygInfo("not_found");

        assertFalse(intygInfo.isPresent());
        verifyNoInteractions(recipientService);
        verifyNoInteractions(moduleRegistry);
        verifyNoInteractions(relationService);
    }

    @Test
    void foundMinInfo() throws InvalidCertificateException, ModuleNotFoundException, ModuleException {
        String intygId = "found2";
        LocalDateTime receivedTime = LocalDateTime.now();
        Certificate certificate = getCertificate(intygId, receivedTime);
        certificate.setType("db");

        Optional<ItIntygInfo> optionalItIntygInfo = testee.getIntygInfo(intygId);

        assertTrue(optionalItIntygInfo.isPresent());

        verify(recipientService).listRecipients(intygId);
        verify(moduleRegistry).getModuleApi(certificate.getType(), certificate.getTypeVersion());
        verify(relationService).getChildRelations(intygId);

        ItIntygInfo intygInfo = optionalItIntygInfo.get();

        // Verify data
        assertEquals(intygId, intygInfo.getIntygId());
        assertEquals("db", intygInfo.getIntygType());
        assertEquals("1.0", intygInfo.getIntygVersion());
        assertEquals(certificate.getSignedDate(), intygInfo.getSignedDate());
        assertEquals(receivedTime, intygInfo.getReceivedDate());
        assertEquals("SigningDoctorName", intygInfo.getSignedByName());

        assertEquals("careGiverId", intygInfo.getCareGiverHsaId());
        assertEquals("careGiverName", intygInfo.getCareGiverName());
        assertEquals("careUnitId", intygInfo.getCareUnitHsaId());
        assertEquals("careUnitName", intygInfo.getCareUnitName());
        assertEquals(1, intygInfo.getNumberOfRecipients());

        assertEquals(1, intygInfo.getEvents().size());

        List<IntygInfoEvent> expectedEvents = new ArrayList<>();

        // Signed event
        IntygInfoEvent signed = new IntygInfoEvent(Source.INTYGSTJANSTEN, certificate.getSignedDate(), IntygInfoEventType.IS004);
        signed.addData("name", certificate.getSigningDoctorName());
        signed.addData("hsaId", intygInfo.getSignedByHsaId());
        expectedEvents.add(signed);

        assertThat(intygInfo.getEvents(), containsInAnyOrder(expectedEvents.toArray(new IntygInfoEvent[0])));
    }

    @Test
    void foundWithAllInfo() throws InvalidCertificateException, ModuleNotFoundException, ModuleException {
        String intygId = "found";
        LocalDateTime receivedTime = LocalDateTime.now();
        LocalDateTime sentTime = LocalDateTime.now();
        LocalDateTime revokeTime = LocalDateTime.now();
        Certificate certificate = getCertificate(intygId, receivedTime);

        List<CertificateStateHistoryEntry> states = new ArrayList<>(certificate.getStates());
        states.add(new CertificateStateHistoryEntry(LisjpEntryPoint.DEFAULT_RECIPIENT_ID, CertificateState.SENT, sentTime));
        states.add(new CertificateStateHistoryEntry("", CertificateState.CANCELLED, revokeTime));
        certificate.setStates(states);

        List<Relation> relations = new ArrayList<>();
        relations.add(new Relation("parent", intygId, RelationKod.ERSATT.value(), sentTime));
        relations.add(new Relation("parent", intygId, RelationKod.FRLANG.value(), sentTime));
        relations.add(new Relation("parent", intygId, RelationKod.KOMPLT.value(), sentTime));
        relations.add(new Relation("parent", intygId, RelationKod.KOPIA.value(), sentTime));

        when(relationService.getChildRelations(intygId)).thenReturn(relations);

        Optional<ItIntygInfo> optionalItIntygInfo = testee.getIntygInfo(intygId);

        assertTrue(optionalItIntygInfo.isPresent());

        verify(recipientService).listRecipients(intygId);
        verify(moduleRegistry).getModuleApi(certificate.getType(), certificate.getTypeVersion());
        verify(relationService).getChildRelations(intygId);

        ItIntygInfo intygInfo = optionalItIntygInfo.get();

        // Verify data
        assertEquals(intygId, intygInfo.getIntygId());
        assertEquals("lisjp", intygInfo.getIntygType());
        assertEquals("1.0", intygInfo.getIntygVersion());
        assertEquals(certificate.getSignedDate(), intygInfo.getSignedDate());
        assertEquals(receivedTime, intygInfo.getReceivedDate());
        assertEquals("SigningDoctorName", intygInfo.getSignedByName());

        assertEquals("careGiverId", intygInfo.getCareGiverHsaId());
        assertEquals("careGiverName", intygInfo.getCareGiverName());
        assertEquals("careUnitId", intygInfo.getCareUnitHsaId());
        assertEquals("careUnitName", intygInfo.getCareUnitName());
        assertEquals(1, intygInfo.getNumberOfRecipients());

        assertEquals(7, intygInfo.getEvents().size());

        List<IntygInfoEvent> expectedEvents = new ArrayList<>();

        // Signed event
        IntygInfoEvent signed = new IntygInfoEvent(Source.INTYGSTJANSTEN, certificate.getSignedDate(), IntygInfoEventType.IS004);
        signed.addData("name", certificate.getSigningDoctorName());
        signed.addData("hsaId", intygInfo.getSignedByHsaId());
        expectedEvents.add(signed);

        // Visible in MI event
        expectedEvents.add(new IntygInfoEvent(Source.INTYGSTJANSTEN, receivedTime, IntygInfoEventType.IS005));

        // Sent
        IntygInfoEvent sent = new IntygInfoEvent(Source.INTYGSTJANSTEN, sentTime, IntygInfoEventType.IS006);
        sent.addData("intygsmottagare", LisjpEntryPoint.DEFAULT_RECIPIENT_ID);
        expectedEvents.add(sent);

        // Revoke
        expectedEvents.add(new IntygInfoEvent(Source.INTYGSTJANSTEN, revokeTime, IntygInfoEventType.IS009));

        // Ersatt
        IntygInfoEvent ersatt = new IntygInfoEvent(Source.INTYGSTJANSTEN, sentTime, IntygInfoEventType.IS008);
        ersatt.addData("intygsId", "parent");
        expectedEvents.add(ersatt);

        // Förlängt
        IntygInfoEvent frlng = new IntygInfoEvent(Source.INTYGSTJANSTEN, sentTime, IntygInfoEventType.IS007);
        frlng.addData("intygsId", "parent");
        expectedEvents.add(frlng);

        // Kompletterat
        IntygInfoEvent kompl = new IntygInfoEvent(Source.INTYGSTJANSTEN, sentTime, IntygInfoEventType.IS014);
        kompl.addData("intygsId", "parent");
        expectedEvents.add(kompl);

        assertThat(intygInfo.getEvents(), containsInAnyOrder(expectedEvents.toArray(new IntygInfoEvent[0])));
    }

    @Test
    void shouldReturnResultFromDatabaseQuery() {
        when(certificateRepository.getCertificateCountForCareProvider(HSA_ID)).thenReturn(CERTIFICATE_COUNT);

        final var response = testee.getCertificateCount(HSA_ID);

        assertEquals(CERTIFICATE_COUNT, response);
    }

    private Certificate getCertificate(String intygId, LocalDateTime received) throws InvalidCertificateException, ModuleException {

        Certificate certificate = new Certificate(intygId);
        certificate.setType("lisjp");
        certificate.setTypeVersion("1.0");
        certificate.setSignedDate(LocalDateTime.now());
        certificate.setCareGiverId("careGiverId");
        certificate.setCareUnitId("careUnitId");
        certificate.setCareUnitName("careUnitName");
        certificate.setSigningDoctorName("SigningDoctorName");

        List<CertificateStateHistoryEntry> certificateStates = new ArrayList<>();

        certificateStates.add(new CertificateStateHistoryEntry("", CertificateState.RECEIVED, received));

        certificate.setStates(certificateStates);

        OriginalCertificate originalCertificate = new OriginalCertificate(received, intygId, certificate);
        certificate.setOriginalCertificate(originalCertificate);

        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setVardgivarnamn("careGiverName");
        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setVardgivare(vardgivare);
        HoSPersonal hoSPersonal = new HoSPersonal();
        hoSPersonal.setVardenhet(vardenhet);
        GrundData grundData = new GrundData();
        grundData.setSkapadAv(hoSPersonal);
        Utlatande utlatande = mock(Utlatande.class);
        when(utlatande.getGrundData()).thenReturn(grundData);

        when(certificateService.getCertificateForCare(intygId)).thenReturn(certificate);
        when(moduleApi.getUtlatandeFromXml(intygId)).thenReturn(utlatande);

        return certificate;
    }

}
