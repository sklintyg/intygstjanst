package se.inera.certificate.integration;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

/**
 *
 */
@WebServiceProvider(targetNamespace = "urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificate:3:rivtabp20", serviceName = "RegisterMedicalCertificateResponderService", wsdlLocation = "schemas/v3/RegisterMedicalCertificateInteraction/RegisterMedicalCertificateInteraction_3.0_rivtabp20.wsdl")
@ServiceMode(value=Service.Mode.MESSAGE)
public class RegisterMedicalCertificateResponderProvider extends RegisterCertificateBase implements Provider<SOAPMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterMedicalCertificateResponderProvider.class);

    private static final QName BODY_NAME = new QName("urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3", "RegisterMedicalCertificate");

    @Override
    public SOAPMessage invoke(SOAPMessage request) {
        SOAPMessage response = null;

        try {
            convertAndPersist(request);
            response = getReturnCode();
        } catch (Exception e) {
            LOGGER.warn("Error in Webservice: ", e);
            Throwables.propagate(e);
        }
        return response;
    }

    @Override
    String getType(Document document) {
        return "fk7263";
    }

    @Override
    QName getBodyElementQName() {
        return BODY_NAME;
    }

    SOAPMessage getReturnCode() {
        try {
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage message = factory.createMessage();

            SOAPBody body = message.getSOAPBody();
            SOAPBodyElement bodyElement = body.addBodyElement(new QName("urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3","RegisterMedicalCertificateResponse"));
            SOAPElement result = bodyElement.addChildElement(new QName("urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3", "result"));
            SOAPElement resultCode = result.addChildElement(new QName("urn:riv:insuranceprocess:healthreporting:2", "resultCode"));
            resultCode.setTextContent("OK");

            return message;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

}
