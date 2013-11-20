package se.inera.certificate.spec
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType
import se.inera.certificate.spec.util.WsClientFixture
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
        request.nationalIdentityNumber = personnummer
        request.careUnit.add(enhet)

        response = responder.listCertificatesForCare(LOGICAL_ADDRESS, request)
        switch (response.result.resultCode) {
            case ResultCodeType.OK:
                intygMeta = response.meta
                resultat = "OK"
                break
            default:
                resultat = "[${response.result.resultCode.toString()}] - ${response.result.resultText}"
        }
    }


    public String resultat() {
        resultat
    }

    /* @XmlElement(required = true)
        protected String certificateId;
        @XmlElement(required = true)
        protected String certificateType;
        @XmlElement(required = true, type = String.class)
        @XmlJavaTypeAdapter(Adapter2 .class)
        @XmlSchemaType(name = "date")
        protected LocalDate validFrom;
        @XmlElement(required = true, type = String.class)
        @XmlJavaTypeAdapter(Adapter2 .class)
        @XmlSchemaType(name = "date")
        protected LocalDate validTo;
        @XmlElement(required = true)
        protected String issuerName;
        @XmlElement(required = true)
        protected String facilityName;
        @XmlElement(required = true, type = String.class)
        @XmlJavaTypeAdapter(Adapter2 .class)
        @XmlSchemaType(name = "date")
        protected LocalDate signDate;
        @XmlElement(required = true)
        protected String available;
        protected List<CertificateStatusType> status;
        @XmlAnyElement(lax = true)
        protected List<Object> any;    */


    def intyg() {
        intygMeta.collect { it.certificateId }
    }
}
