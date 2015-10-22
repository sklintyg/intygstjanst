package se.inera.certificate.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.modules.support.api.dto.Personnummer;
import se.inera.certificate.service.ConsentService;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.intyg.insuranceprocess.healthreporting.getconsent.rivtabp20.v1.GetConsentResponderInterface;
import se.inera.intyg.insuranceprocess.healthreporting.getconsentresponder.v1.GetConsentRequestType;
import se.inera.intyg.insuranceprocess.healthreporting.getconsentresponder.v1.GetConsentResponseType;


@RunWith(MockitoJUnitRunner.class)
public class GetConsentResponderImplTest {

    @Mock
    private ConsentService consentService = mock(ConsentService.class);

    @InjectMocks
    private GetConsentResponderInterface responder = new GetConsentResponderImpl();

    @Test
    public void consentServiceIsCalledWithPersonnummer() {
        responder.getConsent(null, createRequest("12345678-1234"));
        verify(consentService).isConsent(new Personnummer("12345678-1234"));
    }

    @Test
    public void consentServiceReturnsNoConsent() {
        when(consentService.isConsent(new Personnummer("12345678-1234"))).thenReturn(false);
        GetConsentResponseType consent = responder.getConsent(null, createRequest("12345678-1234"));
        assertFalse(consent.isConsentGiven());
        assertEquals(ResultCodeEnum.OK, consent.getResult().getResultCode());
    }

    @Test
    public void consentServiceRetunsConsent() {
        when(consentService.isConsent(new Personnummer("12345678-1235"))).thenReturn(true);
        GetConsentResponseType consent = responder.getConsent(null, createRequest("12345678-1235"));
        assertTrue(consent.isConsentGiven());
        assertEquals(ResultCodeEnum.OK, consent.getResult().getResultCode());
    }

    private GetConsentRequestType createRequest(String id) {
        GetConsentRequestType parameters = new GetConsentRequestType();
        parameters.setPersonnummer(id);
        return parameters;
    }

}
