package se.inera.certificate.spec
import se.inera.certificate.spec.util.WsClientFixture
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType

/**
 *
 * @author andreaskaltenbach
 */
public class JournalsystemHamtarListaAvIntyg extends WsClientFixture {

    private ListCertificatesForCareResponderInterface responder

    static String serviceUrl = System.getProperty("service.clinicalProcess.listCertificatesUrl")

    public JournalsystemHamtarListaAvIntyg() {
        String url = serviceUrl ? serviceUrl : baseUrl + "list-certificates-for-care/v1.0"
        responder = createClient(ListCertificatesForCareResponderInterface.class, url)
    }

    String personnummer
    String enhet

    List intygMeta
    def resultat

    private ListCertificatesForCareResponseType response

    def execute() {
        def request = new ListCertificatesForCareType()
        request.personId = personnummer
        request.enhet.add(enhet)

        response = responder.listCertificatesForCare(logicalAddress.value, request)
        switch (response.result.resultCode) {
            case ResultCodeType.OK:
                intygMeta = response.meta
                resultat = "OK"
                break
            default:
                resultat = "[${response.result.resultCode.toString()}] - ${response.result.resultText}"
        }
    }

    def String resultat() {
        resultat
    }

    def intyg() {
        intygMeta.collect { it.certificateId }
    }
}
