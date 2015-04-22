package se.inera.certificate.spec

import org.joda.time.LocalDateTime
import org.springframework.core.io.ClassPathResource
import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.rivtabp20.v3.RegisterMedicalCertificateResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource


/**
 *
 * @author andreaskaltenbach
 */
public class RegistreraFk7263Intyg extends WsClientFixture {

    RegisterMedicalCertificateResponderInterface registerMedicalCertificateResponder

    static String serviceUrl = System.getProperty("service.registerMedicalCertificateUrl")

    public RegistreraFk7263Intyg() {
		super()
	}
	
    public RegistreraFk7263Intyg(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
		String url = serviceUrl ? serviceUrl : baseUrl + "register-certificate/v3.0"
		registerMedicalCertificateResponder = createClient(RegisterMedicalCertificateResponderInterface.class, url)
    }

    String personnummer
    String utfärdat = LocalDateTime.now().toString()
	String utfärdare
	String enhetsId = "1.2.3"
    String enhet
    String id
    String vårdGivarId = "4.5.6"
	String mall = "M"
	
    RegisterMedicalCertificateResponseType response

	public void reset() {
		mall = "M"
		utfärdare = "EnUtfärdare"
		enhetsId = "1.2.3"
        enhet = null
        vårdGivarId = "4.5.6"
        utfärdat = LocalDateTime.now().toString()
	}

	public void execute() {
        if (!enhet) enhet = enhetsId
        // read request template from file
        JAXBContext jaxbContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        RegisterMedicalCertificateType request = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("fk7263_${mall}_template.xml").getInputStream()), RegisterMedicalCertificateType.class).getValue()

        request.lakarutlatande.patient.personId.extension = personnummer
        request.lakarutlatande.lakarutlatandeId = id
		request.lakarutlatande.signeringsdatum = LocalDateTime.parse(utfärdat)
		request.lakarutlatande.skickatDatum = LocalDateTime.now()
		request.lakarutlatande.skapadAvHosPersonal.fullstandigtNamn = utfärdare
		request.lakarutlatande.skapadAvHosPersonal.enhet.enhetsId.extension = enhetsId
        request.lakarutlatande.skapadAvHosPersonal.enhet.enhetsnamn = enhet
        request.lakarutlatande.skapadAvHosPersonal.enhet.vardgivare.vardgivareId.extension = vårdGivarId
        
        response = registerMedicalCertificateResponder.registerMedicalCertificate(logicalAddress, request);
    }

    public String resultat() {
        resultAsString(response)
    }
}
