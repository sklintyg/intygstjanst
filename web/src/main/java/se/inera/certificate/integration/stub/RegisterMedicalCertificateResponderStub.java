package se.inera.certificate.integration.stub;

import static se.inera.certificate.integration.util.ResultOfCallUtil.failResult;
import static se.inera.certificate.integration.util.ResultOfCallUtil.okResult;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.ws.WebServiceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.integration.rest.ModuleRestApi;
import se.inera.certificate.integration.rest.ModuleRestApiFactory;
import se.inera.certificate.integration.rest.exception.ModuleCallFailedException;
import se.inera.certificate.integration.util.RestUtils;
import se.inera.certificate.integration.util.ResultOfCallUtil;
import se.inera.certificate.integration.validator.ValidationException;
import se.inera.certificate.logging.LogMarkers;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.ObjectFactory;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;

import com.google.common.base.Throwables;

/**
 * @author par.wenaker
 */
@Transactional
@WebServiceProvider(targetNamespace = "urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificate:3:rivtabp20", serviceName = "RegisterMedicalCertificateResponderService", wsdlLocation = "schemas/v3/RegisterMedicalCertificateInteraction/RegisterMedicalCertificateInteraction_3.1_rivtabp20.wsdl")
public class RegisterMedicalCertificateResponderStub implements RegisterMedicalCertificateResponderInterface {

    private static final int OK = 200;
    private static final int BAD_REQUEST = 400;
    private static final int NOT_FOUND = 404;
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterMedicalCertificateResponderStub.class);
    private static final String FK7263 = "fk7263";

    private JAXBContext jaxbContext;
    private ObjectFactory objectFactory;
    private ModuleRestApi endpoint;

    @Autowired
    private FkMedicalCertificatesStore fkMedicalCertificatesStore;

    @Autowired
    private ModuleRestApiFactory moduleRestApiFactory;

    @PostConstruct
    public void initializeJaxbContext() throws JAXBException {
        jaxbContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class);
        objectFactory = new ObjectFactory();
        endpoint = moduleRestApiFactory.getModuleRestService(FK7263);
    }

    @Override
    public RegisterMedicalCertificateResponseType registerMedicalCertificate(AttributedURIType logicalAddress, RegisterMedicalCertificateType request) {

        RegisterMedicalCertificateResponseType response = new RegisterMedicalCertificateResponseType();

        try {
            validate(request);
            String id = request.getLakarutlatande().getLakarutlatandeId();

            Map<String, String> props = new HashMap<>();
            props.put("Personnummer", request.getLakarutlatande().getPatient().getPersonId().getExtension());
            props.put("Makulerad", "NEJ");

            LOGGER.info("STUB Received request");
            fkMedicalCertificatesStore.addCertificate(id, props);
        } catch (ValidationException e) {
            response.setResult(ResultOfCallUtil.failResult(e.getMessage()));
            return response;
        } catch (JAXBException e) {
            response.setResult(failResult("Unable to marshal certificate information"));
            return response;
        }
        response.setResult(okResult());
        return response;
    }

    private String xmlToString(RegisterMedicalCertificateType registerMedicalCertificate) throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        JAXBElement<RegisterMedicalCertificateType> requestElement = objectFactory
                .createRegisterMedicalCertificate(registerMedicalCertificate);
        jaxbContext.createMarshaller().marshal(requestElement, stringWriter);
        return stringWriter.toString();
    }

    protected void validate(RegisterMedicalCertificateType registerMedicalCertificate) throws JAXBException {
        String transportXml = xmlToString(registerMedicalCertificate);
        Response response = endpoint.unmarshall(transportXml);
        String entityContent = RestUtils.entityAsString(response);

        switch (response.getStatus()) {
        case NOT_FOUND:
            String errorMessage = "Module of type " + FK7263 + " not found, 404!";
            LOGGER.error(errorMessage);
            throw new ModuleCallFailedException("Module of type " + FK7263 + " not found, 404!", response);
        case BAD_REQUEST:
            throw new ValidationException(entityContent);
        case OK:
            break;
        default:
            String message = "Failed to validate certificate for certificate type '" + FK7263
                    + "'. HTTP status code is " + response.getStatus();
            LOGGER.error(message);
            throw new ModuleCallFailedException(message, response);
        }
    }

}
