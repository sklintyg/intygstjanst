package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateConverter;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificate;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;

import java.util.List;

@Service
public class CitizenCertificateConverterImpl implements CitizenCertificateConverter {
    @Override
    public List<CitizenCertificateDTO> get(CitizenCertificate citizenCertificate) {
        return null;
    }
}
