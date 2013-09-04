/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Certificate (http://code.google.com/p/inera-certificate).
 *
 * Inera Certificate is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Certificate is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.certificate.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import se.inera.certificate.integration.exception.ExternalWebServiceCallFailedException;
import se.inera.certificate.integration.rest.ModuleRestApi;
import se.inera.certificate.integration.rest.ModuleRestApiFactory;
import se.inera.certificate.integration.util.RestUtils;
import se.inera.certificate.integration.util.XmlUtils;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.service.CertificateSenderService;
import se.inera.certificate.service.CertificateService;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;

import com.google.common.base.Throwables;

/**
 * @author andreaskaltenbach
 */
@org.springframework.stereotype.Service
public class CertificateSenderServiceImpl implements CertificateSenderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateSenderServiceImpl.class);

    private static MessageFactory MESSAGE_FACTORY;

    private static DocumentBuilder DOCUMENT_BUILDER;

    private static Unmarshaller UNMARSHALLER;

    private static final QName TO_ADDRESS = new QName("http://www.w3.org/2005/08/addressing", "To");

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private ModuleRestApiFactory moduleRestApiFactory;

    @Autowired
    @Value("${certificatesender.address.fk7263}")
    String logicalAddress;

    @Autowired
    private DispatchFactory dispatchFactory;

    @Override
    public void sendCertificate(Certificate certificate, String target) {

        Utlatande utlatande = certificateService.getLakarutlatande(certificate);

        ModuleRestApi moduleRestApi = moduleRestApiFactory.getModuleRestService(utlatande);
        Response response = moduleRestApi.marshall("1.0", certificate.getDocument());

        switch (response.getStatus()) {
        case 200:
            invokeReceiverService(RestUtils.entityAsString(response));
            break;
        default:
            String errorMessage = "Failed to unmarshal certificate for certificate type '" + certificate.getType()
                    + "'. HTTP status code is " + response.getStatus();
            LOGGER.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    static {
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        df.setNamespaceAware(true);
        try {
            DOCUMENT_BUILDER = df.newDocumentBuilder();
            MESSAGE_FACTORY = MessageFactory.newInstance();
            JAXBContext jaxbContext = JAXBContext.newInstance(ResultOfCall.class);
            UNMARSHALLER = jaxbContext.createUnmarshaller();

        } catch (ParserConfigurationException | SOAPException | JAXBException e) {
            Throwables.propagate(e);
        }
    }

    private void invokeReceiverService(String xml) {

        try {

            // create SOAP message which will be sent to receiver
            SOAPMessage soapMessage = MESSAGE_FACTORY.createMessage();
            Document doc = DOCUMENT_BUILDER.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
            soapMessage.getSOAPBody().addDocument(doc);

            // add WS addressing header
            SOAPHeaderElement header = soapMessage.getSOAPHeader().addHeaderElement(TO_ADDRESS);
            header.setTextContent(logicalAddress);

            // invoke receiver service
            Dispatch<SOAPMessage> dispatch = dispatchFactory.dispatchForRegisterMedicalCertificate();
            SOAPMessage response = dispatch.invoke(soapMessage);

            // extract ResultOfCall element
            Document soapBody = XmlUtils.documentFromSoapBody(response);

            String responseNamespace = soapBody.getChildNodes().item(0).getNamespaceURI();
            NodeList nodes = soapBody.getElementsByTagNameNS(responseNamespace, "result");
            if (nodes.getLength() == 0) {
                throw new RuntimeException(
                        "No " + responseNamespace + ":result element was found in SOAP response.");
            }
            JAXBElement<ResultOfCall> result = UNMARSHALLER.unmarshal(nodes.item(0), ResultOfCall.class);

            // check whether call was successful or not
            if (result.getValue().getResultCode() != ResultCodeEnum.OK) {
                throw new ExternalWebServiceCallFailedException(result.getValue());
            }


        } catch (IOException | SOAPException | SAXException | JAXBException | ParserConfigurationException e) {
            throw Throwables.propagate(e);
        }
    }
}
