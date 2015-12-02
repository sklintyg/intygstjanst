package se.inera.intyg.intygstjanst.web.support;

import static se.inera.intyg.common.support.common.enumerations.CertificateTypes.FK7263;

import org.joda.time.LocalDateTime;
import se.inera.intyg.intygstjanst.persistence.model.builder.CertificateBuilder;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

/**
 * @author andreaskaltenbach
 */
public final class CertificateFactory {

    private CertificateFactory() {
    }

    public static final String CERTIFICATE_ID = "123456";
    public static final String CERTIFICATE_DOCUMENT = "{\"name\":\"Some JSON\"}";
    public static final Personnummer CIVIC_REGISTRATION_NUMBER = new Personnummer("19001122-3344");

    public static final String VALID_FROM = "2000-01-01";
    public static final String VALID_TO = "2000-12-31";

    public static final LocalDateTime SIGNED_DATE = new LocalDateTime(1999, 12, 31, 10, 32);
    public static final String SIGNING_DOCTOR = "Dr. Oetker";

    public static final String CARE_UNIT_ID = "1.2.3.4.5.6";
    public static final String CARE_UNIT_NAME = "London Bridge Hospital";

    public static Certificate buildCertificate() {
        return buildCertificate(CERTIFICATE_ID);
    }

    public static Certificate buildCertificate(String certificateId) {
        return buildCertificate(certificateId, VALID_FROM, VALID_TO);
    }

    public static Certificate buildCertificate(String certificateId, String certificateType) {
        return buildCertificate(certificateId, certificateType, VALID_FROM, VALID_TO);
    }

    public static Certificate buildCertificate(String certificateId, String validFrom, String validTo) {
        return buildCertificate(certificateId, FK7263.toString(), validFrom, validTo);
    }

    public static Certificate buildCertificate(String certificateId, String certificateType, String validFrom, String validTo) {
        return new CertificateBuilder(certificateId, CERTIFICATE_DOCUMENT)
                .civicRegistrationNumber(CIVIC_REGISTRATION_NUMBER)
                .certificateType(certificateType)
                .validity(validFrom, validTo)
                .signedDate(SIGNED_DATE)
                .signingDoctorName(SIGNING_DOCTOR)
                .careUnitId(CARE_UNIT_ID)
                .careUnitName(CARE_UNIT_NAME)
                .build();
    }
}
