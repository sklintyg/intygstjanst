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
package se.inera.intyg.intygstjanst.web.integration.certificate;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.infra.certificate.dto.CertificateListRequest;
import se.inera.intyg.infra.certificate.dto.CertificateListResponse;
import se.inera.intyg.intygstjanst.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.infrastructure.security.interceptor.ApiBasePath;
import se.inera.intyg.intygstjanst.web.service.CertificateListService;

/**
 * Internal REST endpoint to retrieve list of certificates.
 */
@RestController
@ApiBasePath("/internalapi")
@RequestMapping("/certificatelist")
@RequiredArgsConstructor
public class CertificateListController {

    private final CertificateListService certificateListService;

    /**
     * Internal REST endpoint to retrieve list of signed certificates for a doctor on the logged in unit.
     *
     * @param parameters Parameters of filter query including filters that user has chosen or default filters.
     * @return Response including a list of all signed certificates and the total amount of certificates.
     */
    @PostMapping("/certificates/doctor")
    @PerformanceLogging(eventAction = "list-certificates", eventType = MdcLogConstants.EVENT_TYPE_ACCESSED)
    public CertificateListResponse listCertificatesForDoctor(@RequestBody CertificateListRequest parameters) {
        return certificateListService.listCertificatesForDoctor(parameters);
    }
}
