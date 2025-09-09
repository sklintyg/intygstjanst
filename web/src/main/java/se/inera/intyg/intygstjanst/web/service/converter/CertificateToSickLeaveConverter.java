package se.inera.intyg.intygstjanst.web.service.converter;

import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.builder.SjukfallCertificateBuilder;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;

@Component
public class CertificateToSickLeaveConverter {

    public SjukfallCertificate convert(Certificate certificate) {
        return new SjukfallCertificateBuilder(certificate.getMetadata().getId())
            .build();
    }
}