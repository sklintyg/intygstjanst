package se.inera.intyg.intygstjanst.web.service.repo.model;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationType;

import java.time.LocalDateTime;

@Service
public class CitizenCertificateRelationConverterImpl implements CitizenCertificateRelationConverter {

    @Override
    public CitizenCertificateRelationDTO convert(String certificateId,
                                                 String toCertificateId,
                                                 String fromCertificateId,
                                                 LocalDateTime timeStamp,
                                                 String code) {
        if (!certificateId.equals(toCertificateId) && !certificateId.equals(fromCertificateId)) {
            return null;
        }

        return CitizenCertificateRelationDTO
                .builder()
                .certificateId(getRelatedId(certificateId, toCertificateId, fromCertificateId))
                .timestamp(timeStamp.toString())
                .type(getType(code, certificateId, toCertificateId))
                .build();
    }

    private String getRelatedId(String id, String toId, String fromId) {
        return id.equals(toId) ? fromId : toId;
    }

    private CitizenCertificateRelationType getType(String code, String certificateId, String toCertificateId) {
        if (code.equals(RelationKod.ERSATT.toString())) {
            return certificateId.equals(toCertificateId)
                    ? CitizenCertificateRelationType.RENEWED
                    : CitizenCertificateRelationType.RENEWS;
        }

        return CitizenCertificateRelationType.UNKNOWN;
    }
}
