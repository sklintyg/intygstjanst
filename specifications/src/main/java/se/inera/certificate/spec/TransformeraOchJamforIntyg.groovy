

package se.inera.certificate.spec

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource

import net.sf.json.JSON
import org.custommonkey.xmlunit.XMLUnit

import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareRequestType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponderInterface
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.ObjectFactory
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateType
import se.inera.certificate.spec.util.FitnesseHelper
import se.inera.certificate.spec.util.RestClientFixture
import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType

import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum

/**
 *
 * @author andreaskaltenbachss
 */
class TransformeraOchJamforIntyg extends WsClientFixture {

    RegisterMedicalCertificateResponderInterface registerMedicalCertificateResponder
    private GetCertificateForCareResponderInterface getCertificateResponder

    def restClient

    static String registerServiceUrl = System.getProperty("service.registerMedicalCertificateUrl")
    static String getServiceUrl = System.getProperty("service.getCertificateUrl")

    public TransformeraOchJamforIntyg() {
        this(WsClientFixture.LOGICAL_ADDRESS)
    }

    public TransformeraOchJamforIntyg(String logiskAddress) {
        super(logiskAddress)
        String registerUrl = registerServiceUrl ? registerServiceUrl : baseUrl + "register-certificate/v3.0"
        String getUrl = getServiceUrl ? getServiceUrl : baseUrl + "get-certificate-for-care/v1.0"
        registerMedicalCertificateResponder = createClient(RegisterMedicalCertificateResponderInterface.class, registerUrl)
        getCertificateResponder = createClient(GetCertificateForCareResponderInterface.class, getUrl)
        restClient = RestClientFixture.createRestClient()
        XMLUnit.setIgnoreWhitespace(true)
    }

    String filnamn
    String forvantat

    RegisterMedicalCertificateResponseType registerResponse
    GetCertificateForCareResponseType getResponse

    private def xmlDiff

    public void reset() {
        filnamn = null
        forvantat = null
    }

    public void execute() {
        File xmlRegisterFile = FitnesseHelper.getFile(filnamn)
        File xmlExpectedFile = FitnesseHelper.getFile(forvantat)
        assert xmlRegisterFile.canRead()
        assert xmlExpectedFile.canRead()
        // read request template from file
        Unmarshaller unmarshaller = JAXBContext.newInstance(RegisterMedicalCertificateType.class).createUnmarshaller();

        Marshaller marshaller = JAXBContext.newInstance(RegisterCertificateType).createMarshaller();

        RegisterMedicalCertificateType registerRequest = unmarshaller.unmarshal(new StreamSource(new FileInputStream (xmlRegisterFile)),
                RegisterMedicalCertificateType.class).getValue()

        // read expected template from file
        Unmarshaller unmarshallerNewSchema = JAXBContext.newInstance(RegisterCertificateType.class).createUnmarshaller();
        RegisterCertificateType registerRequestNewSchema = unmarshallerNewSchema.unmarshal(new StreamSource(new FileInputStream (xmlRegisterFile)),
                RegisterCertificateType.class).getValue()

        registerResponse = registerMedicalCertificateResponder.registerMedicalCertificate(logicalAddress, registerRequest);
        assert registerResponse.result.resultCode != ResultCodeEnum.ERROR, registerResponse.result.errorText
        String id = registerRequest.lakarutlatande.lakarutlatandeId
        String personnummer = registerRequest.lakarutlatande.patient.personId.extension
        try {
            GetCertificateForCareRequestType getRequest = new GetCertificateForCareRequestType()
            getRequest.setCertificateId(id)

            getResponse = getCertificateResponder.getCertificateForCare("FK", getRequest)
            assert getResponse.result.resultCode != ResultCodeEnum.ERROR, getResponse.result.resultText

            RegisterCertificateType newRegister =
               new RegisterCertificateType()

            newRegister.setUtlatande(getResponse.certificate)

            ObjectFactory objectFactory = new ObjectFactory()

            JAXBElement<RegisterCertificateType> jaxbObject =
               objectFactory.createRegisterCertificate(newRegister)
            Writer sw = new StringWriter()
            marshaller.marshal(jaxbObject, sw)
            String responseXml = sw.toString()

            xmlDiff = XMLUnit.compareXML(xmlExpectedFile.text, responseXml)
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
