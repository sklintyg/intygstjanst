/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import java.time.LocalDateTime;
import se.inera.intyg.infra.testcertificate.dto.TestCertificateEraseResult;

/**
 * Service for managing test certificates.
 */
public interface TestCertificateService {

    /**
     * Erase certificates flagged as test certificates that has been created within passed date ranges.
     *
     * Any related certificates (e.g. renewed, complemented etc) will also be erased.
     *
     * Important! When the test certificates are erased, it will not be possible to recover them.
     *
     */
    TestCertificateEraseResult eraseTestCertificates(LocalDateTime from, LocalDateTime to);
}
