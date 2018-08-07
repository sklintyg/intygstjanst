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

import org.apache.cxf.annotations.SchemaValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.insuranceprocess.healthreporting.getconsent.rivtabp20.v1.GetConsentResponderInterface;
import se.inera.intyg.insuranceprocess.healthreporting.getconsentresponder.v1.GetConsentRequestType;
import se.inera.intyg.insuranceprocess.healthreporting.getconsentresponder.v1.GetConsentResponseType;
import se.inera.intyg.intygstjanst.web.service.ConsentService;

@SchemaValidation
public class GetConsentResponderImpl implements GetConsentResponderInterface {

    @Autowired
    private ConsentService consentService;

    @Override
    @PrometheusTimeMethod
    public GetConsentResponseType getConsent(AttributedURIType logicalAddress, GetConsentRequestType parameters) {
        GetConsentResponseType response = new GetConsentResponseType();
        response.setConsentGiven(consentService.isConsent(createPnr(parameters.getPersonnummer())));
        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }

    private Personnummer createPnr(String pnr) {
        return Personnummer.createPersonnummer(pnr).orElse(null);
    }
}
