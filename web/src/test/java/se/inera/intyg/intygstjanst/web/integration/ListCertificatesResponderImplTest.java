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
import static se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum.INFO;
import static se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum.OK;

import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.ifv.insuranceprocess.healthreporting.listcertificates.rivtabp20.v1.ListCertificatesResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.listcertificatesresponder.v1.ListCertificatesRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.listcertificatesresponder.v1.ListCertificatesResponseType;
import se.inera.intyg.common.support.integration.module.exception.MissingConsentException;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.service.CertificateService;


/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class ListCertificatesResponderImplTest {

    @Mock
    private CertificateService certificateService = mock(CertificateService.class);

    @InjectMocks
    private ListCertificatesResponderInterface responder = new ListCertificatesResponderImpl();

    @Test
    public void listCertificatesWithNoCertificates() throws Exception {
        Personnummer civicRegistrationNumber = new Personnummer("19350108-1234");
        List<String> certificateTypes = Collections.singletonList("fk7263");
        LocalDate fromDate = new LocalDate(2000, 1, 1);
        LocalDate toDate = new LocalDate(2020, 12, 12);

        List<Certificate> result = Collections.emptyList();

        when(certificateService.listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate)).thenReturn(result);

        ListCertificatesRequestType parameters = createListCertificatesRequest(civicRegistrationNumber, certificateTypes, fromDate, toDate);

        ListCertificatesResponseType response = responder.listCertificates(null, parameters);

        verify(certificateService).listCertificatesForCitizen(civicRegistrationNumber, certificateTypes, fromDate, toDate);

        assertEquals(0, response.getMeta().size());
        assertEquals(OK, response.getResult().getResultCode());
    }

    @Test
    public void listCertificatesWithoutConsent() throws Exception {
        when(certificateService.listCertificatesForCitizen(any(Personnummer.class), Matchers.<List<String>>any(), any(LocalDate.class), any(LocalDate.class))).thenThrow(new MissingConsentException(null));

        List<String> types = Collections.emptyList();
        ListCertificatesRequestType parameters = createListCertificatesRequest(new Personnummer("12-3"), types, null, null);

        ListCertificatesResponseType response = responder.listCertificates(null, parameters);

        assertEquals(0, response.getMeta().size());
        assertEquals(INFO, response.getResult().getResultCode());
        assertEquals("NOCONSENT", response.getResult().getInfoText());
    }

    private ListCertificatesRequestType createListCertificatesRequest(Personnummer civicRegistrationNumber, List<String> types, LocalDate fromDate, LocalDate toDate) {
        ListCertificatesRequestType parameters = new ListCertificatesRequestType();
        parameters.setNationalIdentityNumber(civicRegistrationNumber.getPersonnummer());

        for (String type: types) {
            parameters.getCertificateType().add(type);
        }

        parameters.setFromDate(fromDate);
        parameters.setToDate(toDate);

        return parameters;
    }
}