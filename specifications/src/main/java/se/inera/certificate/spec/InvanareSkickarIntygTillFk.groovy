package se.inera.certificate.spec
import se.inera.certificate.clinicalprocess.healthcond.certificate.sendcertificateforcitizen.v1.SendCertificateForCitizenResponderInterface
import se.inera.certificate.clinicalprocess.healthcond.certificate.sendcertificateforcitizen.v1.SendCertificateForCitizenResponseType
import se.inera.certificate.clinicalprocess.healthcond.certificate.sendcertificateforcitizen.v1.SendCertificateForCitizenType
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType
import se.inera.certificate.spec.util.WsClientFixture

public class InvanareSkickarIntygTillFk extends WsClientFixture {

    private SendCertificateForCitizenResponderInterface responder
    private SendCertificateForCitizenResponseType response

    static String serviceUrl = System.getProperty("service.clinicalProcess.sendCertificateForCitizenUrl")

    String personnummer
    String intyg
    String mottagare

    public InvanareSkickarIntygTillFk() {
        String url = serviceUrl ? serviceUrl : baseUrl + "send-certificate-for-citizen/v1.0"
        responder = createClient(SendCertificateForCitizenResponderInterface.class, url)
    }

    def resultat

    def execute() {
        def request = new SendCertificateForCitizenType()
        request.mottagareId = mottagare
        request.personId = personnummer
        request.utlatandeId = intyg

        response = responder.sendCertificateForCitizen(LOGICAL_ADDRESS, request)

        switch (response.result.resultCode) {
            case ResultCodeType.OK:
                resultat = "OK"
                break
            default:
                resultat = "[${response.result.resultCode.toString()}] - ${response.result.resultText}"
        }
    }

    public String resultat() {
        resultat
    }

}
