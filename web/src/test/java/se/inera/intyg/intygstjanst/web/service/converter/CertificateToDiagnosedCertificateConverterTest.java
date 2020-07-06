package se.inera.intyg.intygstjanst.web.service.converter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.fkparent.model.internal.Diagnos;
import se.inera.intyg.common.luae_fs.v1.model.internal.LuaefsUtlatandeV1;
import se.inera.intyg.common.luae_na.v1.model.internal.LuaenaUtlatandeV1;
import se.inera.intyg.common.luse.v1.model.internal.LuseUtlatandeV1;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.schemas.contract.Personnummer;

@RunWith(MockitoJUnitRunner.class)
public class CertificateToDiagnosedCertificateConverterTest {

    private static final String CERT_ID = "cert-123";
    private static final LocalDateTime CERT_SIGNING_DATETIME = LocalDateTime.parse("2016-02-01T15:00:00");
    private static final String NAME = "Tolvan Tolvansson";
    private static final String PERSONNUMMER = "19121212-1212";
    private static final String DOC_NAME = "Doc Name";
    private static final String CERT_TYPE_LUAEFS = "luaefs";
    private static final String CERT_TYPE_LUSE = "luse";
    private static final String CERT_TYPE_LUAENA = "luaena";

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
    private static final String DIAGNOSE_CODE = "diag-1";
    private static final String DIAGNOSE_CODE_2 = "diag-2";

    private final Personnummer pNr = Personnummer.createPersonnummer(PERSONNUMMER).get();


    @Test
    public void convertLuaefs() {
        Certificate certificate = buildCertificate(CERT_TYPE_LUAEFS);
        LuaefsUtlatandeV1 statement = buildLuaefsStatement();

        var diagnosedCertificate = (new CertificateToDiagnosedCertificateConverter()).convertLuaefs(certificate, statement);

        assertEquals(CERT_ID, diagnosedCertificate.getCertificateId());
        assertEquals(CERT_TYPE_LUAEFS, diagnosedCertificate.getCertificateType());

        assertEquals(CERT_SIGNING_DATETIME, diagnosedCertificate.getSigningDateTime());

        assertEquals(CARE_GIVER_ID, diagnosedCertificate.getCareProviderId());
        assertEquals(CARE_UNIT_ID, diagnosedCertificate.getCareUnitId());
        assertEquals(CARE_UNIT_NAME, diagnosedCertificate.getCareUnitName());

        assertEquals(DOC_ID, diagnosedCertificate.getPersonalHsaId());
        assertEquals(DOC_NAME, diagnosedCertificate.getPersonalFullName());
        assertEquals(NAME, diagnosedCertificate.getPatientFullName());
        assertEquals(PERSONNUMMER, diagnosedCertificate.getPersonId());

        assertEquals(DIAGNOSE_CODE, diagnosedCertificate.getDiagnoseCode());
        assertEquals(DIAGNOSE_CODE_2, diagnosedCertificate.getSecondaryDiagnoseCodes().get(0));
    }

    @Test
    public void convertLuaena() {
        Certificate certificate = buildCertificate(CERT_TYPE_LUAENA);
        LuaenaUtlatandeV1 statement = buildLuaenaStatement();

        var diagnosedCertificate = (new CertificateToDiagnosedCertificateConverter()).convertLuaena(certificate, statement);

        assertEquals(CERT_ID, diagnosedCertificate.getCertificateId());
        assertEquals(CERT_TYPE_LUAENA, diagnosedCertificate.getCertificateType());

        assertEquals(CERT_SIGNING_DATETIME, diagnosedCertificate.getSigningDateTime());

        assertEquals(CARE_GIVER_ID, diagnosedCertificate.getCareProviderId());
        assertEquals(CARE_UNIT_ID, diagnosedCertificate.getCareUnitId());
        assertEquals(CARE_UNIT_NAME, diagnosedCertificate.getCareUnitName());

        assertEquals(DOC_ID, diagnosedCertificate.getPersonalHsaId());
        assertEquals(DOC_NAME, diagnosedCertificate.getPersonalFullName());
        assertEquals(NAME, diagnosedCertificate.getPatientFullName());
        assertEquals(PERSONNUMMER, diagnosedCertificate.getPersonId());

        assertEquals(DIAGNOSE_CODE, diagnosedCertificate.getDiagnoseCode());
        assertEquals(DIAGNOSE_CODE_2, diagnosedCertificate.getSecondaryDiagnoseCodes().get(0));
    }

