/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.intyg.insuranceprocess.healthreporting.getconsent.rivtabp20.v1.GetConsentResponderInterface;
import se.inera.intyg.insuranceprocess.healthreporting.getconsentresponder.v1.GetConsentRequestType;
import se.inera.intyg.insuranceprocess.healthreporting.getconsentresponder.v1.GetConsentResponseType;
import se.inera.intyg.intygstjanst.web.service.ConsentService;
import se.inera.intyg.schemas.contract.Personnummer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class GetConsentResponderImplTest {

    private final String personId = "20121212-1212";

    private GetConsentRequestType request;

    private Personnummer personnummer;

    @Mock
    private ConsentService consentService = mock(ConsentService.class);

    @InjectMocks
    private GetConsentResponderInterface responder = new GetConsentResponderImpl();

    @Before
    public void setup() {
        request = createRequest(personId);
        personnummer = createPnr(personId);
    }

    @Test
    public void consentServiceIsCalledWithPersonnummer() {
        responder.getConsent(null, request);
        verify(consentService).isConsent(personnummer);
    }

    @Test
    public void consentServiceReturnsNoConsent() {
        when(consentService.isConsent(personnummer)).thenReturn(false);
        GetConsentResponseType consent = responder.getConsent(null, request);
        assertFalse(consent.isConsentGiven());
        assertEquals(ResultCodeEnum.OK, consent.getResult().getResultCode());
    }

    @Test
    public void consentServiceRetunsConsent() {
        when(consentService.isConsent(personnummer)).thenReturn(true);
        GetConsentResponseType consent = responder.getConsent(null, request);
        assertTrue(consent.isConsentGiven());
        assertEquals(ResultCodeEnum.OK, consent.getResult().getResultCode());
    }

    private GetConsentRequestType createRequest(String id) {
        GetConsentRequestType parameters = new GetConsentRequestType();
        parameters.setPersonnummer(id);
        return parameters;
    }

    private Personnummer createPnr(String pnr) {
        return Personnummer.createPersonnummer(pnr)
                .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer " + pnr));
    }
}
