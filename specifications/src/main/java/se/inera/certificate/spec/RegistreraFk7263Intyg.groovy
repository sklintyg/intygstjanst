package se.inera.certificate.spec
import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource

import org.joda.time.LocalDateTime
import org.springframework.core.io.ClassPathResource

import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType
/**
 *
 * @author andreaskaltenbach
 */
public class RegistreraFk7263Intyg extends WsClientFixture {

    RegisterMedicalCertificateResponderInterface registerMedicalCertificateResponder

    static String serviceUrl = System.getProperty("service.registerMedicalCertificateUrl")

    public RegistreraFk7263Intyg() {
		this(WsClientFixture.LOGICAL_ADDRESS)
	}
	
    public RegistreraFk7263Intyg(String logiskAddress) {
        super(logiskAddress)
		String url = serviceUrl ? serviceUrl : baseUrl + "register-certificate/v3.0"
		registerMedicalCertificateResponder = createClient(RegisterMedicalCertificateResponderInterface.class, url)
    }

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
        JAXBContext jaxbContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        RegisterMedicalCertificateType request = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("fk7263_${mall}_template.xml").getInputStream()), RegisterMedicalCertificateType.class).getValue()

        request.lakarutlatande.patient.personId.extension = personnummer
        request.lakarutlatande.lakarutlatandeId = id
		request.lakarutlatande.signeringsdatum = LocalDateTime.parse(utfärdat)
		request.lakarutlatande.skickatDatum = LocalDateTime.now()
		request.lakarutlatande.skapadAvHosPersonal.fullstandigtNamn = utfärdare
		request.lakarutlatande.skapadAvHosPersonal.enhet.enhetsId.extension = enhet

        response = registerMedicalCertificateResponder.registerMedicalCertificate(null, request);
    }

    public String resultat() {
        resultAsString(response)
    }
}
