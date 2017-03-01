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
package se.inera.intyg.intygstjanst.persistence.model.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.Consent;
import se.inera.intyg.intygstjanst.persistence.model.dao.ConsentDao;

/**
 * @author andreaskaltenbach
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/persistence-config-unittest.xml" })
@ActiveProfiles("dev")
@Transactional
public class ConsentDaoImplTest {

    public static final Personnummer CIVIC_REGISTRATION_NUMBER = new Personnummer("19001122-3344");
    public static final Personnummer CIVIC_REGISTRATION_NUMBER_NO_DASH = new Personnummer("190011223344");
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ConsentDao consentDao;

    @Test
    public void testSetConsent() {
        assertEquals(0, allConsents().size());

        consentDao.setConsent(CIVIC_REGISTRATION_NUMBER);

        assertEquals(1, allConsents().size());
        assertEquals(CIVIC_REGISTRATION_NUMBER, allConsents().get(0).getCivicRegistrationNumber());
    }

    @Test
    public void testSetConsentWithExistingConsent() {
        entityManager.persist(new Consent(CIVIC_REGISTRATION_NUMBER));
        assertEquals(1, allConsents().size());
        assertEquals(CIVIC_REGISTRATION_NUMBER, allConsents().get(0).getCivicRegistrationNumber());

        consentDao.setConsent(CIVIC_REGISTRATION_NUMBER);

        assertEquals(1, allConsents().size());
        assertEquals(CIVIC_REGISTRATION_NUMBER, allConsents().get(0).getCivicRegistrationNumber());
    }

    @Test
    public void testHasConsent() {

        assertFalse(consentDao.hasConsent(CIVIC_REGISTRATION_NUMBER));
        assertFalse(consentDao.hasConsent(CIVIC_REGISTRATION_NUMBER_NO_DASH));

        consentDao.setConsent(CIVIC_REGISTRATION_NUMBER);

        assertTrue(consentDao.hasConsent(CIVIC_REGISTRATION_NUMBER));
        assertTrue(consentDao.hasConsent(CIVIC_REGISTRATION_NUMBER_NO_DASH));

        consentDao.revokeConsent(CIVIC_REGISTRATION_NUMBER);

        assertFalse(consentDao.hasConsent(CIVIC_REGISTRATION_NUMBER));
        assertFalse(consentDao.hasConsent(CIVIC_REGISTRATION_NUMBER_NO_DASH));
    }

    @Test
    public void testHasConsentNoDash() {

        assertFalse(consentDao.hasConsent(CIVIC_REGISTRATION_NUMBER_NO_DASH));
        assertFalse(consentDao.hasConsent(CIVIC_REGISTRATION_NUMBER));

        consentDao.setConsent(CIVIC_REGISTRATION_NUMBER_NO_DASH);

        assertTrue(consentDao.hasConsent(CIVIC_REGISTRATION_NUMBER_NO_DASH));
        assertTrue(consentDao.hasConsent(CIVIC_REGISTRATION_NUMBER));

        consentDao.revokeConsent(CIVIC_REGISTRATION_NUMBER_NO_DASH);

        assertFalse(consentDao.hasConsent(CIVIC_REGISTRATION_NUMBER_NO_DASH));
        assertFalse(consentDao.hasConsent(CIVIC_REGISTRATION_NUMBER));
    }

    @Test
    public void testRevokeConsent() {

        entityManager.persist(new Consent(CIVIC_REGISTRATION_NUMBER));
        assertEquals(1, allConsents().size());

        consentDao.revokeConsent(CIVIC_REGISTRATION_NUMBER);

        assertEquals(0, allConsents().size());
    }

    @Test
    public void testRevokeConsentWithoutExistingConsent() {
        assertEquals(0, allConsents().size());

        consentDao.revokeConsent(CIVIC_REGISTRATION_NUMBER);

        assertEquals(0, allConsents().size());
    }

    private List<Consent> allConsents() {
        return entityManager.createQuery("SELECT c FROM Consent c", Consent.class).getResultList();
    }
}
