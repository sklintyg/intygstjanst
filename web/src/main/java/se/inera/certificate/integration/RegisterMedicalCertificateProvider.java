package se.inera.certificate.integration;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

/**
 * @author andreaskaltenbach
 */
@WebServiceProvider(targetNamespace = "urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificate:3:rivtabp20", serviceName = "RegisterMedicalCertificateResponderService", wsdlLocation = "schemas/v3/RegisterMedicalCertificateInteraction/RegisterMedicalCertificateInteraction_3.0_rivtabp20.wsdl")
@ServiceMode(value= Service.Mode.MESSAGE)
public interface RegisterMedicalCertificateProvider extends Provider<SOAPMessage> {
}
