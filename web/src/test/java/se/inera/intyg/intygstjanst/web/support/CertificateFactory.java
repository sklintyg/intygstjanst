/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.intygstjanst.web.support;


import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.schemas.contract.Personnummer;

import java.time.LocalDateTime;

/**
 * @author andreaskaltenbach
 */
public final class CertificateFactory {
    private static final String INTYG_TYPE = "fk7263";
    private static final java.lang.String INTYG_TYPE_VERSION = "1.0";

    private CertificateFactory() {
    }

    public static final String CERTIFICATE_ID = "123456";
    public static final String CERTIFICATE_DOCUMENT = "{\"name\":\"Some JSON\"}";
    public static final Personnummer CIVIC_REGISTRATION_NUMBER = Personnummer.createPersonnummer("19001122-3344").get();

    public static final String VALID_FROM = "2000-01-01";
    public static final String VALID_TO = "2000-12-31";

    public static final LocalDateTime SIGNED_DATE = LocalDateTime.of(1999, 12, 31, 10, 32);
    public static final String SIGNING_DOCTOR = "Dr. Oetker";

    public static final String CARE_UNIT_ID = "1.2.3.4.5.6";
    public static final String CARE_UNIT_NAME = "London Bridge Hospital";

    public static Certificate buildCertificate() {
        return buildCertificate(CERTIFICATE_ID);
    }

    public static Certificate buildCertificate(String certificateId) {
        return buildCertificate(certificateId, INTYG_TYPE, INTYG_TYPE_VERSION, VALID_FROM, VALID_TO);
    }

    public static Certificate buildCertificate(String certificateId, String certificateType, String certificateTypeVersion) {
        return buildCertificate(certificateId, certificateType, certificateTypeVersion, VALID_FROM, VALID_TO);
    }

    public static Certificate buildCertificate(String certificateId, String certificateType, String certificateTypeVersion, String validFrom, String validTo) {
        Certificate certificate = new Certificate(certificateId);
        certificate.setCivicRegistrationNumber(CIVIC_REGISTRATION_NUMBER);
        certificate.setType(certificateType);
        certificate.setTypeVersion(certificateTypeVersion);
        certificate.setValidFromDate(validFrom);
        certificate.setValidToDate(validTo);
        certificate.setSignedDate(SIGNED_DATE);
        certificate.setSigningDoctorName(SIGNING_DOCTOR);
        certificate.setCareUnitId(CARE_UNIT_ID);
        certificate.setCareUnitName(CARE_UNIT_NAME);
        certificate.setOriginalCertificate(new OriginalCertificate(LocalDateTime.now(), "XML", certificate));
        return certificate;
    }
}
