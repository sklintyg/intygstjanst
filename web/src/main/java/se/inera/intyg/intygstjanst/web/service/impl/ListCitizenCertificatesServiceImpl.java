package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.intygstjanst.web.service.*;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.ListCitizenCertificatesRequestDTO;
import se.inera.intyg.intygstjanst.web.service.repo.CitizenCertificatesRepository;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;
import se.inera.intyg.schemas.contract.Personnummer;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<CitizenCertificateDTO> get(ListCitizenCertificatesRequestDTO request) {

        final var certificates = citizenCertificatesRepository.getCertificatesForPatient(request.getPatientId());

        monitoringLogService.logCertificateListedByCitizen(Personnummer.createPersonnummer(request.getPatientId()).orElse(null));

        return certificates
                .stream()
                .map(this::getCitizenCertificateDTO)
                .filter((certificate) -> citizenCertificateFilterService.filter(certificate, request))
                .collect(Collectors.toList());
    }

    private CitizenCertificateDTO getCitizenCertificateDTO(CitizenCertificate certificate) {
        try {
            return citizenCertificateDTOConverter.convert(
                    certificate,
                    citizenCertificateTextService.getTypeName(certificate.getType()),
                    citizenCertificateTextService.getAdditionalInfoLabel(certificate.getType(), certificate.getTypeVersion())
            );
        } catch (ModuleNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
