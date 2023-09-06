package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.intygstjanst.web.service.repo.CitizenCertificatesRepository;
import se.inera.intyg.intygstjanst.web.integration.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.ListCitizenCertificatesService;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;

import java.util.Collections;
import java.util.List;

@Service
public class ListCitizenCertificatesServiceImpl implements ListCitizenCertificatesService {
    private final CitizenCertificatesRepository citizenCertificatesRepository;
    private final IntygModuleRegistry intygModuleRegistry;

    public ListCitizenCertificatesServiceImpl(CitizenCertificatesRepository citizenCertificatesRepository,
                                              IntygModuleRegistry intygModuleRegistry) {
        this.citizenCertificatesRepository = citizenCertificatesRepository;
        this.intygModuleRegistry = intygModuleRegistry;
    }

    @Override
    public List<CitizenCertificateDTO> get(String patientId,
                                           List<String> certificateTypes,
                                           List<String> units,
                                           List<CitizenCertificateStatusTypeDTO> statuses,
                                           List<String> years) {

        final var certificates = citizenCertificatesRepository.getCertificatesForPatient(
                patientId,
                certificateTypes,
                units,
                statuses,
                years
        );

        return Collections.emptyList();
    }
}
