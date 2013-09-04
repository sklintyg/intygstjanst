package se.inera.certificate.service.impl;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author andreaskaltenbach
 */
@Component
public class DefaultDispatchFactory implements DispatchFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDispatchFactory.class);

    private static final QName REGISTER_MEDICAL_CERTIFICATE_SERVICE_QNAME = new QName(
            "urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificate:3:rivtabp20",
            "RegisterMedicalCertificateResponderService");

    private static final QName REGISTER_MEDICAL_CERTIFICATE_PORT_QNAME = new QName(
            "urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificate:3:rivtabp20",
            "RegisterMedicalCertificateResponderPort");

    @Autowired
    @Value("${registermedicalcertificatev3.endpoint.url}")
    private String endpointUrl;

    @Override
    public Dispatch<SOAPMessage> dispatchForRegisterMedicalCertificate() {
        try {
            Service service = Service.create(new URL(endpointUrl), REGISTER_MEDICAL_CERTIFICATE_SERVICE_QNAME);
            return service.createDispatch(REGISTER_MEDICAL_CERTIFICATE_PORT_QNAME, SOAPMessage.class,
                    Service.Mode.MESSAGE);
        } catch (MalformedURLException e) {
            String message = "Failed to create Dispatch for endpoint URL " + endpointUrl;
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

}
