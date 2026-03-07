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
package se.inera.intyg.intygstjanst.testability;

import static se.inera.intyg.intygstjanst.testability.TestabilityConstants.ALFA_MEDICINCENTRUM;
import static se.inera.intyg.intygstjanst.testability.TestabilityConstants.ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.intygstjanst.testability.dto.CreateSickLeaveRequestDTO;
import se.inera.intyg.intygstjanst.testability.dto.CreateSickLeaveResponseDTO;
import se.inera.intyg.intygstjanst.testability.dto.TestDataOptionsDTO;
import se.inera.intyg.intygstjanst.infrastructure.security.interceptor.ApiBasePath;
import se.inera.intyg.intygstjanst.testability.service.TestabilityService;

@RestController
@ApiBasePath("/resources")
@RequestMapping("/testability")
@Profile({"dev", "testability-api"})
@RequiredArgsConstructor
public class TestabilityController {

    private final TestabilityService testabilityService;

    private static final String VERIFICATION_MESSAGE = String.format("Test data successfully created for units: %s & %s",
        ALFA_MEDICINCENTRUM, ALFA_MEDICINCENTRUM_INFEKTIONSMOTTAGNINGEN);

    @PostMapping("/createDefault")
    public String createDefaultTestData() {
        testabilityService.createDefaultTestData();
        return VERIFICATION_MESSAGE;
    }

    @PostMapping("/createSickLeave")
    public CreateSickLeaveResponseDTO createSickLeave(@RequestBody CreateSickLeaveRequestDTO createSickLeaveRequestDTO) {
        final var certificateId = testabilityService.create(createSickLeaveRequestDTO);
        return new CreateSickLeaveResponseDTO(certificateId);
    }

    @GetMapping("/testDataOptions")
    public TestDataOptionsDTO getAvailableTestDataOptions() {
        return testabilityService.getTestDataOptions();
    }
}
