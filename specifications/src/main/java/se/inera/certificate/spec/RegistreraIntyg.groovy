package se.inera.certificate.spec

import org.joda.time.LocalDateTime
import org.springframework.core.io.ClassPathResource
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponderInterface
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponseType
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateType
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType
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

    RegisterCertificateResponderInterface registerCertificateResponder

    static String serviceUrl = System.getProperty("service.clinicalProcess.registerCertificateUrl")

    public RegistreraIntyg() {
        this(WsClientFixture.LOGICAL_ADDRESS)
    }

    public RegistreraIntyg(String logiskAddress) {
        super(logiskAddress)
        String url = serviceUrl ? serviceUrl : baseUrl + "register-certificate/v1.0"
        registerCertificateResponder = createClient(RegisterCertificateResponderInterface.class, url)
    }

    String typ
    String personnummer
    String utfärdat
    String utfärdare
    String enhet
    String id
    String mall = "M"

    RegisterCertificateResponseType response

    public void reset() {
        mall = "M"
        utfärdare = "EnUtfärdare"
        enhet = "EnVårdEnhet"
    }

    public void execute() {
        // read request template from file

        def request = new RegisterCertificateType()

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

        response = registerCertificateResponder.registerCertificate(LOGICAL_ADDRESS, request);
    }

    public String resultat() {
        if (response) {
            switch (response.result.resultCode) {
                case ResultCodeType.OK:
                    return response.result.resultCode.toString()
                default:
                    return "[${response.result.resultCode.toString()}] - ${response.result.resultText}"
            }
        }
    }
}
