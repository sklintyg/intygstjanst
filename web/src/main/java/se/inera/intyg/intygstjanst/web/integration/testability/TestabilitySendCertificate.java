/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.integration.testability;

import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.TestCertificateException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.schemas.contract.Personnummer;

@Component
public class TestabilitySendCertificate {

    private final CertificateService certificateService;
    private static final String RECIPIENT_ID = "FKASSA";

    public TestabilitySendCertificate(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    public void sendCertificate(String patientId, String certificateId) {
        try {
            certificateService.sendCertificate(Personnummer.createPersonnummer(patientId).orElse(null), certificateId, RECIPIENT_ID);
        } catch (TestCertificateException | InvalidCertificateException | RecipientUnknownException | CertificateRevokedException e) {
            throw new RuntimeException(e);
        }
    }
}
