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
package se.inera.intyg.intygstjanst.application.intyginfo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.intygstjanst.application.intyginfo.dto.ItIntygInfo;
import se.inera.intyg.intygstjanst.infrastructure.security.interceptor.ApiBasePath;
import se.inera.intyg.intygstjanst.infrastructure.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.infrastructure.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.application.certificate.service.IntygInfoService;

/**
 * Internal REST endpoint for intyg oriented data.
 */
@RestController
@ApiBasePath("/internalapi")
@RequestMapping("/intygInfo")
@RequiredArgsConstructor
public class IntygInfoController {

    private final IntygInfoService intygInfoService;

    @GetMapping("/{id}")
    @PerformanceLogging(eventAction = "retrieve-certificate-info", eventType = MdcLogConstants.EVENT_TYPE_ACCESSED, isActive = false)
    public ResponseEntity<ItIntygInfo> getIntygInfo(@PathVariable String id) {
        final var intygInfo = intygInfoService.getIntygInfo(id);
        return intygInfo.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{hsaId}/count")
    @PerformanceLogging(eventAction = "retrieve-certificate-count", eventType = MdcLogConstants.EVENT_TYPE_ACCESSED)
    public Long getCertificateCountForCareProvider(@PathVariable String hsaId) {
        return intygInfoService.getCertificateCount(hsaId);
    }
}
