

package se.inera.certificate.spec

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource

import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponderInterface
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponseType
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateType
import se.inera.certificate.spec.util.FitnesseHelper
import se.inera.certificate.spec.util.WsClientFixture
import se.inera.certificate.spec.util.WsClientFixtureNyaKontraktet

/**
 *
 * @author andreaskaltenbach
 */
class ValideraRegisterMotNyaKontraktet extends WsClientFixtureNyaKontraktet {

    RegisterCertificateResponderInterface registerCertificateResponder

    static String serviceUrl = System.getProperty("service.clinicalProcess.registerCertificateUrl")

    public ValideraRegisterMotNyaKontraktet() {
        this(WsClientFixture.LOGICAL_ADDRESS)
    }
    
    public ValideraRegisterMotNyaKontraktet(String logiskAddress) {
        super(logiskAddress)
        String url = serviceUrl ? serviceUrl : baseUrl + "register-certificate/v1.0"
        registerCertificateResponder = createClient(RegisterCertificateResponderInterface.class, url)
    }

    String filnamn
    
    RegisterCertificateResponseType response

    public void execute() {
        // read request template from file
        JAXBContext jaxbContext = JAXBContext.newInstance(RegisterCertificateType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        RegisterCertificateType request = unmarshaller.unmarshal(new StreamSource(new FileInputStream (FitnesseHelper.getFile(filnamn))),
                                                                        RegisterCertificateType.class).getValue()

        response = registerCertificateResponder.registerCertificate(logicalAddress.toString(), request);
    }

    public String resultat() {
        resultAsString(response)
    }
}
