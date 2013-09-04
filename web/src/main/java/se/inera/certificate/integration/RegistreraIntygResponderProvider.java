package se.inera.certificate.integration;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 */
@WebServiceProvider(targetNamespace = "urn:intyg:RegistreraIntyg:1", serviceName = "RegistreraIntygResponderService", wsdlLocation = "schemas/v1/RegistreraIntygInteraction/RegistreraIntygInteraction_1.0.wsdl")
@ServiceMode(value=Service.Mode.MESSAGE)
public class RegistreraIntygResponderProvider extends RegisterCertificateBase implements Provider<SOAPMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistreraIntygResponderProvider.class);

    private static final QName BODY_NAME = new QName("urn:intyg:common-model:1", "utlatande");

    @Override
    public SOAPMessage invoke(SOAPMessage request) {

        SOAPMessage response = null;
        try {
            convertAndPersist(request);
            response = request;
        } catch (Exception e) {
            LOGGER.warn("Error in Webservice: ", e);
            Throwables.propagate(e);
        }
        return response;
    }

    @Override
    String getType(Document document) {
        try {
            NodeList nodes = document.getElementsByTagNameNS("urn:intyg:common-model:1", "typAvUtlatande");
            return nodes.item(0).getAttributes().getNamedItem("code").getNodeValue().trim();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    QName getBodyElementQName() {
        return BODY_NAME;
    }
}
