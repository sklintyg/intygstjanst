package se.inera.intyg.common.specifications.spec
import se.inera.intyg.common.specifications.spec.util.WsClientFixture
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType


public class SkickaIntygTillMottagare extends WsClientFixture {

    private SendCertificateToRecipientResponderInterface responder
    private SendCertificateToRecipientResponseType response

    static String serviceUrl = System.getProperty("service.clinicalProcess.SendCertificateToRecipientUrl")

    String personnummer
    String intyg
    String mottagare

    public SkickaIntygTillMottagare() {
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

        resultat = response.result.resultCode.toString();
    }

    public String resultat() {
        resultat
    }

}
