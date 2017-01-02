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
package se.inera.intyg.intygstjanst.web.service.impl;

import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.ConsentDao;
import se.inera.intyg.intygstjanst.web.service.ConsentService;

/**
 * @author andreaskaltenbach
 */
@Service
public class ConsentServiceImpl implements ConsentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsentServiceImpl.class);

    @Autowired
    private ConsentDao consentDao;

    @Autowired
    private CertificateDao certificateDao;

    @Override
    @Transactional(readOnly = true)
    public boolean isConsent(Personnummer civicRegistrationNumber) {
        if (civicRegistrationNumber != null && civicRegistrationNumber.getPersonnummer() != null) {
            return consentDao.hasConsent(civicRegistrationNumber);
        } else {
            return true;
        }
    }

    @Override
    @Transactional
    public void setConsent(Personnummer civicRegistrationNumber, boolean consentGiven) {
        if (consentGiven) {
            consentDao.setConsent(civicRegistrationNumber);
        } else {
            consentDao.revokeConsent(civicRegistrationNumber);

            // Since the consent now is revoked we can delete all intyg with deletedByCareGiver set.
            try {
                certificateDao.removeCertificatesDeletedByCareGiver(civicRegistrationNumber);
            } catch (PersistenceException e) {
                LOGGER.error("Failed to remove certificates deleted by care giver for citizen {}", Personnummer.getPnrHashSafe(civicRegistrationNumber));
            }
        }
    }
}
