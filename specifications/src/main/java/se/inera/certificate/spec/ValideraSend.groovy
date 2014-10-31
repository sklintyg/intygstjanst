



package se.inera.certificate.spec

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource

import se.inera.certificate.spec.util.FitnesseHelper
import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.v1.rivtabp20.SendMedicalCertificateResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType

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
        this(WsClientFixture.LOGICAL_ADDRESS)
    }

    public ValideraSend(String logiskAddress) {
        super(logiskAddress)
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
