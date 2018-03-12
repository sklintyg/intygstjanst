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
package se.inera.intyg.intygstjanst.persistence.model.dao;

import se.inera.intyg.common.support.peristence.dao.util.DaoUtil;
import se.inera.intyg.schemas.contract.Personnummer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author andreaskaltenbach
 */
@Entity
@Table(name = "CONSENT")
public class Consent {

    @javax.persistence.Id
    @Column(name = "CIVIC_REGISTRATION_NUMBER")
    private String civicRegistrationNumber;

    public Consent() {
        // default constructor for hibernate
    }

    public Consent(Personnummer civicRegistrationNumber) {
        this.civicRegistrationNumber = DaoUtil.formatPnrForPersistence(civicRegistrationNumber);
    }

    public Personnummer getCivicRegistrationNumber() {
        return Personnummer.createValidatedPersonnummer(civicRegistrationNumber).get();
    }

    public void setCivicRegistrationNumber(Personnummer civicRegistrationNumber) {
        this.civicRegistrationNumber = DaoUtil.formatPnrForPersistence(civicRegistrationNumber);
    }
}
