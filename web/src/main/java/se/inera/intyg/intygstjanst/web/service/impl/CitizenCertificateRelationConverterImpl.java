package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRelationConverter;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationType;

import java.time.LocalDateTime;

@Service
public class CitizenCertificateRelationConverterImpl implements CitizenCertificateRelationConverter {

    @Override
    public CitizenCertificateRelationDTO get(String certificateId,
                                             String toCertificateId,
                                             String fromCertificateId,
                                             LocalDateTime timeStamp,
                                             String code) {
        return CitizenCertificateRelationDTO
                .builder()
                .certificateId(certificateId)
                .timestamp(timeStamp.toString())
                .type(getType(code, certificateId, toCertificateId, fromCertificateId))
                .build();
    }

    private CitizenCertificateRelationType getType(String code, String certificateId, String toCertificateId, String fromCertificateId) {
        if (!certificateId.equals(toCertificateId) && !certificateId.equals(fromCertificateId)) {
            return null;
        }

        if (code.equals(RelationKod.ERSATT.toString())) {
            return certificateId.equals(toCertificateId)
                    ? CitizenCertificateRelationType.RENEWED
                    : CitizenCertificateRelationType.RENEWS;
        }

        return null;
    }
}
