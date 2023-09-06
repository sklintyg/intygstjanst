package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateDTOConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateFilterService;
import se.inera.intyg.intygstjanst.web.service.repo.CitizenCertificatesRepository;
import se.inera.intyg.intygstjanst.web.integration.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.ListCitizenCertificatesService;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListCitizenCertificatesServiceImpl implements ListCitizenCertificatesService {
    private final CitizenCertificatesRepository citizenCertificatesRepository;
    private final CitizenCertificateDTOConverter citizenCertificateDTOConverter;
    private final CitizenCertificateFilterService citizenCertificateFilterService;
    private final IntygModuleRegistry intygModuleRegistry;

    public ListCitizenCertificatesServiceImpl(CitizenCertificatesRepository citizenCertificatesRepository,
                                              CitizenCertificateDTOConverter citizenCertificateDTOConverter,
                                              CitizenCertificateFilterService citizenCertificateFilterService,
                                              IntygModuleRegistry intygModuleRegistry) {
        this.citizenCertificatesRepository = citizenCertificatesRepository;
        this.citizenCertificateDTOConverter = citizenCertificateDTOConverter;
        this.citizenCertificateFilterService = citizenCertificateFilterService;
        this.intygModuleRegistry = intygModuleRegistry;
    }

    @Override
    public List<CitizenCertificateDTO> get(String patientId,
                                           List<String> certificateTypes,
                                           List<String> units,
                                           List<CitizenCertificateStatusTypeDTO> statuses,
                                           List<String> years) {

        final var certificates = citizenCertificatesRepository.getCertificatesForPatient(patientId);

        return certificates
                .stream()
                .map((certificate) -> {
                    try {
                        return citizenCertificateDTOConverter.get(certificate, "", "");
                    } catch (ModuleNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter((certificate) -> citizenCertificateFilterService.filterOnYears(certificate, years))
                .filter((certificate) -> citizenCertificateFilterService.filterOnSentStatus(certificate, statuses))
                .filter((certificate) -> citizenCertificateFilterService.filterOnUnits(certificate, units))
                .filter((certificate) -> citizenCertificateFilterService.filterOnCertificateTypes(certificate, certificateTypes))
                .collect(Collectors.toList());
    }
}
