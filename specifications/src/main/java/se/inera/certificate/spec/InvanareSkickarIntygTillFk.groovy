package se.inera.certificate.spec
import se.inera.certificate.spec.util.WsClientFixture
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType


public class InvanareSkickarIntygTillFk extends WsClientFixture {

    private SendCertificateToRecipientResponderInterface responder
    private SendCertificateToRecipientResponseType response

    static String serviceUrl = System.getProperty("service.clinicalProcess.SendCertificateToRecipientUrl")

    String personnummer
    String intyg
    String mottagare

    public InvanareSkickarIntygTillFk() {
        String url = serviceUrl ? serviceUrl : baseUrl + "send-certificate-to-recipient/v1.0"
        responder = createClient(SendCertificateToRecipientResponderInterface.class, url)
    }

    def resultat

    def execute() {
        def request = new SendCertificateToRecipientType()
        request.mottagareId = mottagare
        request.personId = personnummer
        request.utlatandeId = intyg

        response = responder.sendCertificateToRecipient(logicalAddress.value, request)

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
