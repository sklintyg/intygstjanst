/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.service.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static se.inera.intyg.intygstjanst.web.support.CertificateForSjukfallFactory.getFactoryInstance;

import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import org.junit.Test;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.fkparent.model.internal.Diagnos;
import se.inera.intyg.common.lisjp.v1.model.internal.LisjpUtlatandeV1;
import se.inera.intyg.common.ts_bas.v6.model.internal.TsBasUtlatandeV6;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;

/**
 * Created by eriklupander on 2016-02-04.
 */

public class CertificateToSjukfallCertificateConverterTest {

    private static final String CERT_ID = "cert-123";
    private static final LocalDateTime CERT_SIGNING_DATETIME = LocalDateTime.parse("2016-02-01T15:00:00");
    private static final String NAME = "Tolvan Tolvansson";
    private static final String PERSONNUMMER = "19121212-1212";
    private static final String DOC_NAME = "Doc Name";
    private static final String CERT_TYPE_FK7263 = "fk7263";
    private static final String CERT_TYPE_LISJP = "lisjp";

    private static final String START_DATE_100 = "2016-02-01";
    private static final String END_DATE_100 = "2216-02-01";
    private static final String START_DATE_75 = "2016-03-01";
    private static final String END_DATE_75 = "2216-03-01";
    private static final String START_DATE_50 = "2016-04-01";
    private static final String END_DATE_50 = "2216-04-01";
    private static final String START_DATE_25 = "2016-05-01";
    private static final String END_DATE_25 = "2216-05-01";

    private static final String DOC_ID = "doc-1";
    private static final String CARE_UNIT_ID = "enhet-1";
    private static final String CARE_UNIT_NAME = "Enhet1";
    private static final String CARE_GIVER_ID = "vardgivare-1";

    private CertificateToSjukfallCertificateConverter testee = new CertificateToSjukfallCertificateConverter();

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsExceptionWhenNonFk7263Type() {
        testee.convertFk7263(getFactoryInstance().buildCert("fk"), TsBasUtlatandeV6.builder().build());
    }

    @Test
    public void testStandardConvert() {
        SjukfallCertificate sjukfallCertificate = testee
            .convertFk7263(getFactoryInstance().buildCert(CERT_TYPE_FK7263), getFactoryInstance().buildFk7263Utlatande());
        assertEquals(CERT_ID, sjukfallCertificate.getId());
        assertEquals(CERT_TYPE_FK7263, sjukfallCertificate.getType());

        assertEquals(CERT_SIGNING_DATETIME, sjukfallCertificate.getSigningDateTime());

        assertEquals(CARE_GIVER_ID, sjukfallCertificate.getCareGiverId());
        assertEquals(CARE_UNIT_ID, sjukfallCertificate.getCareUnitId());
        assertEquals(CARE_UNIT_NAME, sjukfallCertificate.getCareUnitName());

        assertEquals(DOC_ID, sjukfallCertificate.getSigningDoctorId());
        assertEquals(DOC_NAME, sjukfallCertificate.getSigningDoctorName());
        assertEquals(NAME, sjukfallCertificate.getPatientName());
        assertEquals(PERSONNUMMER, sjukfallCertificate.getCivicRegistrationNumber());

        assertEquals(4, sjukfallCertificate.getSjukfallCertificateWorkCapacity().size());
        assertEquals(START_DATE_100, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(0).getFromDate());
        assertEquals(END_DATE_100, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(0).getToDate());
        assertEquals(START_DATE_75, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(1).getFromDate());
        assertEquals(END_DATE_75, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(1).getToDate());
        assertEquals(START_DATE_50, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(2).getFromDate());
        assertEquals(END_DATE_50, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(2).getToDate());
        assertEquals(START_DATE_25, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(3).getFromDate());
        assertEquals(END_DATE_25, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(3).getToDate());

        assertEquals(Integer.valueOf(100), sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(0).getCapacityPercentage());
        assertEquals(Integer.valueOf(75), sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(1).getCapacityPercentage());
        assertEquals(Integer.valueOf(50), sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(2).getCapacityPercentage());
        assertEquals(Integer.valueOf(25), sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(3).getCapacityPercentage());
    }

