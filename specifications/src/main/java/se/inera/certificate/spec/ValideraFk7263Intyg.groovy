

package se.inera.certificate.spec

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource

import se.inera.certificate.clinicalprocess.healthcond.certificate.registerMedicalCertificate.v1.RegisterMedicalCertificateResponderInterface
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerMedicalCertificate.v1.RegisterMedicalCertificateResponseType
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerMedicalCertificate.v1.RegisterMedicalCertificateType
import se.inera.certificate.spec.util.FitnesseHelper
import se.inera.certificate.spec.util.WsClientFixture

/**
 *
 * @author andreaskaltenbach
 */
class ValideraFk7263Intyg extends WsNewClientFixtureV1 {

    RegisterMedicalCertificateResponderInterface registerMedicalCertificateResponder

    static String serviceUrl = System.getProperty("service.registerMedicalCertificateUrl")

    public ValideraFk7263Intyg() {
        this(WsClientFixture.LOGICAL_ADDRESS)
    }
    
    public ValideraFk7263Intyg(String logiskAddress) {
        super(logiskAddress)
        String url = serviceUrl ? serviceUrl : baseUrl + "register-medical-certificate/v1.0"
        registerMedicalCertificateResponder = createClient(RegisterMedicalCertificateResponderInterface.class, url)
    }

    String filnamn
    
    RegisterMedicalCertificateResponseType response

    public void execute() {
        // read request template from file
        JAXBContext jaxbContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        RegisterMedicalCertificateType request = unmarshaller.unmarshal(new StreamSource(new FileInputStream (FitnesseHelper.getFile(filnamn))),
                                                                        RegisterMedicalCertificateType.class).getValue()

        response = registerMedicalCertificateResponder.registerMedicalCertificate(logicalAddress.toString(), request);
    }

    public String resultat() {
        resultAsString(response)
    }
}
