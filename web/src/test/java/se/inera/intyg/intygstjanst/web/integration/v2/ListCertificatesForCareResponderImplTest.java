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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v2.ListCertificatesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v2.ListCertificatesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v2.ListCertificatesForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v2.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;

@RunWith(MockitoJUnitRunner.class)
public class ListCertificatesForCareResponderImplTest {

    @Mock
    private CertificateService certificateService;

    @Mock
    private MonitoringLogService monitoringLogService;

    @Mock
    private IntygModuleRegistryImpl moduleRegistry;

    @Mock
    private ModuleApi moduleApi;

    @InjectMocks
    private ListCertificatesForCareResponderInterface responder = new ListCertificatesForCareResponderImpl();

    @Before
    public void setup() throws ModuleNotFoundException, ModuleException {
        when(moduleRegistry.getModuleApi(anyString())).thenReturn(moduleApi);
        when(moduleApi.getIntygFromCertificateHolder(any(CertificateHolder.class))).thenReturn(new Intyg());
    }

    @Test
    public void listCertificatesWithNoCertificates() throws Exception {
        Personnummer civicRegistrationNumber = new Personnummer("19350108-1234");
        List<String> careUnit = Collections.singletonList("enhet");
        List<Certificate> result = Collections.emptyList();

        when(certificateService.listCertificatesForCare(civicRegistrationNumber, careUnit)).thenReturn(result);

        ListCertificatesForCareType parameters = createListCertificatesRequest(civicRegistrationNumber, createHsaId("vardgivare"),
                createHsaId("enhet"));

        ListCertificatesForCareResponseType response = responder.listCertificatesForCare(null, parameters);

        verify(certificateService).listCertificatesForCare(civicRegistrationNumber, careUnit);

        assertEquals(0, response.getIntygsLista().getIntyg().size());
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
    }

    @Test
    public void listCertificates() throws Exception {
        final String document1 = "document1";
        final String document2 = "document2";

        Personnummer civicRegistrationNumber = new Personnummer("19350108-1234");
        List<String> careUnit = Collections.singletonList("enhet");

        Certificate certificate1 = new Certificate();
        certificate1.setDocument(document1);
        certificate1.setDeletedByCareGiver(Boolean.FALSE);
        Certificate certificate2 = new Certificate();
        certificate2.setDocument(document2);
        certificate2.setDeletedByCareGiver(Boolean.FALSE);

        List<Certificate> result = Arrays.asList(certificate1, certificate2);

        when(certificateService.listCertificatesForCare(civicRegistrationNumber, careUnit)).thenReturn(result);

        ListCertificatesForCareType parameters = createListCertificatesRequest(civicRegistrationNumber, createHsaId("vardgivare"),
                createHsaId("enhet"));

        ListCertificatesForCareResponseType response = responder.listCertificatesForCare(null, parameters);

        verify(certificateService).listCertificatesForCare(civicRegistrationNumber, careUnit);

        assertEquals(2, response.getIntygsLista().getIntyg().size());
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
    }

    @Test
    public void listCertificatesDoesNotListCertificatesDeletedByCaregiver() throws Exception {
        final String deletedDocument = "deleted document";
        final String document = "document";

        Personnummer civicRegistrationNumber = new Personnummer("19350108-1234");
        List<String> careUnit = Collections.singletonList("enhet");

        Certificate certificate = new Certificate();
        certificate.setDocument(deletedDocument);
        certificate.setDeletedByCareGiver(Boolean.TRUE);
        Certificate certificate2 = new Certificate();
        certificate2.setDocument(document);
        certificate2.setDeletedByCareGiver(Boolean.FALSE);

        List<Certificate> result = Arrays.asList(certificate, certificate2);

        when(certificateService.listCertificatesForCare(civicRegistrationNumber, careUnit)).thenReturn(result);

        ListCertificatesForCareType parameters = createListCertificatesRequest(civicRegistrationNumber, createHsaId("vardgivare"),
                createHsaId("enhet"));

        ListCertificatesForCareResponseType response = responder.listCertificatesForCare(null, parameters);

        verify(certificateService).listCertificatesForCare(civicRegistrationNumber, careUnit);
        ArgumentCaptor<CertificateHolder> certificateHolderCaptor = ArgumentCaptor.forClass(CertificateHolder.class);
        verify(moduleApi).getIntygFromCertificateHolder(certificateHolderCaptor.capture());

        // We only return Intyg that are not deletedByCaregiver
        assertEquals(document, certificateHolderCaptor.getValue().getDocument());
        assertEquals(1, response.getIntygsLista().getIntyg().size());
        assertEquals(ResultCodeType.OK, response.getResult().getResultCode());
    }

    private HsaId createHsaId(String id) {
        HsaId hsaId = new HsaId();
        hsaId.setExtension(id);
        hsaId.setRoot("root");
        return hsaId;
    }

    private ListCertificatesForCareType createListCertificatesRequest(Personnummer civicRegistrationNumber, HsaId vardgivarId, HsaId enhet) {
        ListCertificatesForCareType parameters = new ListCertificatesForCareType();
        parameters.setPersonId(new PersonId());
        parameters.getPersonId().setExtension(civicRegistrationNumber.getPersonnummer());
        parameters.setVardgivarId(vardgivarId);
        parameters.getEnhetsId().add(enhet);
        return parameters;
    }
}