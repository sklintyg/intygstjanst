/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.infrastructure.csintegration;

import static se.inera.intyg.intygstjanst.infrastructure.csintegration.util.PersonIdTypeEvaluator.getType;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.application.citizen.dto.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.application.sickleave.dto.PersonIdDTO;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.dto.GetCitizenCertificatesRequest;
import se.inera.intyg.schemas.contract.Personnummer;

@Service
@RequiredArgsConstructor
public class CitizenCertificatesFromCS {

  private final CSIntegrationService csIntegrationService;
  private final CitizenCertificateConverterFromCS citizenCertificateConverterFromCS;

  public List<CitizenCertificateDTO> get(Personnummer personId) {

    final var citizenCertificates =
        csIntegrationService.getCitizenCertificates(
            GetCitizenCertificatesRequest.builder()
                .personId(
                    PersonIdDTO.builder()
                        .id(personId.getOriginalPnr())
                        .type(getType(personId))
                        .build())
                .build());

    return citizenCertificates.stream()
        .map(citizenCertificateConverterFromCS::convert)
        .collect(Collectors.toList());
  }
}
