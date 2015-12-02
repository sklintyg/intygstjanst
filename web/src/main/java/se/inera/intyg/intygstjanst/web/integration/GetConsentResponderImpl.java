package se.inera.intyg.intygstjanst.web.integration;

import org.apache.cxf.annotations.SchemaValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.web.service.ConsentService;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.insuranceprocess.healthreporting.getconsent.rivtabp20.v1.GetConsentResponderInterface;
import se.inera.intyg.insuranceprocess.healthreporting.getconsentresponder.v1.GetConsentRequestType;
import se.inera.intyg.insuranceprocess.healthreporting.getconsentresponder.v1.GetConsentResponseType;


@SchemaValidation
public class GetConsentResponderImpl implements GetConsentResponderInterface {

    @Autowired
    private ConsentService consentService;

    @Override
    public GetConsentResponseType getConsent(AttributedURIType logicalAddress, GetConsentRequestType parameters) {
        GetConsentResponseType response = new GetConsentResponseType();
        response.setConsentGiven(consentService.isConsent(new Personnummer(parameters.getPersonnummer())));
        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }

}
