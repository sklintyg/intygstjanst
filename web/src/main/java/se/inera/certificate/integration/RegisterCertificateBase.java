package se.inera.certificate.integration;

import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.integration.rest.ModuleRestApi;
import se.inera.certificate.integration.rest.ModuleRestApiFactory;
import se.inera.certificate.integration.util.RestUtils;
import se.inera.certificate.integration.util.XmlUtils;
import se.inera.certificate.validate.ValidationException;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.service.CertificateService;

/**
 *
 */
abstract class RegisterCertificateBase {

    private static final int OK = 200;

    private static final int BAD_REQUEST = 400;

    private static final int NOT_FOUND = 404;

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterCertificateBase.class);

    private static ObjectMapper objectMapper = new CustomObjectMapper();

    @Autowired
    private ModuleRestApiFactory moduleRestApiFactory;

    @Autowired
    private CertificateService certificateService;

    abstract String getType(Document document);

    abstract QName getBodyElementQName();

    Document convertAndPersist(SOAPMessage soapMessage) {
        try {

            // Get a DOM document from the SOAP body element.
            Document soapBody = XmlUtils.documentFromSoapBody(soapMessage, getBodyElementQName());

            // Extract document type and xml
            String type = getType(soapBody);
            String utlatandeXml = XmlUtils.getDocumentAsString(soapBody);

            // Unmarshall xml to module external JSON model
            String externalJson = unmarshall(type, utlatandeXml);

            // Convert module external JSON model to common Java model
            Utlatande utlatande = convertToCommonUtlatande(externalJson);

            // Store the intyg and meta data in a Certificate object
            Certificate certificate = certificateService.storeCertificate(utlatande, externalJson);

            // Save original xml to database
            certificateService.storeOriginalCertificate(utlatandeXml, certificate);

            return soapBody;

        } catch (TransformerException | SOAPException | ParserConfigurationException e) {
            throw Throwables.propagate(e);
        }
    }

    private String unmarshall(String type, String transportXml) {

        ModuleRestApi endpoint = moduleRestApiFactory.getModuleRestService(type);

        Response response = endpoint.unmarshall(transportXml);

        String entityContent = RestUtils.entityAsString(response);

        switch (response.getStatus()) {
            case NOT_FOUND:
                throw new ValidationException("Module of type " + type + " not found, 404!");
            case BAD_REQUEST:
                throw new ValidationException(entityContent);
            case OK:
                return entityContent;
            default:
                String errorMessage = "Failed to validate certificate for certificate type '" + type + "'. HTTP status code is " + response.getStatus();
                LOGGER.error(errorMessage);
                throw new ValidationException(errorMessage);
        }
    }

    private Utlatande convertToCommonUtlatande(String externalJson) {
        try {
            return objectMapper.readValue(externalJson, Utlatande.class);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
