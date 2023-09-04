package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.persistence.model.dao.CitizenCertificatesDao;
import se.inera.intyg.intygstjanst.web.integration.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateConverter;
import se.inera.intyg.intygstjanst.web.service.ListCitizenCertificatesService;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListCitizenCertificatesServiceImpl implements ListCitizenCertificatesService {
    private final CitizenCertificateConverter citizenCertificateConverter;
    private final CitizenCertificatesDao citizenCertificatesDao;

    public ListCitizenCertificatesServiceImpl(CitizenCertificateConverter citizenCertificateConverter,
                                                CitizenCertificatesDao citizenCertificatesDao) {
        this.citizenCertificateConverter = citizenCertificateConverter;
        this.citizenCertificatesDao = citizenCertificatesDao;
    }

    @Override
    public List<CitizenCertificateDTO> get(String patientId,
                                           List<String> certificateTypes,
                                           List<String> units,
                                           List<CitizenCertificateStatusTypeDTO> statuses,
                                           List<String> years) {

        final var certificates = citizenCertificatesDao.getCertificatesForPatient(
                patientId,
                certificateTypes,
                units,
                statuses.stream().map(Enum::toString).collect(Collectors.toList()),
                years
        );

        certificates
                .stream()
                .map((certificate) -> citizenCertificateConverter.get(certificate))
                .collect(Collectors.toList());

        return null;
    }
}
