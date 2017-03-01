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

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.ConsentDao;
import se.inera.intyg.intygstjanst.web.service.ConsentService;

@RunWith(MockitoJUnitRunner.class)
public class ConsentServiceImplTest {

    private static final Personnummer CONSENT_USER = new Personnummer("consentUser");

    @Mock
    private ConsentDao consentDao = mock(ConsentDao.class);

    @Mock
    private CertificateDao certificateDao = mock(CertificateDao.class);

    @InjectMocks
    private ConsentService consentService = new ConsentServiceImpl();

    @Test
    public void unknownUserHasNoConsent() {
        when(consentDao.hasConsent(new Personnummer("unknown"))).thenReturn(false);

        assertFalse(consentService.isConsent(new Personnummer("unknown")));
    }

    @Test
    public void testSettingConsent() {
        consentService.setConsent(CONSENT_USER, true);
        verify(consentDao).setConsent(CONSENT_USER);
    }

    @Test
    public void testSettingNoConsent() {
        consentService.setConsent(CONSENT_USER, false);
        verify(consentDao).revokeConsent(CONSENT_USER);
    }

    @Test
    public void testRevokeConsentPerformsCleanup() {
        consentService.setConsent(CONSENT_USER, false);
        verify(certificateDao).removeCertificatesDeletedByCareGiver(eq(CONSENT_USER));
    }
}
