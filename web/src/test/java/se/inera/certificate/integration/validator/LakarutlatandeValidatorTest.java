package se.inera.certificate.integration.validator;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import se.inera.ifv.insuranceprocess.healthreporting.mu7263.v3.AktivitetType;
import se.inera.ifv.insuranceprocess.healthreporting.mu7263.v3.Aktivitetskod;
import se.inera.ifv.insuranceprocess.healthreporting.mu7263.v3.LakarutlatandeType;
import se.inera.ifv.insuranceprocess.healthreporting.mu7263.v3.Prognosangivelse;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import java.io.IOException;

/**
 * @author andreaskaltenbach
 */
public class LakarutlatandeValidatorTest {

    private static Unmarshaller UNMARSHALLER;

    @BeforeClass
    public static void setupJaxb() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class);
        UNMARSHALLER = jaxbContext.createUnmarshaller();
    }

    private LakarutlatandeType lakarutlatande() throws IOException, JAXBException {
        // read request from file
        JAXBElement<RegisterMedicalCertificateType> request = UNMARSHALLER.unmarshal(new StreamSource(new ClassPathResource("register-medical-certificate/register-medical-certificate-valid.xml").getInputStream()), RegisterMedicalCertificateType.class);
        return request.getValue().getLakarutlatande();
    }

    private LakarutlatandeType lakarutlatandeSMIL() throws IOException, JAXBException {
        // read request from file
        JAXBElement<RegisterMedicalCertificateType> request = UNMARSHALLER.unmarshal(new StreamSource(new ClassPathResource("register-medical-certificate/register-medical-certificate-valid.xml").getInputStream()), RegisterMedicalCertificateType.class);
        return request.getValue().getLakarutlatande();
    }

    @Test
    public void testHappyCase() throws Exception {
        new LakarutlatandeValidator(lakarutlatande()).validate();
    }

    @Test
    public void testHappyCaseSMIL() throws Exception {
        new LakarutlatandeValidator(lakarutlatandeSMIL()).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingUtlatandeId() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.setLakarutlatandeId(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingSkickatDatum() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.setSkickatDatum(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingPatient() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.setPatient(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testWrongPatientIdCodeSystem() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getPatient().getPersonId().setRoot("<strange>");
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingPatientId() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getPatient().getPersonId().setExtension(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testWrongPatientId() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getPatient().getPersonId().setExtension("999999-9999");
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingPatientNamn() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getPatient().setFullstandigtNamn(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingSkapadAvHosPersonal() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.setSkapadAvHosPersonal(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testWrongSkapadAvHosPersonalCodeSystem() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().getPersonalId().setRoot("<strange>");
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingSkapadAvHosPersonalID() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().getPersonalId().setExtension(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingSkapadAvHosPersonalNamn() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().setFullstandigtNamn(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingEnhet() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().setEnhet(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingEnhetRoot() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().getEnhet().getEnhetsId().setRoot("<strange>");
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingEnhetId() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().getEnhet().getEnhetsId().setExtension(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingEnhetsNamn() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().getEnhet().setEnhetsnamn(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingEnhetsPostaddress() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().getEnhet().setPostadress(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingEnhetsPostnr() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().getEnhet().setPostnummer(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingEnhetsPostort() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().getEnhet().setPostort(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingEnhetsTelefonnummer() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().getEnhet().setTelefonnummer(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingVardgivare() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().getEnhet().setVardgivare(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingVardgivareID() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().getEnhet().getVardgivare().setVardgivareId(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingVardgivareRoot() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().getEnhet().getVardgivare().getVardgivareId().setRoot("<strange>");
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingVardgivareHSAID() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().getEnhet().getVardgivare().getVardgivareId().setExtension(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingVardgivareNamn() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().getEnhet().getVardgivare().setVardgivarnamn(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingFunktionstillstandAktivitet() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getFunktionstillstand().remove(1);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingMedicinsktTillstand() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.setMedicinsktTillstand(null);

        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingMedicinsktTillstandCode() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getMedicinsktTillstand().setTillstandskod(null);

        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingMedicinsktTillstandCodeSystemName() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getMedicinsktTillstand().getTillstandskod().setCodeSystemName(null);

        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testIllegalMedicinsktTillstandCodeSystemName() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getMedicinsktTillstand().getTillstandskod().setCodeSystemName("<strange>");

        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingFunktionstillstandKroppsFunktion() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getFunktionstillstand().remove(0);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingFunktionstillstandKroppsFunktionBeskrivning() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getFunktionstillstand().get(0).setBeskrivning(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingVårdKontaktAndReference() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getVardkontakt().clear();
        lakarutlatande.getReferens().clear();
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingVårdKontakt1Tid() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getVardkontakt().get(0).setVardkontaktstid(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingVårdKontakt2Tid() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getVardkontakt().get(1).setVardkontaktstid(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingReferens1Tid() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getReferens().get(0).setDatum(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingReferens2Tid() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getReferens().get(1).setDatum(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingArbetsförmåga() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getFunktionstillstand().get(1).setArbetsformaga(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingSysselsättning() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getFunktionstillstand().get(1).getArbetsformaga().getSysselsattning().clear();
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingArbetsBeskrivning() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getFunktionstillstand().get(1).getArbetsformaga().getSysselsattning().remove(2);
        lakarutlatande.getFunktionstillstand().get(1).getArbetsformaga().getSysselsattning().remove(1);
        lakarutlatande.getFunktionstillstand().get(1).getArbetsformaga().setArbetsuppgift(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingVaraktighet() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getFunktionstillstand().get(1).getArbetsformaga().getArbetsformagaNedsattning().clear();
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingVaraktighetFromDatum() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getFunktionstillstand().get(1).getArbetsformaga().getArbetsformagaNedsattning().get(0).setVaraktighetFrom(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingVaraktighetTomDatum() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getFunktionstillstand().get(1).getArbetsformaga().getArbetsformagaNedsattning().get(0).setVaraktighetTom(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMultipleRessätt() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        AktivitetType a = new AktivitetType();
        a.setAktivitetskod(Aktivitetskod.FORANDRAT_RESSATT_TILL_ARBETSPLATSEN_AR_AKTUELLT);
        lakarutlatande.getAktivitet().add(a);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testKommentarOmAnnatSomReferens() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getVardkontakt().clear();
        lakarutlatande.getReferens().remove(0);
        lakarutlatande.setKommentar(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testKommentarOmPrognosGarEjAttBedömma() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getFunktionstillstand().get(1).getArbetsformaga().setPrognosangivelse(Prognosangivelse.DET_GAR_INTE_ATT_BEDOMMA);
        lakarutlatande.setKommentar(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingSigneringsTidpunkt() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.setSigneringsdatum(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test(expected = ValidationException.class)
    public void testMissingArbetsplatskod() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().getEnhet().setArbetsplatskod(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test( expected = ValidationException.class )
    public void testWrongArbetsplatskodRoot() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().getEnhet().getArbetsplatskod().setRoot("<strange>");
        new LakarutlatandeValidator(lakarutlatande).validate();
    }

    @Test( expected = ValidationException.class )
    public void testWrongArbetsplatskodId() throws Exception {
        LakarutlatandeType lakarutlatande = lakarutlatande();
        lakarutlatande.getSkapadAvHosPersonal().getEnhet().getArbetsplatskod().setExtension(null);
        new LakarutlatandeValidator(lakarutlatande).validate();
    }
}
