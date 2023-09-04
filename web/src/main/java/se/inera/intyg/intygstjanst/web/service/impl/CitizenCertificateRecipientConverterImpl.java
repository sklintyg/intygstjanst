package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRecipientConverter;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationType;

@Service
public class CitizenCertificateRecipientConverterImpl implements CitizenCertificateRecipientConverter {

    @Override
    public CitizenCertificateRecipientDTO get(String id, String name, String sent) {
        return CitizenCertificateRecipientDTO
                .builder()
                .id(id)
                .name(name)
                .sent(sent)
                .build();
    }

    private CitizenCertificateRelationType getType(String code, String certificateId, String fromCertificateId) {
        if (code.equals(RelationKod.ERSATT.toString())) {
            return certificateId.equals(fromCertificateId)
                    ? CitizenCertificateRelationType.RENEWED
                    : CitizenCertificateRelationType.RENEWS;
        }

        return null;
    }
}
