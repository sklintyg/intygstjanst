package se.inera.certificate.integration;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.w3c.dom.Document;

import com.google.common.base.Throwables;

/**
 *
 */
@WebServiceProvider(targetNamespace = "urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificate:3:rivtabp20", serviceName = "RegisterMedicalCertificateResponderService", wsdlLocation = "schemas/v3/RegisterMedicalCertificateInteraction/RegisterMedicalCertificateInteraction_3.0_rivtabp20.wsdl")
@ServiceMode(value=Service.Mode.MESSAGE)
public class RegisterMedicalCertificateLegacyResponderProvider extends RegisterCertificateBase implements Provider<SOAPMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterMedicalCertificateLegacyResponderProvider.class);

    private static final QName BODY_NAME = new QName("urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3", "RegisterMedicalCertificate");

    @Override
    public SOAPMessage invoke(SOAPMessage request) {
        SOAPMessage response = null;

        try {
            convertAndPersist(request);
            response = getOKReturnMessage();
        } catch (DataIntegrityViolationException e) {
            response = getInfoReturnMessage("Certificate already exists");
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

    SOAPMessage getOKReturnMessage() {
        return getReturnMessage("OK", null, null);
    }
    
    SOAPMessage getInfoReturnMessage(String message) {
        return getReturnMessage("INFO", "infoText", message);
    }
    
    SOAPMessage getReturnMessage(String code, String messageType, String messageText) {
        try {
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage message = factory.createMessage();

            SOAPBody body = message.getSOAPBody();
            SOAPBodyElement bodyElement = body.addBodyElement(new QName("urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3","RegisterMedicalCertificateResponse"));
            SOAPElement result = bodyElement.addChildElement(new QName("urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:3", "result"));
            SOAPElement resultCode = result.addChildElement(new QName("urn:riv:insuranceprocess:healthreporting:2", "resultCode"));
            resultCode.setTextContent(code);
            if (messageType != null && messageText != null) {
                SOAPElement messageElement = result.addChildElement(new QName("urn:riv:insuranceprocess:healthreporting:2", messageType));
                messageElement.setTextContent(messageText);
            }

            return message;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

}
