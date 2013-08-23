package se.inera.certificate.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import intyg.registreraintyg._1.RegistreraIntygResponderInterface;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.integration.rest.ModuleRestApi;
import se.inera.certificate.integration.rest.ModuleRestApiFactory;
import se.inera.certificate.integration.validator.ValidationException;
import se.inera.certificate.model.Utlatande;
import se.inera.certificate.service.CertificateService;

import javax.ws.rs.core.Response;
import javax.xml.ws.Holder;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author andreaskaltenbach
 */
public class RegistreraIntygResponder implements RegistreraIntygResponderInterface {

    private static final int OK = 200;

    private static final int BAD_REQUEST = 400;

    private static final int NOT_FOUND = 404;

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistreraIntygResponder.class);

    private static ObjectMapper objectMapper = new CustomObjectMapper();

    @Autowired
    private ModuleRestApiFactory moduleRestApiFactory;

    @Autowired
    private CertificateService certificateService;

    @Override
    public void registreraIntyg(Holder<se.inera.certificate.common.v1.Utlatande> utlatande) {
        // Get type of intyg
        String type = utlatande.value.getTypAvUtlatande().getCode();

        // Get module external format from the module
        String externalJson = unmarshall(type, utlatande.value);

        // Map common parts of the module external format to the common format.
        Utlatande commonUtlatande = convertToCommonUtlatande(externalJson);

        // Store the intyg and meta data in a Certificate object
        certificateService.storeCertificate(commonUtlatande, externalJson);
    }

    private String unmarshall(String type, se.inera.certificate.common.v1.Utlatande utlatande) {

        ModuleRestApi endpoint = moduleRestApiFactory.getModuleRestService(type);

        Response response = endpoint.unmarshall(utlatande);

        String entityContent = readEntity(response);

        switch (response.getStatus()) {
            case NOT_FOUND:
                throw new ValidationException("Module of type "+ type +" not found, 404!");
            case BAD_REQUEST:
                throw new ValidationException(entityContent);
            case OK:
                return entityContent;
        }
        String errorMessage = "Failed to validate certificate for certificate type '" + type + "'. HTTP status code is " + response.getStatus();
        LOGGER.error(errorMessage);
        throw new ValidationException(errorMessage);
    }

    private String readEntity(Response response) {
        try {
            if (response.hasEntity()) {
                return IOUtils.toString((InputStream) response.getEntity());
            }
            return null;
        } catch (IOException ioe) {
            throw Throwables.propagate(ioe);
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
