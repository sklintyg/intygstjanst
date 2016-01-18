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

package se.inera.intyg.intygstjanst.web.integration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.insuranceprocess.healthreporting.setcertificatearchivedresponder.v1.SetCertificateArchivedRequestType;
import se.inera.intyg.intygstjanst.web.service.CertificateService;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class SetCertificateArchivedResponderImplTest {

    @Mock
    private CertificateService certificateService = mock(CertificateService.class);

    @InjectMocks
    private SetCertificateArchivedResponderImpl responder = new SetCertificateArchivedResponderImpl();

    @Test
    public void testSetCertificateArchived() throws Exception {

        SetCertificateArchivedRequestType request = new SetCertificateArchivedRequestType();
        request.setCertificateId("no5");
        request.setNationalIdentityNumber("19001122-3344");
        request.setArchivedState("true");
        responder.setCertificateArchived(null, request);

        verify(certificateService).setArchived("no5", new Personnummer("19001122-3344"), "true");
    }
}
