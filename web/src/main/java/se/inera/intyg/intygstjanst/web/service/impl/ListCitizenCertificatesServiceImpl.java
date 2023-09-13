/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateDTOConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateFilterService;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateTextService;
import se.inera.intyg.intygstjanst.web.service.ListCitizenCertificatesService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.ListCitizenCertificatesRequest;
import se.inera.intyg.intygstjanst.web.service.repo.CitizenCertificatesRepository;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;

@Service
public class ListCitizenCertificatesServiceImpl implements ListCitizenCertificatesService {

    private final CitizenCertificatesRepository citizenCertificatesRepository;
    private final CitizenCertificateDTOConverter citizenCertificateDTOConverter;
    private final CitizenCertificateFilterService citizenCertificateFilterService;
    private final CitizenCertificateTextService citizenCertificateTextService;
    private final MonitoringLogService monitoringLogService;

    public ListCitizenCertificatesServiceImpl(CitizenCertificatesRepository citizenCertificatesRepository,
        CitizenCertificateDTOConverter citizenCertificateDTOConverter,
        CitizenCertificateFilterService citizenCertificateFilterService,
        CitizenCertificateTextService citizenCertificateTextService,
        MonitoringLogService monitoringLogService) {
        this.citizenCertificatesRepository = citizenCertificatesRepository;
        this.citizenCertificateDTOConverter = citizenCertificateDTOConverter;
        this.citizenCertificateFilterService = citizenCertificateFilterService;
        this.citizenCertificateTextService = citizenCertificateTextService;
        this.monitoringLogService = monitoringLogService;
    }

    @Override
    public List<CitizenCertificateDTO> get(ListCitizenCertificatesRequest request) {

        final var certificates = citizenCertificatesRepository.getCertificatesForPatient(request.getPatientId());

        monitoringLogService.logCertificateListedByCitizen(request.getPatientId());

        return certificates
            .stream()
            .map(this::getCitizenCertificateDTO)
            .filter((certificate) -> citizenCertificateFilterService.filter(certificate, request))
            .collect(Collectors.toList());
    }

    private CitizenCertificateDTO getCitizenCertificateDTO(CitizenCertificate certificate) {
        return citizenCertificateDTOConverter.convert(
            certificate,
            citizenCertificateTextService.getTypeName(certificate.getType()),
            citizenCertificateTextService.getAdditionalInfoLabel(certificate.getType(), certificate.getTypeVersion())
        );
    }
}
