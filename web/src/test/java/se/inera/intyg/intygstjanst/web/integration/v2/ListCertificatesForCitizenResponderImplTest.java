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

package se.inera.intyg.intygstjanst.web.integration.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.integration.module.exception.MissingConsentException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.StatusKod;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.riv.clinicalprocess.healthcond.certificate.listCertificatesForCitizen.v2.*;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.TypAvIntyg;
import se.riv.clinicalprocess.healthcond.certificate.v2.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;

@RunWith(MockitoJUnitRunner.class)
public class ListCertificatesForCitizenResponderImplTest {

    @Mock
    private CertificateService certificateService;

    @Mock
    private MonitoringLogService monitoringLogService;

    @Mock
    private IntygModuleRegistryImpl moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @InjectMocks
    private ListCertificatesForCitizenResponderInterface responder = new ListCertificatesForCitizenResponderImpl();

    @Before
    public void setup() throws ModuleNotFoundException, ModuleException {
        when(moduleRegistry.getModuleApi(anyString())).thenReturn(moduleApi);
        when(moduleApi.getIntygFromCertificateHolder(any(CertificateHolder.class))).thenReturn(new Intyg());
    }

    @Test
    public void listCertificatesWithNoCertificates() throws Exception {
        Personnummer civicRegistrationNumber = new Personnummer("19350108-1234");
        List<String> certificateTypes = Collections.singletonList("fk7263");
        LocalDate fromDate = new LocalDate(2000, 1, 1);
        LocalDate toDate = new LocalDate(2020, 12, 12);

        List<Certificate> result = Collections.emptyList();

        when(certificateService.listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate)).thenReturn(result);

        ListCertificatesForCitizenType parameters = createListCertificatesRequest(civicRegistrationNumber, certificateTypes, fromDate, toDate, false);

        ListCertificatesForCitizenResponseType response = responder.listCertificatesForCitizen(null, parameters);

        verify(certificateService).listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate);

        assertEquals(0, response.getIntygsLista().getIntyg().size());
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
    }

    @Test
    public void listCertificatesWithoutConsent() throws Exception {
        when(certificateService.listCertificatesForCitizen(any(Personnummer.class), Matchers.<List<String>> any(), any(LocalDate.class),
                any(LocalDate.class))).thenThrow(new MissingConsentException(null));

        List<String> types = Collections.emptyList();
        ListCertificatesForCitizenType parameters = createListCertificatesRequest(new Personnummer("12-3"), types, null, null, false);

        ListCertificatesForCitizenResponseType response = responder.listCertificatesForCitizen(null, parameters);

        assertEquals(0, response.getIntygsLista().getIntyg().size());
        assertEquals(ResultCodeType.INFO, response.getResult().getResultCode());
        assertEquals("NOCONSENT", response.getResult().getResultText());
    }

    @Test
    public void listCertificatesArkiveradFalse() throws Exception {
        final String deletedDocument = "deleted document";
        final String document = "document";
        Personnummer civicRegistrationNumber = new Personnummer("19350108-1234");
        List<String> certificateTypes = Collections.singletonList("fk7263");
        LocalDate fromDate = new LocalDate(2000, 1, 1);
        LocalDate toDate = new LocalDate(2020, 12, 12);

        Certificate certificate = new Certificate();
        certificate.setDocument(deletedDocument);
        certificate.setDeleted(Boolean.TRUE);
        Certificate certificate2 = new Certificate();
        certificate2.setDocument(document);
        certificate2.setDeleted(Boolean.FALSE);
        List<Certificate> result = Arrays.asList(certificate, certificate2);

        when(certificateService.listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate)).thenReturn(result);

        ListCertificatesForCitizenType parameters = createListCertificatesRequest(civicRegistrationNumber, certificateTypes, fromDate, toDate, false);

        ListCertificatesForCitizenResponseType response = responder.listCertificatesForCitizen(null, parameters);

        verify(certificateService).listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate);
        ArgumentCaptor<CertificateHolder> certificateHolderCaptor = ArgumentCaptor.forClass(CertificateHolder.class);
        verify(moduleApi).getIntygFromCertificateHolder(certificateHolderCaptor.capture());

        assertEquals(document, certificateHolderCaptor.getValue().getDocument());
        assertEquals(1, response.getIntygsLista().getIntyg().size());
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
    }

    @Test
    public void listCertificatesArkiveradTrue() throws Exception {
        final String deletedDocument = "deleted document";
        final String document = "document";
        Personnummer civicRegistrationNumber = new Personnummer("19350108-1234");
        List<String> certificateTypes = Collections.singletonList("fk7263");
        LocalDate fromDate = new LocalDate(2000, 1, 1);
        LocalDate toDate = new LocalDate(2020, 12, 12);

        Certificate certificate = new Certificate();
        certificate.setDocument(deletedDocument);
        certificate.setDeleted(Boolean.TRUE);
        Certificate certificate2 = new Certificate();
        certificate2.setDocument(document);
        certificate2.setDeleted(Boolean.FALSE);
        List<Certificate> result = Arrays.asList(certificate, certificate2);

        when(certificateService.listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate)).thenReturn(result);

        ListCertificatesForCitizenType parameters = createListCertificatesRequest(civicRegistrationNumber, certificateTypes, fromDate, toDate, true);

        ListCertificatesForCitizenResponseType response = responder.listCertificatesForCitizen(null, parameters);

        verify(certificateService).listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate);
        ArgumentCaptor<CertificateHolder> certificateHolderCaptor = ArgumentCaptor.forClass(CertificateHolder.class);
        verify(moduleApi).getIntygFromCertificateHolder(certificateHolderCaptor.capture());

        assertEquals(deletedDocument, certificateHolderCaptor.getValue().getDocument());
        assertEquals(1, response.getIntygsLista().getIntyg().size());
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
    }

    @Test
    public void listCertificatesSetsStatuses() throws Exception {
        final LocalDateTime statusTimestamp = LocalDateTime.now();
        Personnummer civicRegistrationNumber = new Personnummer("19350108-1234");
        List<String> certificateTypes = Collections.singletonList("fk7263");
        LocalDate fromDate = new LocalDate(2000, 1, 1);
        LocalDate toDate = new LocalDate(2020, 12, 12);

        Certificate certificate = new Certificate();
        certificate.setDocument("document");
        certificate.setDeleted(Boolean.FALSE);
        CertificateStateHistoryEntry state = new CertificateStateHistoryEntry();
        state.setState(CertificateState.SENT);
        state.setTarget("FK");
        state.setTimestamp(statusTimestamp);
        certificate.setStates(Arrays.asList(state));
        List<Certificate> result = Arrays.asList(certificate);

        when(certificateService.listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate)).thenReturn(result);

        ListCertificatesForCitizenType parameters = createListCertificatesRequest(civicRegistrationNumber, certificateTypes, fromDate, toDate, false);

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

    private ListCertificatesForCitizenType createListCertificatesRequest(Personnummer civicRegistrationNumber, List<String> types, LocalDate fromDate,
            LocalDate toDate, boolean arkiverad) {
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

        return parameters;
    }
}