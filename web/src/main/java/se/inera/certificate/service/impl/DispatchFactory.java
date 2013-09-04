package se.inera.certificate.service.impl;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;

/**
 * @author andreaskaltenbach
 */
public interface DispatchFactory {
    Dispatch<SOAPMessage> dispatchForRegisterMedicalCertificate();
}
