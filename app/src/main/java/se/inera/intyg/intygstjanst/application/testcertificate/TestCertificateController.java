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
package se.inera.intyg.intygstjanst.application.testcertificate;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.intygstjanst.application.testcertificate.dto.TestCertificateEraseRequest;
import se.inera.intyg.intygstjanst.infrastructure.security.interceptor.ApiBasePath;
import se.inera.intyg.intygstjanst.application.testcertificate.dto.TestCertificateEraseResult;
import se.inera.intyg.intygstjanst.infrastructure.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.infrastructure.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.application.testcertificate.service.TestCertificateService;

/**
 * Internal REST endpoint for managing test certificates.
 */
@RestController
@ApiBasePath("/internalapi")
@RequestMapping("/testCertificate")
@RequiredArgsConstructor
public class TestCertificateController {

    private final TestCertificateService testCertificateService;

    @PostMapping("/erase")
    @PerformanceLogging(eventAction = "erase-test-certificate", eventType = MdcLogConstants.EVENT_TYPE_DELETION)
    public TestCertificateEraseResult eraseTestCertificates(@RequestBody TestCertificateEraseRequest eraseRequest) {

        if (eraseRequest.getTo() == null) {
            throw new IllegalArgumentException("Missing date to");
        }

        if (eraseRequest.getFrom() != null && eraseRequest.getFrom().isAfter(eraseRequest.getTo())) {
            throw new IllegalArgumentException("From date is after to date");
        }

        return testCertificateService.eraseTestCertificates(eraseRequest.getFrom(), eraseRequest.getTo());
    }
}
