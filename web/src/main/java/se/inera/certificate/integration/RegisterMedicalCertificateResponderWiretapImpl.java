package se.inera.certificate.integration;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

import com.google.common.base.Throwables;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.service.CertificateService;

/**
 * @author andreaskaltenbach
 */
@WebServiceProvider(targetNamespace = "urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificate:3:rivtabp20", serviceName = "RegisterMedicalCertificateResponderService", wsdlLocation = "schemas/v3/RegisterMedicalCertificateInteraction/RegisterMedicalCertificateInteraction_3.0_rivtabp20.wsdl")
@ServiceMode(value= Service.Mode.MESSAGE)
public class RegisterMedicalCertificateResponderWiretapImpl extends RegisterMedicalCertificateResponderProvider
        implements Provider<SOAPMessage> {

    @Autowired
    private CertificateService certificateService;

    @Override
    Document convertAndPersist(SOAPMessage soapMessage) {
        Document document = super.convertAndPersist(soapMessage);

        // extract personnummer & certificate ID and explicitly set status SENT for Försäkringskassan
        try {
            Node patientNode = document.getElementsByTagNameNS("urn:riv:insuranceprocess:healthreporting:2",
                    "person-id").item(0);
            String personnummer = patientNode.getAttributes().getNamedItem("extension").getNodeValue().trim();

            Node certificateIdNode = document.getElementsByTagNameNS(
                    "urn:riv:insuranceprocess:healthreporting:mu7263:3", "lakarutlatande-id").item(0);
            String certificateId = certificateIdNode.getTextContent();

            certificateService.setCertificateState(personnummer, certificateId, "FK", CertificateState.SENT,
                    new LocalDateTime());

        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        return document;
    }
}
