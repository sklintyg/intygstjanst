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

package se.inera.intyg.intygstjanst.web.csintegration;

import static se.inera.intyg.intygstjanst.web.csintegration.util.PersonIdTypeEvaluator.getType;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.certificate.dto.SickLeaveCertificate;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SickLeaveCertificatesRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.PersonIdDTO;
import se.inera.intyg.schemas.contract.Personnummer;

@Service
@RequiredArgsConstructor
public class GetSickLeaveCertificatesFromCS {

    private final CSIntegrationService csIntegrationService;

    public List<SickLeaveCertificate> get(Personnummer personId, List<String> certificateTypeList, LocalDate fromDate, LocalDate toDate,
        List<String> units) {
        final var request = SickLeaveCertificatesRequestDTO.builder()
            .personId(
                personId == null ? null :
                    PersonIdDTO.builder()
                        .id(personId.getOriginalPnr())
                        .type(getType(personId))
                        .build()
            )
            .certificateTypes(
                certificateTypeList == null ? List.of() : certificateTypeList
            )
            .signedFrom(fromDate)
            .signedTo(toDate)
            .issuedByUnitIds(
                units == null ? List.of() : units
            )
            .build();

        return csIntegrationService.getSickLeaveCertificates(request);
    }
}
