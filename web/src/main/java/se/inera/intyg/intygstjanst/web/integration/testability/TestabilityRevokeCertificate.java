/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.web.exception.TestCertificateException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.schemas.contract.Personnummer;

@Component
@Transactional
public class TestabilityRevokeCertificate {

    private final CertificateService certificateService;

    public TestabilityRevokeCertificate(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    public void revokeCertificate(String patientId, String certificateId) {
        try {
            certificateService.revokeCertificate(Personnummer.createPersonnummer(patientId).orElse(null), certificateId);
        } catch (TestCertificateException | CertificateRevokedException | InvalidCertificateException e) {
            throw new RuntimeException(e);
        }
    }
}
