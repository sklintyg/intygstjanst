/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateFilterService;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.ListCitizenCertificatesRequest;

@Service
public class CitizenCertificateFilterServiceImpl implements CitizenCertificateFilterService {

    @Override
    public boolean filter(CitizenCertificateDTO certificate, ListCitizenCertificatesRequest request) {
        return filterOnYears(certificate, request.getYears())
            && filterOnUnits(certificate, request.getUnits())
            && filterOnCertificateTypes(certificate, request.getCertificateTypes())
            && filterOnSentStatus(certificate, request.getStatuses());
    }

    private boolean filterOnYears(CitizenCertificateDTO certificate, List<String> includedYears) {
        if (includedYears == null || includedYears.isEmpty()) {
            return true;
        }

        final var signedYear = certificate.getIssued().getYear();

        return includedYears
            .stream()
            .anyMatch((year) -> Integer.parseInt(year) == signedYear);
    }

    private boolean filterOnSentStatus(CitizenCertificateDTO certificate, List<CitizenCertificateStatusTypeDTO> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return true;
        }

        final var includeSent = statuses.stream().anyMatch((status) -> status == CitizenCertificateStatusTypeDTO.SENT);
        final var includeNotSent = statuses.stream().anyMatch((status) -> status == CitizenCertificateStatusTypeDTO.NOT_SENT);

        if (includeSent && includeNotSent) {
            return filterOnSent(certificate) || filterOnNotSent(certificate);
        }

        if (includeSent) {
            return filterOnSent(certificate);
        }

        if (includeNotSent) {
            return filterOnNotSent(certificate);
        }

        return true;
    }

    private boolean filterOnSent(CitizenCertificateDTO certificate) {
        return certificate.getRecipient() != null && certificate.getRecipient().getSent() != null;
    }

    private boolean filterOnNotSent(CitizenCertificateDTO certificate) {
        return certificate.getRecipient() != null && certificate.getRecipient().getSent() == null;
    }

    private boolean filterOnUnits(CitizenCertificateDTO certificate, List<String> unitIds) {
        if (unitIds == null || unitIds.isEmpty()) {
            return true;
        }

        return unitIds.contains(certificate.getUnit().getId());
    }

    private boolean filterOnCertificateTypes(CitizenCertificateDTO certificate, List<String> certificateTypes) {
        if (certificateTypes == null || certificateTypes.isEmpty()) {
            return true;
        }

        return certificateTypes.contains(certificate.getType().getId());
    }
}
