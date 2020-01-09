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
package se.inera.intyg.intygstjanst.web.integration.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.StatusKod;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCitizen.v3.ListCertificatesForCitizenResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCitizen.v3.ListCertificatesForCitizenResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCitizen.v3.ListCertificatesForCitizenType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Part;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.IntygsStatus;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

@RunWith(MockitoJUnitRunner.class)
public class ListCertificatesForCitizenResponderImplTest {

    private static final String FKASSA_RECIPIENT_ID = "FKASSA";
    private static final String OTHER_RECIPIENT_ID = "someotherID";
    private static final String MINA_INTYG_RECIPIENT_ID = "INVANA";
    private static final String INTYG_TYPE_VERSION = "1.0";

    @Mock
    private CertificateService certificateService;

    @Mock
    private MonitoringLogService monitoringLogService;

    @Mock
    private IntygModuleRegistryImpl moduleRegistry;

    @Mock
    private ModuleEntryPoint moduleEntryPoint;

    @Mock
    private ModuleApi moduleApi;

    @InjectMocks
    private ListCertificatesForCitizenResponderInterface responder = new ListCertificatesForCitizenResponderImpl();

    @Before
    public void setup() throws ModuleNotFoundException, ModuleException {
        when(moduleRegistry.getModuleApi(or(isNull(), anyString()), or(isNull(), anyString()))).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromXml(or(isNull(), anyString()))).thenReturn(mock(Utlatande.class));
        when(moduleApi.getIntygFromUtlatande(or(isNull(), any(Utlatande.class)))).thenReturn(new Intyg());
    }

    @Test
    public void listCertificatesWithNoCertificates() throws Exception {
        Personnummer civicRegistrationNumber = createPnr("19350108-1234");
        List<String> certificateTypes = Collections.singletonList("fk7263");
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2020, 12, 12);

        List<Certificate> result = Collections.emptyList();

        when(certificateService.listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate)).thenReturn(result);

        ListCertificatesForCitizenType parameters = createListCertificatesRequest(civicRegistrationNumber, certificateTypes, fromDate,
            toDate, false);

        ListCertificatesForCitizenResponseType response = responder.listCertificatesForCitizen(null, parameters);

        verify(certificateService).listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate);

        assertEquals(0, response.getIntygsLista().getIntyg().size());
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
    }

    @Test
    public void listCertificatesArkiveradFalse() throws Exception {
        Personnummer civicRegistrationNumber = createPnr("19350108-1234");
        List<String> certificateTypes = Collections.singletonList("fk7263");
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2020, 12, 12);

        Certificate certificate = new Certificate();
        certificate.setTypeVersion(INTYG_TYPE_VERSION);
        certificate.addState(new CertificateStateHistoryEntry("MI", CertificateState.DELETED, LocalDateTime.now().minusDays(4)));
        Certificate certificate2 = new Certificate();
        certificate2.setTypeVersion(INTYG_TYPE_VERSION);
        List<Certificate> result = Arrays.asList(certificate, certificate2);

        when(certificateService.listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate)).thenReturn(result);

        ListCertificatesForCitizenType parameters = createListCertificatesRequest(civicRegistrationNumber, certificateTypes, fromDate,
            toDate, false);

        ListCertificatesForCitizenResponseType response = responder.listCertificatesForCitizen(null, parameters);

        verify(certificateService).listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate);
        verify(moduleApi).getIntygFromUtlatande(any(Utlatande.class));
        verify(moduleApi).getUtlatandeFromXml(or(isNull(), anyString()));

        assertEquals(1, response.getIntygsLista().getIntyg().size());
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
    }

    @Test
    public void listCertificatesArkiveradTrue() throws Exception {
        Personnummer civicRegistrationNumber = createPnr("19350108-1234");
        List<String> certificateTypes = Collections.singletonList("fk7263");
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2020, 12, 12);

        Certificate certificate = new Certificate();
        certificate.setTypeVersion(INTYG_TYPE_VERSION);
        certificate.addState(new CertificateStateHistoryEntry("INVANA", CertificateState.DELETED, LocalDateTime.now().minusDays(4)));
        Certificate certificate2 = new Certificate();
        certificate2.setTypeVersion(INTYG_TYPE_VERSION);
        List<Certificate> result = Arrays.asList(certificate, certificate2);

        when(certificateService.listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate)).thenReturn(result);

        ListCertificatesForCitizenType parameters = createListCertificatesRequest(civicRegistrationNumber, certificateTypes, fromDate,
            toDate, true);

        ListCertificatesForCitizenResponseType response = responder.listCertificatesForCitizen(null, parameters);

        verify(certificateService).listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate);
        verify(moduleApi).getIntygFromUtlatande(any(Utlatande.class));
        verify(moduleApi).getUtlatandeFromXml(or(isNull(), anyString()));

        assertEquals(1, response.getIntygsLista().getIntyg().size());
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
    }

    @Test
    public void listCertificatesSetsStatuses() throws Exception {
        final LocalDateTime statusTimestamp = LocalDateTime.now();
        Personnummer civicRegistrationNumber = createPnr("19350108-1234");
        List<String> certificateTypes = Collections.singletonList("fk7263");
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2020, 12, 12);

        Certificate certificate = new Certificate();
        certificate.setTypeVersion(INTYG_TYPE_VERSION);
        certificate.setStates(Arrays.asList(new CertificateStateHistoryEntry("FKASSA", CertificateState.SENT, statusTimestamp)));
        List<Certificate> result = Arrays.asList(certificate);

        when(certificateService.listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate)).thenReturn(result);

        ListCertificatesForCitizenType parameters = createListCertificatesRequest(civicRegistrationNumber, certificateTypes, fromDate,
            toDate, false);

        ListCertificatesForCitizenResponseType response = responder.listCertificatesForCitizen(null, parameters);

        assertEquals(1, response.getIntygsLista().getIntyg().size());
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
        assertEquals(1, response.getIntygsLista().getIntyg().get(0).getStatus().size());
        assertEquals("FKASSA", response.getIntygsLista().getIntyg().get(0).getStatus().get(0).getPart().getCode());
        assertNotNull(response.getIntygsLista().getIntyg().get(0).getStatus().get(0).getPart().getCodeSystem());
        assertEquals(statusTimestamp, response.getIntygsLista().getIntyg().get(0).getStatus().get(0).getTidpunkt());
        assertEquals(StatusKod.SENTTO.name(), response.getIntygsLista().getIntyg().get(0).getStatus().get(0).getStatus().getCode());
        assertNotNull(response.getIntygsLista().getIntyg().get(0).getStatus().get(0).getStatus().getCodeSystem());
    }

    @Test
    public void statusesAreFilteredForFk() {
        // Given
        LocalDateTime firstStatusSaved = LocalDateTime.of(2017, 4, 7, 15, 15);
        LocalDateTime[] timestamps = {
            firstStatusSaved,
            firstStatusSaved.plusHours(1),
            firstStatusSaved.plusHours(2),
            firstStatusSaved.plusHours(3),
            firstStatusSaved.plusHours(4),
        };
        Personnummer pnr = createPnr("19121212-1212");
        List<String> certificateTypes = Collections.singletonList("fk7263");
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2020, 12, 12);

        Certificate certificate = new Certificate();
        certificate.setTypeVersion(INTYG_TYPE_VERSION);
        certificate.setStates(Arrays.asList(
            new CertificateStateHistoryEntry(FKASSA_RECIPIENT_ID, CertificateState.SENT, timestamps[0]),
            new CertificateStateHistoryEntry(OTHER_RECIPIENT_ID, CertificateState.SENT, timestamps[1]),
            new CertificateStateHistoryEntry(OTHER_RECIPIENT_ID, CertificateState.DELETED, timestamps[2]),
            new CertificateStateHistoryEntry(OTHER_RECIPIENT_ID, CertificateState.RESTORED, timestamps[3]),
            new CertificateStateHistoryEntry(OTHER_RECIPIENT_ID, CertificateState.CANCELLED, timestamps[4])));
        List<Certificate> result = Arrays.asList(certificate);

        when(certificateService.listCertificatesForCitizen(pnr, certificateTypes, fromDate, toDate)).thenReturn(result);

        ListCertificatesForCitizenType parameters = createListCertificatesRequest(pnr, certificateTypes, fromDate,
            toDate, false, FKASSA_RECIPIENT_ID);

        // When
        ListCertificatesForCitizenResponseType response = responder.listCertificatesForCitizen(null, parameters);

        // Then
        assertEquals(1, response.getIntygsLista().getIntyg().size());
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
        assertEquals(2, response.getIntygsLista().getIntyg().get(0).getStatus().size());

        IntygsStatus status = response.getIntygsLista().getIntyg().get(0).getStatus().get(0);
        assertEquals(OTHER_RECIPIENT_ID, status.getPart().getCode());
        assertNotNull(status.getPart().getCodeSystem());
        assertEquals(timestamps[4], status.getTidpunkt());
        assertEquals(StatusKod.CANCEL.name(), status.getStatus().getCode());
        assertNotNull(status.getStatus().getCodeSystem());

        status = response.getIntygsLista().getIntyg().get(0).getStatus().get(1);
        assertEquals(FKASSA_RECIPIENT_ID, status.getPart().getCode());
        assertNotNull(status.getPart().getCodeSystem());
        assertEquals(timestamps[0], status.getTidpunkt());
        assertEquals(StatusKod.SENTTO.name(), status.getStatus().getCode());
        assertNotNull(status.getStatus().getCodeSystem());
    }

    @Test
    public void statusesAreNotFilteredForMinaIntyg() {
        // Given
        LocalDateTime firstStatusSaved = LocalDateTime.of(2017, 4, 7, 15, 15);
        LocalDateTime[] timestamps = {
            firstStatusSaved,
            firstStatusSaved.plusHours(1),
            firstStatusSaved.plusHours(2),
            firstStatusSaved.plusHours(3),
            firstStatusSaved.plusHours(4),
        };
        Personnummer pnr = createPnr("19121212-1212");
        List<String> certificateTypes = Collections.singletonList("fk7263");
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2020, 12, 12);

        Certificate certificate = new Certificate();
        certificate.setTypeVersion(INTYG_TYPE_VERSION);
        certificate.setStates(Arrays.asList(
            new CertificateStateHistoryEntry(FKASSA_RECIPIENT_ID, CertificateState.SENT, timestamps[0]),
            new CertificateStateHistoryEntry(OTHER_RECIPIENT_ID, CertificateState.SENT, timestamps[1]),
            new CertificateStateHistoryEntry(OTHER_RECIPIENT_ID, CertificateState.DELETED, timestamps[2]),
            new CertificateStateHistoryEntry(OTHER_RECIPIENT_ID, CertificateState.RESTORED, timestamps[3]),
            new CertificateStateHistoryEntry(OTHER_RECIPIENT_ID, CertificateState.CANCELLED, timestamps[4])));
        List<Certificate> result = Arrays.asList(certificate);

        when(certificateService.listCertificatesForCitizen(pnr, certificateTypes, fromDate, toDate)).thenReturn(result);

        ListCertificatesForCitizenType parameters = createListCertificatesRequest(pnr, certificateTypes, fromDate,
            toDate, false, MINA_INTYG_RECIPIENT_ID);

        // When
        ListCertificatesForCitizenResponseType response = responder.listCertificatesForCitizen(null, parameters);

        // Then
        assertEquals(1, response.getIntygsLista().getIntyg().size());
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
        assertEquals(5, response.getIntygsLista().getIntyg().get(0).getStatus().size());

        IntygsStatus status = response.getIntygsLista().getIntyg().get(0).getStatus().get(0);
        assertEquals(OTHER_RECIPIENT_ID, status.getPart().getCode());
        assertNotNull(status.getPart().getCodeSystem());
        assertEquals(timestamps[4], status.getTidpunkt());
        assertEquals(StatusKod.CANCEL.name(), status.getStatus().getCode());
        assertNotNull(status.getStatus().getCodeSystem());

        status = response.getIntygsLista().getIntyg().get(0).getStatus().get(1);
        assertEquals(OTHER_RECIPIENT_ID, status.getPart().getCode());
        assertNotNull(status.getPart().getCodeSystem());
        assertEquals(timestamps[3], status.getTidpunkt());
        assertEquals(StatusKod.RESTOR.name(), status.getStatus().getCode());
        assertNotNull(status.getStatus().getCodeSystem());

        status = response.getIntygsLista().getIntyg().get(0).getStatus().get(2);
        assertEquals(OTHER_RECIPIENT_ID, status.getPart().getCode());
        assertNotNull(status.getPart().getCodeSystem());
        assertEquals(timestamps[2], status.getTidpunkt());
        assertEquals(StatusKod.DELETE.name(), status.getStatus().getCode());
        assertNotNull(status.getStatus().getCodeSystem());

        status = response.getIntygsLista().getIntyg().get(0).getStatus().get(3);
        assertEquals(OTHER_RECIPIENT_ID, status.getPart().getCode());
        assertNotNull(status.getPart().getCodeSystem());
        assertEquals(timestamps[1], status.getTidpunkt());
        assertEquals(StatusKod.SENTTO.name(), status.getStatus().getCode());
        assertNotNull(status.getStatus().getCodeSystem());

        status = response.getIntygsLista().getIntyg().get(0).getStatus().get(4);
        assertEquals(FKASSA_RECIPIENT_ID, status.getPart().getCode());
        assertNotNull(status.getPart().getCodeSystem());
        assertEquals(timestamps[0], status.getTidpunkt());
        assertEquals(StatusKod.SENTTO.name(), status.getStatus().getCode());
        assertNotNull(status.getStatus().getCodeSystem());
    }

    @Test
    public void testDbDoiAreExcluded() throws Exception {
        Personnummer civicRegistrationNumber = createPnr("19350108-1234");
        List<String> certificateTypes = Collections.emptyList();
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.of(2020, 12, 12);

        Certificate certificate = new Certificate();
        certificate.setType(Fk7263EntryPoint.MODULE_ID);
        certificate.setTypeVersion(INTYG_TYPE_VERSION);
        Certificate certificate2 = new Certificate();
        certificate2.setType(Fk7263EntryPoint.MODULE_ID);
        certificate2.setTypeVersion(INTYG_TYPE_VERSION);
        Certificate certificate3 = new Certificate();
        certificate3.setType(DbModuleEntryPoint.MODULE_ID);
        certificate3.setTypeVersion(INTYG_TYPE_VERSION);
        Certificate certificate4 = new Certificate();
        certificate4.setType(DoiModuleEntryPoint.MODULE_ID);
        certificate4.setTypeVersion(INTYG_TYPE_VERSION);
        List<Certificate> result = Arrays.asList(certificate, certificate3, certificate4, certificate2);

        when(certificateService.listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate)).thenReturn(result);

        ListCertificatesForCitizenType parameters = createListCertificatesRequest(civicRegistrationNumber, certificateTypes, fromDate,
            toDate, false);

        ListCertificatesForCitizenResponseType response = responder.listCertificatesForCitizen(null, parameters);

        verify(certificateService).listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate);

        assertEquals(2, response.getIntygsLista().getIntyg().size());
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
    }

    private ListCertificatesForCitizenType createListCertificatesRequest(Personnummer civicRegistrationNumber, List<String> types,
        LocalDate fromDate, LocalDate toDate, boolean arkiverad) {
        return createListCertificatesRequest(civicRegistrationNumber, types, fromDate, toDate, arkiverad, FKASSA_RECIPIENT_ID);
    }

    private ListCertificatesForCitizenType createListCertificatesRequest(Personnummer civicRegistrationNumber, List<String> types,
        LocalDate fromDate, LocalDate toDate, boolean arkiverad, String partId) {
        ListCertificatesForCitizenType parameters = new ListCertificatesForCitizenType();
        parameters.setPersonId(new PersonId());
        parameters.getPersonId().setExtension(civicRegistrationNumber.getPersonnummer());

        for (String type : types) {
            TypAvIntyg typAvIntyg = new TypAvIntyg();
            typAvIntyg.setCode(type);
            parameters.getIntygTyp().add(typAvIntyg);
        }

        parameters.setFromDatum(fromDate);
        parameters.setTomDatum(toDate);
        parameters.setArkiverade(arkiverad);
        Part part = new Part();
        part.setCode(partId);
        parameters.setPart(part);

        return parameters;
    }

    private Personnummer createPnr(String pnr) {
        return Personnummer.createPersonnummer(pnr)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer"));
    }

}
