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
package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.schemas.contract.Personnummer;

public interface ConsentService {

    /**
     * Consent status for given civicRegistrationNumber.
     *
     * @param civicRegistrationNumber
     * @return true if consent is registered for or civicRegistrationNumer is null
     */
    boolean isConsent(Personnummer civicRegistrationNumber);

    /**
     * Set consent status for given cuvucRegistrationNumber. True means consent is given and false means no consent is given.
     *
     * @param civicRegistrationNumber
     * @param consentGiven
     */
    void setConsent(Personnummer civicRegistrationNumber, boolean consentGiven);
}