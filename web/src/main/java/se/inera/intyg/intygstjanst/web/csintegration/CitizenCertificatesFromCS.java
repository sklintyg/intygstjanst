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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCitizenCertificatesRequest;
import se.inera.intyg.intygstjanst.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.intygstjanst.web.service.dto.PersonIdDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.schemas.contract.Personnummer;

@Service
@RequiredArgsConstructor
public class CitizenCertificatesFromCS {

    private final CertificateServiceProfile certificateServiceProfile;
    private final CSIntegrationService csIntegrationService;
    private final CitizenCertificateConverter citizenCertificateConverter;

    public List<CitizenCertificateDTO> get(Personnummer personId) {
        if (!certificateServiceProfile.active()) {
            return Collections.emptyList();
        }
        
        final var citizenCertificates = csIntegrationService.getCitizenCertificates(
            GetCitizenCertificatesRequest.builder()
                .personId(
                    PersonIdDTO.builder()
                        .id(personId.getOriginalPnr())
                        .type(getType(personId))
                        .build())
                .build()
        );

        return citizenCertificates.stream()
            .map(citizenCertificateConverter::convert)
            .collect(Collectors.toList());
    }
}
