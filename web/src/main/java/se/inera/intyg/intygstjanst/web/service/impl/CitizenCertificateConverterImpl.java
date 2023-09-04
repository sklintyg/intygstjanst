package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.persistence.model.dao.CitizenCertificate;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRecipientConverter;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRelationConverter;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;

@Service
public class CitizenCertificateConverterImpl implements CitizenCertificateConverter {
    private final CitizenCertificateRecipientConverter citizenCertificateRecipientConverter;
    private final CitizenCertificateRelationConverter citizenCertificateRelationConverter;

    public CitizenCertificateConverterImpl(CitizenCertificateRecipientConverter citizenCertificateRecipientConverter,
                                           CitizenCertificateRelationConverter citizenCertificateRelationConverter) {
        this.citizenCertificateRecipientConverter = citizenCertificateRecipientConverter;
        this.citizenCertificateRelationConverter = citizenCertificateRelationConverter;
    }

    @Override
    public CitizenCertificateDTO get(CitizenCertificate citizenCertificate) {
        return null;
    }
}
