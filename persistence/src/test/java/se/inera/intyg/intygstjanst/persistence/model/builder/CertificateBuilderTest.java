/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.persistence.model.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;

public class CertificateBuilderTest {

    private static final String FROM_DATE = "2013-03-01";
    private static final String TO_DATE = "2013-03-20";
    private static final LocalDateTime SIGNED_DATE = new LocalDateTime(2013, 3, 1, 11, 32);

    @Test
    public void setAllFields() {
        Certificate certificate = new CertificateBuilder("certificateId")
            .certificateType("certificateType")
            .validity(FROM_DATE, TO_DATE)
            .signingDoctorName("signingDoctorName")
            .careUnitName("careUnitName")
            .signedDate(SIGNED_DATE)
            .deleted(false)
            .build();

        assertEquals("certificateId", certificate.getId());
        assertEquals("careUnitName", certificate.getCareUnitName());
        assertEquals("certificateType", certificate.getType());
        assertEquals("signingDoctorName", certificate.getSigningDoctorName());
        assertEquals(SIGNED_DATE, certificate.getSignedDate());
        assertEquals(FROM_DATE, certificate.getValidFromDate());
        assertEquals(TO_DATE, certificate.getValidToDate());
        assertFalse(certificate.getDeleted());
    }

    @Test
    public void testNullDate() {
        Certificate certificate = new CertificateBuilder("certificateId")
        .certificateType("certificateType")
        .validity(null, null)
        .signingDoctorName("signingDoctorName")
        .careUnitName("careUnitName")
        .signedDate(SIGNED_DATE)
        .deleted(false)
        .build();

        assertEquals("certificateId", certificate.getId());
        assertEquals("careUnitName", certificate.getCareUnitName());
        assertEquals("certificateType", certificate.getType());
        assertEquals("signingDoctorName", certificate.getSigningDoctorName());
        assertEquals(SIGNED_DATE, certificate.getSignedDate());
        assertEquals(null, certificate.getValidFromDate());
        assertEquals(null, certificate.getValidToDate());
        assertFalse(certificate.getDeleted());
    }
}
