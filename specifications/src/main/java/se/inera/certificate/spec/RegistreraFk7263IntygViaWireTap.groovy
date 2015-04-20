package se.inera.certificate.spec

import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.rivtabp20.v3.RegisterMedicalCertificateResponderInterface


/**
 *
 * @author andreaskaltenbach
 */
public class RegistreraFk7263IntygViaWireTap extends RegistreraFk7263Intyg {

    public RegistreraFk7263IntygViaWireTap() {
		super()
	}

    public RegistreraFk7263IntygViaWireTap(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
		String url = serviceUrl ? serviceUrl : baseUrl + "register-certificate-wiretap/v3.0"
		registerMedicalCertificateResponder = createClient(RegisterMedicalCertificateResponderInterface.class, url)
    }
}
