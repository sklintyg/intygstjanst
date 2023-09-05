package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificatesRepository;
import se.inera.intyg.intygstjanst.web.integration.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateConverter;
import se.inera.intyg.intygstjanst.web.service.ListCitizenCertificatesService;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;

import java.util.List;

@Service
public class ListCitizenCertificatesServiceImpl implements ListCitizenCertificatesService {
    private final CitizenCertificateConverter citizenCertificateConverter;
    private final CitizenCertificatesRepository citizenCertificatesRepository;

    public ListCitizenCertificatesServiceImpl(CitizenCertificateConverter citizenCertificateConverter,
                                                CitizenCertificatesRepository citizenCertificatesRepository) {
        this.citizenCertificateConverter = citizenCertificateConverter;
        this.citizenCertificatesRepository = citizenCertificatesRepository;
    }

    @Override
    public List<CitizenCertificateDTO> get(String patientId,
                                           List<String> certificateTypes,
                                           List<String> units,
                                           List<CitizenCertificateStatusTypeDTO> statuses,
                                           List<String> years) {
        return citizenCertificatesRepository.getCertificatesForPatient(
                patientId,
                certificateTypes,
                units,
                statuses,
                years
        );
    }
}
