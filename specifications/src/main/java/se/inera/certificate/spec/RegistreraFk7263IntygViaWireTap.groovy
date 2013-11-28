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
public class RegistreraFk7263IntygViaWireTap extends RegistreraFk7263Intyg {

    public RegistreraFk7263IntygViaWireTap() {
		this(WsClientFixture.LOGICAL_ADDRESS)
	}
	
    public RegistreraFk7263IntygViaWireTap(String logiskAddress) {
        super(logiskAddress)
		String url = serviceUrl ? serviceUrl : baseUrl + "register-certificate-wiretap/v3.0"
		registerMedicalCertificateResponder = createClient(RegisterMedicalCertificateResponderInterface.class, url)
    }
}
