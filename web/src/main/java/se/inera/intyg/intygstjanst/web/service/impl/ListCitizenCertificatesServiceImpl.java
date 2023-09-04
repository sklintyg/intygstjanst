package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.integration.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRecipientConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRelationConverter;
import se.inera.intyg.intygstjanst.web.service.ListCitizenCertificatesService;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;

import java.util.List;

@Service
public class ListCitizenCertificatesServiceImpl implements ListCitizenCertificatesService {
    private final CitizenCertificateConverter citizenCertificateConverter;
    private final CitizenCertificateRecipientConverter citizenCertificateRecipientConverter;
    private final CitizenCertificateRelationConverter citizenCertificateRelationConverter;

    public ListCitizenCertificatesServiceImpl(CitizenCertificateConverter citizenCertificateConverter,
                                              CitizenCertificateRecipientConverter citizenCertificateRecipientConverter,
                                              CitizenCertificateRelationConverter citizenCertificateRelationConverter) {
        this.citizenCertificateConverter = citizenCertificateConverter;
        this.citizenCertificateRecipientConverter = citizenCertificateRecipientConverter;
        this.citizenCertificateRelationConverter = citizenCertificateRelationConverter;
    }

    @Override
    public List<CitizenCertificateDTO> get(String patientId,
                                           List<String> certificateTypes,
                                           List<String> units,
                                           List<CitizenCertificateStatusTypeDTO> statuses,
                                           List<String> years) {
        return null;
    }
}
