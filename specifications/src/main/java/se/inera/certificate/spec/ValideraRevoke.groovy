



package se.inera.certificate.spec

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource

import se.inera.certificate.spec.util.FitnesseHelper
import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.v1.rivtabp20.RevokeMedicalCertificateResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType

/**
 *
 * @author andreaskaltenbach
 */
class ValideraRevoke extends WsClientFixture {

    private RevokeMedicalCertificateResponderInterface revokeResponder

    static String serviceUrl = System.getProperty("service.revokeCertificateUrl")

    String filnamn
    RevokeMedicalCertificateResponseType response

    public ValideraRevoke() {
        super()
    }

    public ValideraRevoke(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
        String url = serviceUrl ? serviceUrl : baseUrl + "revoke-certificate/v1.0"
        revokeResponder = createClient(RevokeMedicalCertificateResponderInterface.class, url)
    }

    public void execute() {
        // read request template from file
        JAXBContext jaxbContext = JAXBContext.newInstance(RevokeMedicalCertificateRequestType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        RevokeMedicalCertificateRequestType request = unmarshaller.unmarshal(new StreamSource(new FileInputStream (FitnesseHelper.getFile(filnamn))),
                                                                        RevokeMedicalCertificateRequestType.class).getValue()

        response = revokeResponder.revokeMedicalCertificate(logicalAddress, request);
    }

    public String resultat() {
        resultAsString(response)
    }
}
