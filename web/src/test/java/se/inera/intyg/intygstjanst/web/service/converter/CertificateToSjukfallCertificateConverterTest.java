package se.inera.intyg.intygstjanst.web.service.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static se.inera.intyg.intygstjanst.web.support.CertificateForSjukfallFactory.getFactoryInstance;

import java.time.LocalDateTime;

import org.junit.Test;

import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande;

/**
 * Created by eriklupander on 2016-02-04.
 */

public class CertificateToSjukfallCertificateConverterTest {

    private static final String CERT_ID = "cert-123";
    private static final LocalDateTime CERT_SIGNING_DATETIME = LocalDateTime.parse("2016-02-01T15:00:00");
    private static final String NAME = "Tolvan Tolvansson";
    private static final String PERSONNUMMER = "19121212-1212";
    private static final String DOC_NAME = "Doc Name";
    private static final String CERT_TYPE = "fk7263";
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
        testee.convertFk7263(getFactoryInstance().buildCert(), new se.inera.intyg.intygstyper.ts_bas.model.internal.Utlatande());
    }

    @Test
    public void testStandardConvert() {
        SjukfallCertificate sjukfallCertificate = testee.convertFk7263(getFactoryInstance().buildCert(), getFactoryInstance().buildUtlatande());
        assertEquals(CERT_ID, sjukfallCertificate.getId());
        assertEquals(CERT_TYPE, sjukfallCertificate.getType());

        assertEquals(CERT_SIGNING_DATETIME, sjukfallCertificate.getSigningDateTime());

        assertEquals(CARE_GIVER_ID, sjukfallCertificate.getCareGiverId());
        assertEquals(CARE_UNIT_ID, sjukfallCertificate.getCareUnitId());
        assertEquals(CARE_UNIT_NAME, sjukfallCertificate.getCareUnitName());

        assertEquals(DOC_ID, sjukfallCertificate.getSigningDoctorId());
        assertEquals(DOC_NAME, sjukfallCertificate.getSigningDoctorName());
        assertEquals(NAME, sjukfallCertificate.getPatientName());
        assertEquals(PERSONNUMMER, sjukfallCertificate.getCivicRegistrationNumber());

        assertEquals(START_DATE_100, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(0).getFromDate());
        assertEquals(END_DATE_100, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(0).getToDate());
        assertEquals(START_DATE_75, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(1).getFromDate());
        assertEquals(END_DATE_75, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(1).getToDate());
        assertEquals(START_DATE_50, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(2).getFromDate());
        assertEquals(END_DATE_50, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(2).getToDate());
        assertEquals(START_DATE_25, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(3).getFromDate());
        assertEquals(END_DATE_25, sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(3).getToDate());

        assertEquals(new Integer(100), sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(0).getCapacityPercentage());
        assertEquals(new Integer(75), sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(1).getCapacityPercentage());
        assertEquals(new Integer(50), sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(2).getCapacityPercentage());
        assertEquals(new Integer(25), sjukfallCertificate.getSjukfallCertificateWorkCapacity().get(3).getCapacityPercentage());
    }

    @Test
    public void testIsConvertableFk7263() {
        Utlatande utlatande = getFactoryInstance().buildUtlatande();
        when(utlatande.getDiagnosKod()).thenReturn("J91");
        assertTrue(testee.isConvertableFk7263(utlatande));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsConvertableFk7263ThrowsExceptionWhenNonFk7263Type() {
        assertTrue(testee.isConvertableFk7263(new se.inera.intyg.intygstyper.ts_bas.model.internal.Utlatande()));
    }

    @Test
    public void testIsConvertableFk7263WhenIsSmittskydd() {
        Utlatande utlatande = getFactoryInstance().buildUtlatande();
        when(utlatande.isAvstangningSmittskydd()).thenReturn(true);
        assertFalse(testee.isConvertableFk7263(utlatande));
    }

    @Test
    public void testIsConvertableFk7263DiagnosisNull() {
        Utlatande utlatande = getFactoryInstance().buildUtlatande();
        when(utlatande.getDiagnosKod()).thenReturn(null);
        assertFalse(testee.isConvertableFk7263(utlatande));
    }

    @Test
    public void testIsConvertableFk7263DiagnosisEmpty() {
        Utlatande utlatande = getFactoryInstance().buildUtlatande();
        when(utlatande.getDiagnosKod()).thenReturn("  ");
        assertFalse(testee.isConvertableFk7263(utlatande));
    }
}
