

package se.inera.certificate.spec

import net.sf.json.JSON
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import se.inera.certificate.spec.util.FitnesseHelper
import se.inera.certificate.spec.util.RestClientFixture
import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.getcertificate.rivtab20.v1.GetCertificateResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.getcertificateresponder.v1.CertificateType
import se.inera.ifv.insuranceprocess.healthreporting.getcertificateresponder.v1.GetCertificateRequestType
import se.inera.ifv.insuranceprocess.healthreporting.getcertificateresponder.v1.GetCertificateResponseType
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.rivtab20.v3.RegisterMedicalCertificateResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource


/**
 *
 * @author andreaskaltenbach
 */
class TransformeraIntyg extends WsClientFixture {

    RegisterMedicalCertificateResponderInterface registerMedicalCertificateResponder
    private GetCertificateResponderInterface getCertificateResponder
    
    def restClient
    
    static String registerServiceUrl = System.getProperty("service.registerMedicalCertificateUrl")
    static String getServiceUrl = System.getProperty("service.getCertificateUrl")
    
    public TransformeraIntyg() {
        super()
    }
    
    public TransformeraIntyg(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
        String registerUrl = registerServiceUrl ? registerServiceUrl : baseUrl + "register-certificate/v3.0"
        String getUrl = getServiceUrl ? getServiceUrl : baseUrl + "get-certificate/v1.0"
        registerMedicalCertificateResponder = createClient(RegisterMedicalCertificateResponderInterface.class, registerUrl)
        getCertificateResponder = createClient(GetCertificateResponderInterface.class, getUrl)
        restClient = RestClientFixture.createRestClient("${baseUrl}resources/")
        XMLUnit.setIgnoreWhitespace(true)
    }

    String filnamn
    
    RegisterMedicalCertificateResponseType registerResponse
    GetCertificateResponseType getResponse
    
    private def xmlDiff
    
    public void reset() {
        filnamn = null
        xmlDiff = null
    }
    
    public void execute() {
        File xmlFile = FitnesseHelper.getFile(filnamn)
        assert xmlFile.canRead()
        // read request template from file
        Unmarshaller unmarshaller = JAXBContext.newInstance(RegisterMedicalCertificateType.class).createUnmarshaller();
        Marshaller marshaller = JAXBContext.newInstance(GetCertificateResponseType.class).createMarshaller();
        RegisterMedicalCertificateType registerRequest = unmarshaller.unmarshal(new StreamSource(new FileInputStream (xmlFile)),
                                                                        RegisterMedicalCertificateType.class).getValue()

        registerResponse = registerMedicalCertificateResponder.registerMedicalCertificate(logicalAddress, registerRequest);
        assert registerResponse.result.resultCode != ResultCodeEnum.ERROR, registerResponse.result.errorText
        String id = registerRequest.lakarutlatande.lakarutlatandeId 
        String personnummer = registerRequest.lakarutlatande.patient.personId.extension 
        try {
            GetCertificateRequestType getRequest = new GetCertificateRequestType()
            getRequest.setNationalIdentityNumber(personnummer)
            getRequest.setCertificateId(id)
    
            getResponse = getCertificateResponder.getCertificate(logicalAddress, getRequest)
            assert getResponse.result.resultCode != ResultCodeEnum.ERROR, getResponse.result.errorText
            CertificateType certificate = getResponse.certificate
            JAXBContext payloadContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class);
            org.w3c.dom.Node node = (org.w3c.dom.Node) certificate.any[0]
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            StringWriter sw = new StringWriter()
            serializer.transform(new DOMSource(node), new StreamResult(sw));
            String responseXml = sw.getBuffer().toString();
            xmlDiff = new Diff(xmlFile.text, responseXml)
        } finally {
            restClient.delete(
                path: 'certificate/' + id,
                requestContentType: JSON
            )
        }
    }

    public String resultat() {
        assert xmlDiff.similar()
        "OK"
    }
}
