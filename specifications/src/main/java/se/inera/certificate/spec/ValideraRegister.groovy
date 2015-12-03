

package se.inera.intyg.common.specifications.spec

import se.inera.intyg.common.specifications.spec.util.FitnesseHelper
import se.inera.intyg.common.specifications.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.rivtabp20.v3.RegisterMedicalCertificateResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource

/**
 *
 * @author andreaskaltenbach
 */
class ValideraRegister extends WsClientFixture {

    RegisterMedicalCertificateResponderInterface registerMedicalCertificateResponder

    static String serviceUrl = System.getProperty("service.registerMedicalCertificateUrl")

    public ValideraRegister() {
        super()
    }
    
    public ValideraRegister(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
        String url = serviceUrl ? serviceUrl : baseUrl + "register-certificate/v3.0"
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

        response = registerMedicalCertificateResponder.registerMedicalCertificate(logicalAddress, request);
    }

    public String resultat() {
        resultAsString(response)
    }
}
