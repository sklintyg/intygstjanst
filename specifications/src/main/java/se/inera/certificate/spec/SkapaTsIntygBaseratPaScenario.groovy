package se.inera.certificate.spec

import java.util.UUID.*

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource

import org.springframework.core.io.ClassPathResource

import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponderInterface
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponseType
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateType
import se.inera.certificate.spec.util.WsClientFixture
import se.inera.certificate.spec.util.WsClientFixtureNyaKontraktet

public class SkapaTsIntygBaseratPaScenario extends WsClientFixtureNyaKontraktet {

	static String serviceUrl = System.getProperty("service.clinicalProcess.registerCertificateUrl")
	static JAXBContext jaxbContext = JAXBContext.newInstance(RegisterCertificateType.class)
	static Unmarshaller unmarshaller = jaxbContext.createUnmarshaller()
	UUID uid = UUID.fromString("8d796df0-0bad-40bd-88ab-0d0e75952c7e")

	String mall
    String typ
    String personnummer
	String antalIntyg
	
	def responses = []
	
	// Used to iterate through list of personnummer 
	int index = 0

    RegisterCertificateResponseType response
    RegisterCertificateResponderInterface registerCertificateResponder

	public SkapaTsIntygBaseratPaScenario() {
		this(WsClientFixture.LOGICAL_ADDRESS)
	}

	public SkapaTsIntygBaseratPaScenario(String logiskAddress) {
		super(logiskAddress)
		String url = serviceUrl ? serviceUrl : baseUrl + "register-certificate/v1.0"
        registerCertificateResponder = createClient(RegisterCertificateResponderInterface.class, url)
	}
	
	public void execute() {
		
		for (int i = 0; i < Integer.parseInt(antalIntyg); i++) {
			RegisterCertificateType request = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("grundladda/" + typ + "/" + mall).getInputStream()), RegisterCertificateType.class).getValue()

			request.utlatande.patient.personId.extension = personnummer
			request.utlatande.utlatandeId.extension = uid.randomUUID()
			response = registerCertificateResponder.registerCertificate(logicalAddress.toString(), request)
			
			// Put the resultAsString in a list so we can check it all went okay
			responses << resultAsString(response)
		}
	}

	public String resultat() {
		return (responses.count("OK") > 0) ? "OK" : "FAILED"
	}
}