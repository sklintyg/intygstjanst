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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.insuranceprocess.healthreporting.setconsent.rivtabp20.v1.SetConsentResponderInterface;
import se.inera.intyg.insuranceprocess.healthreporting.setconsentresponder.v1.SetConsentRequestType;
import se.inera.intyg.insuranceprocess.healthreporting.setconsentresponder.v1.SetConsentResponseType;
import se.inera.intyg.intygstjanst.web.service.ConsentService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;


@SchemaValidation
public class SetConsentResponderImpl implements SetConsentResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetConsentResponderImpl.class);

    @Autowired
    private ConsentService consentService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Override
    public SetConsentResponseType setConsent(AttributedURIType logicalAddress, SetConsentRequestType parameters) {
        SetConsentResponseType response = new SetConsentResponseType();
        final Personnummer civicRegistrationNumber = new Personnummer(parameters.getPersonnummer());
        try {
            consentService.setConsent(civicRegistrationNumber, parameters.isConsentGiven());
            response.setResult(ResultOfCallUtil.okResult());
            if (parameters.isConsentGiven()) {
                monitoringLogService.logConsentGiven(civicRegistrationNumber);
            } else {
                monitoringLogService.logConsentRevoked(civicRegistrationNumber);
            }
        } catch (DataIntegrityViolationException e) {
            // INTYG-886 GeSamtycke anropas ibland flera gånger i rask takt av klienter, vilket leder till ett
            // race condition som ger DataIntegrityViolationException.
            LOGGER.warn("Consent already given for " + civicRegistrationNumber.getPnrHash() + " - ignored.");
            response.setResult(ResultOfCallUtil.infoResult("Consent already given for " + civicRegistrationNumber.getPnrHash()));
        } catch (HibernateOptimisticLockingFailureException e) {
            // INTYG-886 ÅtertaSamtycke kan teoretiskt anropas flera gånger i rask takt av klienter, vilket leder till ett
            // race condition som ger HibernateOptimisticLockingFailureException.
            LOGGER.warn("Consent already revoked for " + civicRegistrationNumber.getPnrHash() + " - ignored.");
            response.setResult(ResultOfCallUtil.infoResult("Consent already revoked for " + civicRegistrationNumber.getPnrHash()));
        }
        return response;
    }

}
