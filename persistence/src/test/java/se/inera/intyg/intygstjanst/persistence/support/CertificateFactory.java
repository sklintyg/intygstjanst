/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.persistence.support;

import org.joda.time.LocalDateTime;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.builder.CertificateBuilder;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;

/**
 * @author andreaskaltenbach
 */
public final class CertificateFactory {

    private CertificateFactory() {
    }

    public static final String CERTIFICATE_ID = "123456";
    public static final String CERTIFICATE_DOCUMENT = "{\"name\":\"Some JSON\"}";
    public static final Personnummer CIVIC_REGISTRATION_NUMBER = new Personnummer("19001122-3344");
    public static final String FK7263 = "fk7263";

    public static final String VALID_FROM = "2000-01-01";
    public static final String VALID_TO = "2000-12-31";

    public static final LocalDateTime SIGNED_DATE = new LocalDateTime(1999, 12, 31, 10, 32);
    public static final String SIGNING_DOCTOR = "Dr. Oetker";

    public static final String CARE_UNIT_ID = "1.2.3.4.5.6";
    public static final String CARE_UNIT_NAME = "London Bridge Hospital";
    public static final String CARE_GIVER_ID = "5678";

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
        return buildCertificate(certificateId, FK7263, validFrom, validTo);
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
                .careGiverId(CARE_GIVER_ID)
                .build();
    }
}