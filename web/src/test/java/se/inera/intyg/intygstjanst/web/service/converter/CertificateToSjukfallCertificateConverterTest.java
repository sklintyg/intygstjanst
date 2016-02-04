package se.inera.intyg.intygstjanst.web.service.converter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import se.inera.intyg.common.support.model.InternalLocalDateInterval;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstyper.fk7263.model.internal.Utlatande;

/**
 * Created by eriklupander on 2016-02-04.
 */

public class CertificateToSjukfallCertificateConverterTest {

    private static final String CERT_ID = "cert-123";
    private static final String FNAME = "Tolvan";
    private static final String ENAME = "Tolvansson";
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
    private Personnummer pNr = new Personnummer(PERSONNUMMER);

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsExceptionWhenNonFk7263Type() {
        testee.convertFk7263(buildCert(), new se.inera.intyg.intygstyper.ts_bas.model.internal.Utlatande());
    }

    @Test
    public void testStandardConvert() {
        SjukfallCertificate sjukfallCertificate = testee.convertFk7263(buildCert(), buildUtlatande());
        assertEquals(CERT_ID, sjukfallCertificate.getId());
        assertEquals(CERT_TYPE, sjukfallCertificate.getType());

        assertEquals(CARE_GIVER_ID, sjukfallCertificate.getCareGiverId());
        assertEquals(CARE_UNIT_ID, sjukfallCertificate.getCareUnitId());
        assertEquals(CARE_UNIT_NAME, sjukfallCertificate.getCareUnitName());

        assertEquals(DOC_ID, sjukfallCertificate.getSigningDoctorId());
        assertEquals(DOC_NAME, sjukfallCertificate.getSigningDoctorName());
        assertEquals(FNAME, sjukfallCertificate.getPatientFirstName());
        assertEquals(ENAME, sjukfallCertificate.getPatientLastName());
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

    private Utlatande buildUtlatande() {
        Utlatande utlatande = mock(Utlatande.class);
        GrundData grundData = mock(GrundData.class);

        HoSPersonal hoSPersonal = mock(HoSPersonal.class);
        Patient patient = mock(Patient.class);

        when(utlatande.getGrundData()).thenReturn(grundData);
        when(grundData.getPatient()).thenReturn(patient);
        when(grundData.getSkapadAv()).thenReturn(hoSPersonal);
        when(hoSPersonal.getPersonId()).thenReturn(DOC_ID);
        when(hoSPersonal.getFullstandigtNamn()).thenReturn(DOC_NAME);
        when(patient.getFornamn()).thenReturn(FNAME);
        when(patient.getEfternamn()).thenReturn(ENAME);

        when(utlatande.getNedsattMed100()).thenReturn(new InternalLocalDateInterval(START_DATE_100, END_DATE_100));
        when(utlatande.getNedsattMed75()).thenReturn(new InternalLocalDateInterval(START_DATE_75, END_DATE_75));
        when(utlatande.getNedsattMed50()).thenReturn(new InternalLocalDateInterval(START_DATE_50, END_DATE_50));
        when(utlatande.getNedsattMed25()).thenReturn(new InternalLocalDateInterval(START_DATE_25, END_DATE_25));

        return utlatande;
    }

    private Certificate buildCert() {
        Certificate cert = new Certificate(CERT_ID, "doc");
        cert.setType(CERT_TYPE);
        cert.setSigningDoctorName(DOC_NAME);
        cert.setCivicRegistrationNumber(pNr);
        cert.setCareGiverId(CARE_GIVER_ID);
        cert.setCareUnitId(CARE_UNIT_ID);
        cert.setCareUnitName(CARE_UNIT_NAME);
        return cert;
    }
}
