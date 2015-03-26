package se.inera.certificate.spec

import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.v1.rivtabp20.RevokeMedicalCertificateResponderInterface

/**
 *
 * @author andreaskaltenbach
 */
class RattaIntygViaWireTap extends RattaIntyg {

    public RattaIntygViaWireTap() {
        super()
    }

    public RattaIntygViaWireTap(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
		String url = serviceUrl ? serviceUrl : baseUrl + "revoke-certificate-wiretap/v1.0"
		revokeResponder = createClient(RevokeMedicalCertificateResponderInterface.class, url)
    }
}
