package se.inera.certificate.integration.stub;

import static se.inera.certificate.integration.util.ResultTypeUtil.errorResult;
import static se.inera.certificate.integration.util.ResultTypeUtil.okResult;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ObjectFactory;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.exception.CertificateValidationException;
import se.inera.certificate.exception.ServerException;
import se.inera.certificate.integration.module.ModuleApiFactory;
import se.inera.certificate.integration.module.exception.ModuleNotFoundException;
import se.inera.certificate.integration.util.IdUtil;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.TransportModelHolder;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.certificate.modules.support.api.exception.ModuleValidationException;

/**
 * @author par.wenaker
 */
@Transactional
@WebServiceProvider(targetNamespace = "urn:riv:clinicalprocess:healthcond:certificate:RegisterCertificate:1:rivtabp21", serviceName = "RegisterCertificateResponderService", wsdlLocation = "schemas/clinicalprocess_healthcond_certificate/interactions/RegisterCertificateInteraction/RegisterCertificateInteraction_1.0_RIVTABP21.wsdl")
public class RegisterCertificateResponderStub implements RegisterCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterCertificateResponderStub.class);

    private ObjectFactory objectFactory;

    @Autowired
    private FkMedicalCertificatesStore fkMedicalCertificatesStore;

    @Autowired
    private ModuleApiFactory moduleApiFactory;

    private JAXBContext jaxbContext;

    @PostConstruct
    public void initializeJaxbContext() throws JAXBException {
        jaxbContext = JAXBContext.newInstance(UtlatandeType.class);
        objectFactory = new ObjectFactory();
    }

    @Override
    public RegisterCertificateResponseType registerCertificate(String logicalAddress, RegisterCertificateType request) {

        RegisterCertificateResponseType response = new RegisterCertificateResponseType();

        try {
            String type = request.getUtlatande().getTypAvUtlatande().getCode();
            ModuleApi endpoint = moduleApiFactory.getModuleEntryPoint(type).getModuleApi();

            validate(request, endpoint);
            String id = IdUtil.generateStringId(request.getUtlatande().getUtlatandeId());

            Map<String, String> props = new HashMap<>();
            props.put("Personnummer", request.getUtlatande().getPatient().getPersonId().getExtension());
            props.put("Makulerad", "NEJ");

            LOGGER.info(request.getUtlatande().getTypAvUtlatande().getCode() + " - STUB Received request");
            fkMedicalCertificatesStore.addCertificate(id, props);
        } catch (CertificateValidationException e) {
            response.setResult(errorResult(ErrorIdType.VALIDATION_ERROR, e.getMessage()));
            return response;
        } catch (JAXBException e) {
            response.setResult(errorResult(ErrorIdType.APPLICATION_ERROR, "Unable to marshal certificate information"));
            return response;
        } catch (ModuleNotFoundException e) {
            response.setResult(errorResult(ErrorIdType.APPLICATION_ERROR, "Could not find module for certificate"));
            return response;
        }
        response.setResult(okResult());
        return response;
    }

    private String xmlToString(UtlatandeType utlatandeType) throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        JAXBElement<UtlatandeType> requestElement = objectFactory
                .createUtlatande(utlatandeType);
        jaxbContext.createMarshaller().marshal(requestElement, stringWriter);
        return stringWriter.toString();
    }

    protected void validate(RegisterCertificateType registerCertificate, ModuleApi module) throws JAXBException,
            CertificateValidationException {
        try {
            String transportXml = xmlToString(registerCertificate.getUtlatande());
            module.unmarshall(new TransportModelHolder(transportXml));

        } catch (ModuleValidationException e) {
            throw new CertificateValidationException(e.getValidationEntries());

        } catch (ModuleException e) {
            String message = String.format("Failed to validate certificate for certificate type '%s'", registerCertificate.getUtlatande()
                    .getTypAvUtlatande().getCode());
            LOGGER.error(message);
            throw new ServerException(message, e);
        }
    }
}
