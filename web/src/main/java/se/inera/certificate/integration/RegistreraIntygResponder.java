package se.inera.certificate.integration;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistreraIntygResponder.class);

    @Autowired
    private ModuleRestApiFactory moduleRestApiFactory;

    @Autowired
    private CertificateService certificateService;

    @Override
    public void registreraIntyg(Holder<se.inera.certificate.common.v1.Utlatande> utlatande) {
        String type = utlatande.value.getTypAvUtlatande().getCode();

        String externalJson = unmarshall(type, utlatande.value);

        Utlatande commonUtlatande = null;
        try {
            commonUtlatande = new CustomObjectMapper().readValue(externalJson, Utlatande.class);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        certificateService.storeCertificate(commonUtlatande, externalJson);
    }

    private String unmarshall(String type, se.inera.certificate.common.v1.Utlatande utlatande) {

        ModuleRestApi endpoint = moduleRestApiFactory.getModuleRestService(type);

        Response response = endpoint.unmarshall(utlatande);

        switch (response.getStatus()) {
            case BAD_REQUEST:
                try {
                    InputStream inputStream = (InputStream) response.getEntity();
                    String validationErrorMessage = IOUtils.toString(inputStream);
                    throw new ValidationException(validationErrorMessage);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read response for validation of '" + type + "' certificate.", e);
                }
            case OK:
                InputStream inputStream = (InputStream) response.getEntity();
                try {
                    String json = IOUtils.toString(inputStream);
                    return json;
                } catch (IOException e) {
                    Throwables.propagate(e);
                }
                break;
        }
        String errorMessage = "Failed to validate certificate for certificate type '" + type + "'. HTTP status code is " + response.getStatus();
        LOGGER.error(errorMessage);
        throw new ValidationException(errorMessage);
    }

    private void validate(String type, Utlatande utlatande) {

        ModuleRestApi endpoint = moduleRestApiFactory.getModuleRestService(type);

        Response response = endpoint.validate(utlatande);

        switch (response.getStatus()) {
            case BAD_REQUEST:
                try {
                    InputStream inputStream = (InputStream) response.getEntity();
                    String validationErrorMessage = IOUtils.toString(inputStream);
                    throw new ValidationException(validationErrorMessage);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read response for validation of '" + type + "' certificate.", e);
                }
            case OK:
                break;
            default:
                String errorMessage = "Failed to validate certificate for certificate type '" + type + "'. HTTP status code is " + response.getStatus();
                LOGGER.error(errorMessage);
                throw new ValidationException(errorMessage);
        }
    }
}