    @Test
    public void convertLuse() {
        Certificate certificate = buildCertificate(CERT_TYPE_LUSE);
        LuseUtlatandeV1 statement = buildLuseStatement();

        var diagnosedCertificate = (new CertificateToDiagnosedCertificateConverter()).convertLuse(certificate, statement);

        assertEquals(CERT_ID, diagnosedCertificate.getCertificateId());
        assertEquals(CERT_TYPE_LUSE, diagnosedCertificate.getCertificateType());

        assertEquals(CERT_SIGNING_DATETIME, diagnosedCertificate.getSigningDateTime());

        assertEquals(CARE_GIVER_ID, diagnosedCertificate.getCareProviderId());
        assertEquals(CARE_UNIT_ID, diagnosedCertificate.getCareUnitId());
        assertEquals(CARE_UNIT_NAME, diagnosedCertificate.getCareUnitName());

        assertEquals(DOC_ID, diagnosedCertificate.getPersonalHsaId());
        assertEquals(DOC_NAME, diagnosedCertificate.getPersonalFullName());
        assertEquals(NAME, diagnosedCertificate.getPatientFullName());
        assertEquals(PERSONNUMMER, diagnosedCertificate.getPersonId());

        assertEquals(DIAGNOSE_CODE, diagnosedCertificate.getDiagnoseCode());
        assertEquals(DIAGNOSE_CODE_2, diagnosedCertificate.getSecondaryDiagnoseCodes().get(0));
    }

    private LuaefsUtlatandeV1 buildLuaefsStatement() {
        var statement = mock(LuaefsUtlatandeV1.class);
        when(statement.getDiagnoser()).thenReturn(getDiagnosis());
        when(statement.getGrundData()).thenReturn(getBasicData());
        return statement;
    }

    private LuaenaUtlatandeV1 buildLuaenaStatement() {
        var statement = mock(LuaenaUtlatandeV1.class);
        when(statement.getDiagnoser()).thenReturn(getDiagnosis());
        when(statement.getGrundData()).thenReturn(getBasicData());
        return statement;
    }

    private LuseUtlatandeV1 buildLuseStatement() {
        var statement = mock(LuseUtlatandeV1.class);
        when(statement.getDiagnoser()).thenReturn(getDiagnosis());
        when(statement.getGrundData()).thenReturn(getBasicData());
        return statement;
    }

    private ImmutableList<Diagnos> getDiagnosis() {
        return ImmutableList
            .copyOf(java.util.List.of(Diagnos.create(DIAGNOSE_CODE, null, null, null),
                Diagnos.create(DIAGNOSE_CODE_2, null, null, null)));
    }

    private GrundData getBasicData() {
        var basicData = new GrundData();

        var patient = new Patient();
        patient.setFullstandigtNamn(NAME);
        patient.setPersonId(pNr);

        basicData.setPatient(patient);
        basicData.setSigneringsdatum(CERT_SIGNING_DATETIME);

        var hoSPersonal = new HoSPersonal();
        hoSPersonal.setPersonId(DOC_ID);
        hoSPersonal.setFullstandigtNamn(DOC_NAME);
        basicData.setSkapadAv(hoSPersonal);

        return basicData;
    }

    private Certificate buildCertificate(String type) {
        var certificate = new Certificate(CERT_ID);
        certificate.setType(type);
        certificate.setTypeVersion("1.0");
        certificate.setSignedDate(CERT_SIGNING_DATETIME);
        certificate.setSigningDoctorName(DOC_NAME);
        certificate.setCivicRegistrationNumber(pNr);
        certificate.setCareGiverId(CARE_GIVER_ID);
        certificate.setCareUnitId(CARE_UNIT_ID);
        certificate.setCareUnitName(CARE_UNIT_NAME);
        certificate.setOriginalCertificate(new OriginalCertificate(LocalDateTime.now(), "XML", certificate));
        return certificate;
    }
}