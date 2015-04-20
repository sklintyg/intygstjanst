package se.inera.certificate.spec

import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.rivtab20.v3.RegisterMedicalCertificateResponderInterface


/**
 *
 * @author andreaskaltenbach
 */
public class RegistreraFk7263IntygHosFK extends RegistreraFk7263Intyg {

    public void execute() {
        String url = serviceUrl ? serviceUrl : baseUrl + "register-certificate-wiretap/v3.0"
        registerMedicalCertificateResponder = createClient(RegisterMedicalCertificateResponderInterface.class, url)

        super.execute()
    }

    public String resultat() {
        resultAsString(response)
    }
}
