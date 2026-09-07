/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.intygstjanst.application.exception;

import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFactory;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.soap.SOAPFaultException;
import javax.xml.namespace.QName;

public class SoapFaultFactory {

  private static final String SOAP_11_NS = "http://schemas.xmlsoap.org/soap/envelope/";
  private static final QName SOAP_11_CLIENT_FAULT_CODE = new QName(SOAP_11_NS, "Client", "soap");
  private static final QName SOAP_11_SERVER_FAULT_CODE = new QName(SOAP_11_NS, "Server", "soap");

  private SoapFaultFactory() {}

  public static SOAPFaultException clientFault(String message) {
    return buildFault(message, SOAP_11_CLIENT_FAULT_CODE);
  }

  public static SOAPFaultException serverFault(String message) {
    return buildFault(message, SOAP_11_SERVER_FAULT_CODE);
  }

  private static SOAPFaultException buildFault(String message, QName faultCode) {
    try {
      final SOAPFault fault = SOAPFactory.newInstance().createFault(message, faultCode);
      return new SOAPFaultException(fault);
    } catch (SOAPException e) {
      throw new ServerException(message, e);
    }
  }
}
