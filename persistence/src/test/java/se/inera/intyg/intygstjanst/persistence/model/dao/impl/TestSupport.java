/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.persistence.model.dao.impl;

import java.time.LocalDateTime;

import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.intygstjanst.persistence.TestConfig;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.schemas.contract.Personnummer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles({"dev"})
@Transactional
public abstract class TestSupport {

    public static final LocalDateTime SIGNED_DATE = LocalDateTime.of(1999, 12, 31, 10, 32);

    public static final Personnummer CIVIC_REGISTRATION_NUMBER = Personnummer.createPersonnummer("19001122-3344").get();
    public static final Personnummer CIVIC_REGISTRATION_NUMBER_NO_DASH = Personnummer.createPersonnummer("190011223344").get();

    public static final String CERTIFICATE_ID = "123456";
    public static final String CERTIFICATE_DOCUMENT = "{\"name\":\"Some JSON\"}";
    public static final String FK7263 = "fk7263";
    public static final String SIGNING_DOCTOR = "Dr. Oetker";
    public static final String CARE_UNIT_ID = "1.2.3.4.5.6";
    public static final String CARE_UNIT_NAME = "London Bridge Hospital";
    public static final String CARE_GIVER_ID = "5678";
    private static final String DEFAULT_TYPE_VERSION = "1.0";

    public static Certificate buildCertificate() {
        return buildCertificate(CERTIFICATE_ID);
    }

    public static Certificate buildCertificate(String certificateId) {
        return buildCertificate(certificateId, SIGNED_DATE);
    }

    public static Certificate buildCertificate(String certificateId, String certificateType) {
        return buildCertificate(certificateId, certificateType, SIGNED_DATE);
    }

    public static Certificate buildCertificate(String certificateId, LocalDateTime signedDate) {
        return buildCertificate(certificateId, FK7263, signedDate);
    }

    public static Certificate buildCertificate(String certificateId, String certificateType, LocalDateTime signedDate) {
        Certificate certificate = new Certificate(certificateId);
        certificate.setCivicRegistrationNumber(CIVIC_REGISTRATION_NUMBER);
        certificate.setType(certificateType);
        certificate.setTypeVersion(DEFAULT_TYPE_VERSION);
        certificate.setSignedDate(signedDate);
        certificate.setSigningDoctorName(SIGNING_DOCTOR);
        certificate.setCareUnitId(CARE_UNIT_ID);
        certificate.setCareUnitName(CARE_UNIT_NAME);
        certificate.setCareGiverId(CARE_GIVER_ID);
        return certificate;
    }
}
