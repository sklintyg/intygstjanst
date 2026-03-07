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

package se.inera.intyg.intygstjanst.application.reko;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.intygstjanst.application.reko.dto.CreateRekoStatusRequestDTO;
import se.inera.intyg.intygstjanst.application.reko.dto.GetRekoStatusRequestDTO;
import se.inera.intyg.intygstjanst.application.sickleave.dto.RekoStatusDTO;
import se.inera.intyg.intygstjanst.infrastructure.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.infrastructure.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.application.reko.service.CreateRekoStatusService;
import se.inera.intyg.intygstjanst.infrastructure.security.interceptor.ApiBasePath;
import se.inera.intyg.intygstjanst.application.reko.service.GetRekoStatusService;

@RestController
@ApiBasePath("/internalapi")
@RequestMapping("/reko")
@RequiredArgsConstructor
public class RekoController {

    private final CreateRekoStatusService createRekoStatusService;
    private final GetRekoStatusService getRekoStatusService;

    @PostMapping()
    @PerformanceLogging(eventAction = "create-reko-status", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public RekoStatusDTO createRekoStatus(@RequestBody CreateRekoStatusRequestDTO request) {
        return createRekoStatusService.create(
            request.getPatientId(),
            request.getStatusId(),
            request.getCareProviderId(),
            request.getCareUnitId(),
            request.getUnitId(),
            request.getStaffId(),
            request.getStaffName(),
            request.getSickLeaveTimestamp()
        );
    }

    @PostMapping("/patient")
    @PerformanceLogging(eventAction = "retrieve-reko-status", eventType = MdcLogConstants.EVENT_TYPE_ACCESSED)
    public RekoStatusDTO getRekoStatus(@RequestBody GetRekoStatusRequestDTO request) {
        return getRekoStatusService.get(
            request.getPatientId(),
            request.getEndDate(),
            request.getStartDate(),
            request.getCareUnitId()
        );
    }
}
