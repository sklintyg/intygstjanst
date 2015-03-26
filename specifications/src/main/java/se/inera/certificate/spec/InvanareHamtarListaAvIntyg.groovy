package se.inera.certificate.spec
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenResponderInterface
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenResponseType
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenType
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType
import se.inera.certificate.spec.util.WsClientFixture

public class InvanareHamtarListaAvIntyg extends WsClientFixture {

    private ListCertificatesForCitizenResponderInterface responder

    static String serviceUrl = System.getProperty("service.clinicalProcess.listCertificatesForCitizenUrl")

    public InvanareHamtarListaAvIntyg() {
        String url = serviceUrl ? serviceUrl : baseUrl + "list-certificates-for-citizen/v1.0"
        responder = createClient(ListCertificatesForCitizenResponderInterface.class, url)
    }

    String personnummer
    String typ

    List intygMeta
    def resultat

    private ListCertificatesForCitizenResponseType response

    def execute() {
        def request = new ListCertificatesForCitizenType()
        request.personId = personnummer
        request.utlatandeTyp = [typ]

        response = responder.listCertificatesForCitizen(logicalAddress.value, request)
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
        intygMeta.collect { it.certificateId }.sort { it }
    }
}
