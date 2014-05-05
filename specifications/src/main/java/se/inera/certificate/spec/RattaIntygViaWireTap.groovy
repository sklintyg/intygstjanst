package se.inera.certificate.spec

import iso.v21090.dt.v1.II

import org.joda.time.LocalDateTime

import riv.insuranceprocess.healthreporting.medcertqa._1.LakarutlatandeEnkelType
import riv.insuranceprocess.healthreporting.medcertqa._1.VardAdresseringsType
import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.v1.rivtabp20.RevokeMedicalCertificateResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.v1.rivtabp20.RevokeMedicalCertificateResponderService
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType
import se.inera.ifv.insuranceprocess.healthreporting.v2.EnhetType
import se.inera.ifv.insuranceprocess.healthreporting.v2.HosPersonalType
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType
import se.inera.ifv.insuranceprocess.healthreporting.v2.VardgivareType

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