    @Test
    public void testConvertLisjp() {
        LisjpUtlatandeV1 lisjpUtlatandeV1 = getFactoryInstance().buildLisjpUtlatande();

        SjukfallCertificate sjukfallCertificate = testee.convertLisjp(getFactoryInstance().buildCert(CERT_TYPE_LISJP), lisjpUtlatandeV1);
        assertEquals(CERT_ID, sjukfallCertificate.getId());
        assertEquals(CERT_TYPE_LISJP, sjukfallCertificate.getType());

        assertEquals(CERT_SIGNING_DATETIME, sjukfallCertificate.getSigningDateTime());

        assertEquals(CARE_GIVER_ID, sjukfallCertificate.getCareGiverId());
        assertEquals(CARE_UNIT_ID, sjukfallCertificate.getCareUnitId());
        assertEquals(CARE_UNIT_NAME, sjukfallCertificate.getCareUnitName());

        assertEquals(DOC_ID, sjukfallCertificate.getSigningDoctorId());
        assertEquals(DOC_NAME, sjukfallCertificate.getSigningDoctorName());
        assertEquals(NAME, sjukfallCertificate.getPatientName());
        assertEquals(PERSONNUMMER, sjukfallCertificate.getCivicRegistrationNumber());

        assertEquals(4, sjukfallCertificate.getSjukfallCertificateWorkCapacity().size());
        assertEquals(START_DATE_100, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(0).getFromDate());
        assertEquals(END_DATE_100, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(0).getToDate());
        assertEquals(START_DATE_75, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(1).getFromDate());
        assertEquals(END_DATE_75, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(1).getToDate());
        assertEquals(START_DATE_50, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(2).getFromDate());
        assertEquals(END_DATE_50, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(2).getToDate());
        assertEquals(START_DATE_25, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(3).getFromDate());
        assertEquals(END_DATE_25, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(3).getToDate());

        assertEquals(Integer.valueOf(100), sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(0).getCapacityPercentage());
        assertEquals(Integer.valueOf(75), sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(1).getCapacityPercentage());
        assertEquals(Integer.valueOf(50), sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(2).getCapacityPercentage());
        assertEquals(Integer.valueOf(25), sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(3).getCapacityPercentage());

    }

    @Test
    public void testConvertOnlyOneSjukfallCertificateWorkCapacity() {
        Fk7263Utlatande utlatande = getFactoryInstance().buildFk7263Utlatande();
        when(utlatande.getNedsattMed75()).thenReturn(null);
        when(utlatande.getNedsattMed50()).thenReturn(null);
        when(utlatande.getNedsattMed25()).thenReturn(null);
        SjukfallCertificate sjukfallCertificate = testee.convertFk7263(getFactoryInstance().buildCert(CERT_TYPE_FK7263), utlatande);
        assertEquals(CERT_ID, sjukfallCertificate.getId());
        assertEquals(CERT_TYPE_FK7263, sjukfallCertificate.getType());

        assertEquals(CERT_SIGNING_DATETIME, sjukfallCertificate.getSigningDateTime());

        assertEquals(CARE_GIVER_ID, sjukfallCertificate.getCareGiverId());
        assertEquals(CARE_UNIT_ID, sjukfallCertificate.getCareUnitId());
        assertEquals(CARE_UNIT_NAME, sjukfallCertificate.getCareUnitName());

        assertEquals(DOC_ID, sjukfallCertificate.getSigningDoctorId());
        assertEquals(DOC_NAME, sjukfallCertificate.getSigningDoctorName());
        assertEquals(NAME, sjukfallCertificate.getPatientName());
        assertEquals(PERSONNUMMER, sjukfallCertificate.getCivicRegistrationNumber());

        assertEquals(1, sjukfallCertificate.getSjukfallCertificateWorkCapacity().size());
        assertEquals(START_DATE_100, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(0).getFromDate());
        assertEquals(END_DATE_100, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(0).getToDate());
        assertEquals(Integer.valueOf(100), sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(0).getCapacityPercentage());
    }

