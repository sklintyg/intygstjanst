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

package se.inera.intyg.intygstjanst.persistence.model.dao.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.Consent;
import se.inera.intyg.intygstjanst.persistence.model.dao.ConsentDao;

/**
 * @author andreaskaltenbach
 */
@Repository
public class ConsentDaoImpl implements ConsentDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void setConsent(Personnummer civicRegistrationNumber) {
        if (!hasConsent(civicRegistrationNumber)) {
            entityManager.persist(new Consent(civicRegistrationNumber));
        }
    }

    @Override
    public void revokeConsent(Personnummer civicRegistrationNumber) {
        Consent consent = findConsent(civicRegistrationNumber);
        if (consent != null) {
            entityManager.remove(consent);
        }
    }

    @Override
    public boolean hasConsent(Personnummer civicRegistrationNumber) {
        return findConsent(civicRegistrationNumber) != null;
    }

    private Consent findConsent(Personnummer civicRegistrationNumber) {
        return entityManager.find(Consent.class, civicRegistrationNumber.getPersonnummer());
    }
}