package se.inera.certificate.integration;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 */
@WebService(targetNamespace = "urn:riv:clinicalprocess:healtcond:certificate:RegisterMedicalCertificate:1:rivtabp20", name = "RegisterMedicalCertificateResponderInterface")
@ServiceMode(value=Service.Mode.MESSAGE)
public class RegisterMedicalCertificateResponderImpl extends RegisterCertificateBase implements Provider<SOAPMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterMedicalCertificateResponderImpl.class);

    private static final QName BODY_NAME = new QName("urn:riv:clinicalprocess:healtcond:certificate:1", "utlatande");

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
            NodeList nodes = document.getElementsByTagNameNS("urn:riv:clinicalprocess:healtcond:certificate:1", "typAvUtlatande");
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
