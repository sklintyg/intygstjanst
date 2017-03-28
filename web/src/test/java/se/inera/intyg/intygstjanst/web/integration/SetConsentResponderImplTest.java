/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.intyg.insuranceprocess.healthreporting.setconsent.rivtabp20.v1.SetConsentResponderInterface;
import se.inera.intyg.insuranceprocess.healthreporting.setconsentresponder.v1.SetConsentRequestType;
import se.inera.intyg.insuranceprocess.healthreporting.setconsentresponder.v1.SetConsentResponseType;
import se.inera.intyg.intygstjanst.web.service.ConsentService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.schemas.contract.Personnummer;

@RunWith(MockitoJUnitRunner.class)
public class SetConsentResponderImplTest {

    @Mock
    private ConsentService consentService = mock(ConsentService.class);

    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private SetConsentResponderInterface responder = new SetConsentResponderImpl();

    @Test
    public void consentServiceIsCalledWithPersonnummerAndConsentGiven() {
        responder.setConsent(null, createRequest("12345678-1234", false));
        responder.setConsent(null, createRequest("12345678-1234", true));
        verify(consentService).setConsent(new Personnummer("12345678-1234"), false);
        verify(consentService).setConsent(new Personnummer("12345678-1234"), true);
    }

    @Test
    public void consentServiceReturnsOK() {
        SetConsentResponseType result = responder.setConsent(null, createRequest("12345678-1234", false));
        assertEquals(ResultCodeEnum.OK, result.getResult().getResultCode());
    }

    private SetConsentRequestType createRequest(String id, boolean consentGiven) {
        SetConsentRequestType parameters = new SetConsentRequestType();
        parameters.setPersonnummer(id);
        parameters.setConsentGiven(consentGiven);
        return parameters;
    }

}
