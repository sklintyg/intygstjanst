package se.inera.certificate.integration.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.certificate.exception.CertificateValidationException;
import se.inera.certificate.exception.ServerException;
import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.registry.ModuleNotFoundException;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.TransportModelHolder;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.certificate.modules.support.api.exception.ModuleValidationException;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.ObjectFactory;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;
import se.inera.ifv.insuranceprocess.healthreporting.utils.ResultOfCallUtil;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceProvider;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author par.wenaker
 */
@Transactional
@WebServiceProvider(targetNamespace = "urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificate:3:rivtabp20", serviceName = "RegisterMedicalCertificateResponderService", wsdlLocation = "schemas/v3/RegisterMedicalCertificateInteraction/RegisterMedicalCertificateInteraction_3.1_rivtabp20.wsdl")
public class RegisterMedicalCertificateResponderStub implements RegisterMedicalCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterMedicalCertificateResponderStub.class);
    private static final String FK7263 = "fk7263";

    private ObjectFactory objectFactory;

    private ModuleApi moduleApi;

    @Autowired
    private FkMedicalCertificatesStore fkMedicalCertificatesStore;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    private JAXBContext jaxbContext;

    @PostConstruct
    public void initializeJaxbContextAndModuleApi() throws JAXBException, ModuleNotFoundException {
        jaxbContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class);
        objectFactory = new ObjectFactory();
        // Since only FK7263 uses RegisterMedicalCertificateType we can hard code it here.
        moduleApi = moduleRegistry.getModuleApi(FK7263);
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
        } catch (CertificateValidationException e) {
            response.setResult(ResultOfCallUtil.failResult(e.getMessage()));
            return response;
        } catch (JAXBException e) {
            response.setResult(ResultOfCallUtil.failResult("Unable to marshal certificate information"));
            return response;
        }
        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }

    private String xmlToString(RegisterMedicalCertificateType registerMedicalCertificate) throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        JAXBElement<RegisterMedicalCertificateType> requestElement = objectFactory
                .createRegisterMedicalCertificate(registerMedicalCertificate);
        jaxbContext.createMarshaller().marshal(requestElement, stringWriter);
        return stringWriter.toString();
    }

    protected void validate(RegisterMedicalCertificateType registerMedicalCertificate) throws JAXBException, CertificateValidationException {
        try {
            String transportXml = xmlToString(registerMedicalCertificate);
            moduleApi.unmarshall(new TransportModelHolder(transportXml));

        } catch (ModuleValidationException e) {
            throw new CertificateValidationException(e.getValidationEntries());

        } catch (ModuleException e) {
            String message = String.format("Failed to validate certificate for certificate type '%s'", FK7263);
            LOGGER.error(message);

            throw new ServerException(message, e);
        }
    }
}
