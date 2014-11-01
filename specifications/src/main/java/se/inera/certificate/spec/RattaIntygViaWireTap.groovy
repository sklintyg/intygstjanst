package se.inera.certificate.spec

import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.v1.rivtabp20.RevokeMedicalCertificateResponderInterface

/**
 *
 * @author andreaskaltenbach
 */
class RattaIntygViaWireTap extends RattaIntyg {

    public RattaIntygViaWireTap() {
        this(WsClientFixture.LOGICAL_ADDRESS)
    }

    public RattaIntygViaWireTap(String logiskAddress) {
        super(logiskAddress)
		String url = serviceUrl ? serviceUrl : baseUrl + "revoke-certificate-wiretap/v1.0"
		revokeResponder = createClient(RevokeMedicalCertificateResponderInterface.class, url)
    }
}