    @Test
    public void testIsConvertableFk7263() {
        Fk7263Utlatande utlatande = getFactoryInstance().buildFk7263Utlatande();
        when(utlatande.getDiagnosKod()).thenReturn("J91");
        assertTrue(testee.isConvertableFk7263(utlatande));
    }

    @Test
    public void testIsConvertableFk7263ReturnsFalseWhenNonFk7263Type() {
        assertFalse(testee.isConvertableFk7263(TsBasUtlatandeV6.builder().build()));
    }

    @Test
    public void testIsConvertableFk7263WhenIsSmittskydd() {
        Fk7263Utlatande utlatande = getFactoryInstance().buildFk7263Utlatande();
        when(utlatande.isAvstangningSmittskydd()).thenReturn(true);
        assertFalse(testee.isConvertableFk7263(utlatande));
    }

    @Test
    public void testIsConvertableFk7263DiagnosisNull() {
        Fk7263Utlatande utlatande = getFactoryInstance().buildFk7263Utlatande();
        when(utlatande.getDiagnosKod()).thenReturn(null);
        assertFalse(testee.isConvertableFk7263(utlatande));
    }

    @Test
    public void testIsConvertableFk7263DiagnosisEmpty() {
        Fk7263Utlatande utlatande = getFactoryInstance().buildFk7263Utlatande();
        when(utlatande.getDiagnosKod()).thenReturn("  ");
        assertFalse(testee.isConvertableFk7263(utlatande));
    }

    @Test
    public void testTrimsIds() {
        Fk7263Utlatande utlatande = getFactoryInstance().buildFk7263Utlatande();

        SjukfallCertificate sjukfallCertificate = testee.convertFk7263(getFactoryInstance().buildCert(CERT_TYPE_FK7263), utlatande);
        assertTrue(noTrimmableWhitespaces(sjukfallCertificate.getCareGiverId()));
        assertTrue(noTrimmableWhitespaces(sjukfallCertificate.getCareUnitId()));
        assertTrue(noTrimmableWhitespaces(sjukfallCertificate.getCivicRegistrationNumber()));
        assertTrue(noTrimmableWhitespaces(sjukfallCertificate.getId()));

    }

    private boolean noTrimmableWhitespaces(String str) {
        return str == null || (!str.startsWith(" ") && !str.endsWith(" "));
    }

    @Test
    public void testIsConvertableLisjp() {
        LisjpUtlatandeV1 utlatande = getFactoryInstance().buildLisjpUtlatande();
        when(utlatande.getDiagnoser()).thenReturn(ImmutableList.of(Diagnos.create("J91", "", "", "")));
        assertTrue(testee.isConvertableLisjp(utlatande));
    }

    @Test
    public void testIsConvertableLisjpReturnsFalseWhenNonLisjpType() {
        assertFalse(testee.isConvertableLisjp(TsBasUtlatandeV6.builder().build()));
    }

    @Test
    public void testIsConvertableLisjpWhenIsSmittskydd() {
        LisjpUtlatandeV1 utlatande = getFactoryInstance().buildLisjpUtlatande();
        when(utlatande.getAvstangningSmittskydd()).thenReturn(true);
        assertFalse(testee.isConvertableLisjp(utlatande));
    }

    @Test
    public void testIsConvertableLisjpDiagnosisNull() {
        LisjpUtlatandeV1 utlatande = getFactoryInstance().buildLisjpUtlatande();
        when(utlatande.getDiagnoser()).thenReturn(ImmutableList.of(Diagnos.create(null, "", "", "")));
        assertFalse(testee.isConvertableLisjp(utlatande));
    }

    @Test
    public void testIsConvertableLisjpDiagnosisEmpty() {
        LisjpUtlatandeV1 utlatande = getFactoryInstance().buildLisjpUtlatande();
        when(utlatande.getDiagnoser()).thenReturn(ImmutableList.of(Diagnos.create(" ", "", "", "")));
        assertFalse(testee.isConvertableLisjp(utlatande));
    }

}
