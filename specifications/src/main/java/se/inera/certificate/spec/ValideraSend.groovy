



package se.inera.certificate.spec

import se.inera.certificate.spec.util.FitnesseHelper
import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.rivtabp20.v1.SendMedicalCertificateResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource


/**
 *
 * @author andreaskaltenbach
 */
class ValideraSend extends WsClientFixture {

    private SendMedicalCertificateResponderInterface sendResponder

    static String serviceUrl = System.getProperty("service.sendCertificateUrl")

    String filnamn
    SendMedicalCertificateResponseType response

    public ValideraSend() {
        super()
    }

    public ValideraSend(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
        String url = serviceUrl ? serviceUrl : baseUrl + "send-certificate/v1.0"
        sendResponder = createClient(SendMedicalCertificateResponderInterface.class, url)
    }

    public void execute() {
        // read request template from file
        JAXBContext jaxbContext = JAXBContext.newInstance(SendMedicalCertificateRequestType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        SendMedicalCertificateRequestType request = unmarshaller.unmarshal(new StreamSource(new FileInputStream (FitnesseHelper.getFile(filnamn))),
                                                                        SendMedicalCertificateRequestType.class).getValue()

        response = sendResponder.sendMedicalCertificate(logicalAddress, request);
    }

    public String resultat() {
        resultAsString(response)
    }
}
