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
package se.inera.intyg.intygstjanst.web.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType.INFO;
import static se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType.OK;

import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenType;
import se.inera.intyg.common.support.integration.module.exception.MissingConsentException;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;


/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class ListCertificatesForCitizenResponderImplTest {

    @Mock
    private CertificateService certificateService = mock(CertificateService.class);

    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private ListCertificatesForCitizenResponderInterface responder = new ListCertificatesForCitizenResponderImpl();

    @Test
    public void listCertificatesWithNoCertificates() throws Exception {
        Personnummer civicRegistrationNumber = new Personnummer("19350108-1234");
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
    public void listCertificatesWithoutConsent() throws Exception {
        when(certificateService.listCertificatesForCitizen(any(Personnummer.class), Matchers.<List<String>>any(), any(LocalDate.class), any(LocalDate.class))).thenThrow(new MissingConsentException(null));

        List<String> types = Collections.emptyList();
        ListCertificatesForCitizenType parameters = createListCertificatesRequest(new Personnummer("12-3"), types, null, null);

        ListCertificatesForCitizenResponseType response = responder.listCertificatesForCitizen(null, parameters);

        assertEquals(0, response.getMeta().size());
        assertEquals(INFO, response.getResult().getResultCode());
        assertEquals("NOCONSENT", response.getResult().getResultText());
    }

    private ListCertificatesForCitizenType createListCertificatesRequest(Personnummer civicRegistrationNumber, List<String> types, LocalDate fromDate, LocalDate toDate) {
        ListCertificatesForCitizenType parameters = new ListCertificatesForCitizenType();
        parameters.setPersonId(civicRegistrationNumber.getPersonnummer());

        for (String type: types) {
            parameters.getUtlatandeTyp().add(type);
        }

        parameters.setFranDatum(fromDate);
        parameters.setTillDatum(toDate);

        return parameters;
    }
}