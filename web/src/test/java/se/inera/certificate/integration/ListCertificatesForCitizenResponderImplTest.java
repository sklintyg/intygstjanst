/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Certificate (http://code.google.com/p/inera-certificate).
 *
 * Inera Certificate is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Certificate is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.certificate.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType.INFO;
import static se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType.OK;

import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenType;
import se.inera.certificate.exception.ClientException;
import se.inera.certificate.exception.MissingConsentException;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.service.CertificateService;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class ListCertificatesForCitizenResponderImplTest {

    @Mock
    private CertificateService certificateService = mock(CertificateService.class);

    @InjectMocks
    private ListCertificatesForCitizenResponderInterface responder = new ListCertificatesForCitizenResponderImpl();

    @Test
    public void listCertificatesWithNoCertificates() throws ClientException {
        String civicRegistrationNumber = "19350108-1234";
        List<String> certificateTypes = Collections.singletonList("fk7263");
        LocalDate fromDate = new LocalDate(2000, 1, 1);
        LocalDate toDate = new LocalDate(2020, 12, 12);

        List<Certificate> result = Collections.emptyList();

        when(certificateService.listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate)).thenReturn(result);

        ListCertificatesForCitizenType parameters = createListCertificatesRequest(civicRegistrationNumber, certificateTypes, fromDate, toDate);

        ListCertificatesForCitizenResponseType response = responder.listCertificatesForCitizen(null, parameters);

        verify(certificateService).listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate);

        assertEquals(0, response.getMeta().size());
        assertEquals(OK, response.getResult().getResultCode());
    }

    @Test
    public void listCertificatesWithoutConsent() throws ClientException {
        when(certificateService.listCertificatesForCitizen(anyString(), Matchers.<List<String>>any(), any(LocalDate.class), any(LocalDate.class))).thenThrow(new MissingConsentException(""));

        List<String> types = Collections.emptyList();
        ListCertificatesForCitizenType parameters = createListCertificatesRequest("12-3", types, null, null);

        ListCertificatesForCitizenResponseType response = responder.listCertificatesForCitizen(null, parameters);

        assertEquals(0, response.getMeta().size());
        assertEquals(INFO, response.getResult().getResultCode());
        assertEquals("NOCONSENT", response.getResult().getResultText());
    }

    private ListCertificatesForCitizenType createListCertificatesRequest(String civicRegistrationNumber, List<String> types, LocalDate fromDate, LocalDate toDate) {
        ListCertificatesForCitizenType parameters = new ListCertificatesForCitizenType();
        parameters.setNationalIdentityNumber(civicRegistrationNumber);

        for (String type: types) {
            parameters.getCertificateType().add(type);
        }

        parameters.setFromDate(fromDate);
        parameters.setToDate(toDate);

        return parameters;
    }
}