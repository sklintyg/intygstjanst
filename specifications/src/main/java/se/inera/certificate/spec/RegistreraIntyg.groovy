package se.inera.certificate.spec
import org.joda.time.LocalDateTime
import org.springframework.core.io.ClassPathResource
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerMedicalCertificate.v1.RegisterMedicalCertificateResponderInterface
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerMedicalCertificate.v1.RegisterMedicalCertificateResponseType
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerMedicalCertificate.v1.RegisterMedicalCertificateType
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType
import se.inera.certificate.spec.util.WsClientFixture

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource
/**
 *
 * @author andreaskaltenbach
 */
public class RegistreraIntyg extends WsClientFixture {

    RegisterMedicalCertificateResponderInterface registerMedicalCertificateResponder

    static String serviceUrl = System.getProperty("service.clinicalProcess.registerMedicalCertificateUrl")

    public RegistreraIntyg() {
        this(WsClientFixture.LOGICAL_ADDRESS)
    }

    public RegistreraIntyg(String logiskAddress) {
        super(logiskAddress)
        String url = serviceUrl ? serviceUrl : baseUrl + "register-medical-certificate/v1.0"
        registerMedicalCertificateResponder = createClient(RegisterMedicalCertificateResponderInterface.class, url)
    }

    String typ
    String personnummer
    String utfärdat
    String utfärdare
    String enhet
    String id
    String mall = "M"

    RegisterMedicalCertificateResponseType response

    public void reset() {
        mall = "M"
        utfärdare = "EnUtfärdare"
        enhet = "EnVårdEnhet"
    }

    public void execute() {
        // read request template from file

        def request = new RegisterMedicalCertificateType()

        JAXBContext jaxbContext = JAXBContext.newInstance(UtlatandeType.class)
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller()
        UtlatandeType utlatande = (UtlatandeType) unmarshaller.unmarshal(new StreamSource(new ClassPathResource("clinicalprocess/utlatande_template.xml").getInputStream()), UtlatandeType.class).getValue()

        if (typ) utlatande.typAvUtlatande.code = typ
        utlatande.patient.personId.extension = personnummer
        utlatande.utlatandeId.root = id
        if (utfärdat) utlatande.signeringsdatum = LocalDateTime.parse(utfärdat)
        utlatande.skickatdatum = LocalDateTime.now()
        utlatande.skapadAv.fullstandigtNamn = utfärdare
        utlatande.skapadAv.enhet.enhetsId.extension = enhet

        request.utlatande = utlatande

        response = registerMedicalCertificateResponder.registerMedicalCertificate(logicalAddress, request);
    }

    public String resultat() {
        resultAsString(response)
    }
}
