package se.inera.certificate.spec

import org.joda.time.LocalDateTime
import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.certificate.v1.StatusType
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatus.rivtab20.v1.SetCertificateStatusResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatusresponder.v1.SetCertificateStatusRequestType
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatusresponder.v1.SetCertificateStatusResponseType

/**
 *
 * @author andreaskaltenbach
 */
class MarkeraIntygSomHamtat extends WsClientFixture {

    private SetCertificateStatusResponderInterface setCertificateStatusResponder

    String personnr
    String intyg

    String kommentar

    static String serviceUrl = System.getProperty("service.setCertificateStatusUrl")

	public MarkeraIntygSomHamtat() {
		String url = serviceUrl ? serviceUrl : baseUrl + "set-certificate-status/v1.0"
		setCertificateStatusResponder = createClient(SetCertificateStatusResponderInterface.class, url)
    }

    public String resultat() {
        SetCertificateStatusRequestType parameters = new SetCertificateStatusRequestType()
        parameters.nationalIdentityNumber = personnr
        parameters.certificateId = intyg

        parameters.target = "FK"
        parameters.timestamp = LocalDateTime.now()
        parameters.status = StatusType.SENT

        SetCertificateStatusResponseType response = setCertificateStatusResponder.setCertificateStatus(logicalAddress, parameters)

        resultAsString(response)
    }
}
