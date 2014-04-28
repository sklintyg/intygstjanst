package se.inera.certificate.integration;

import java.io.StringWriter;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.exception.CertificateAlreadyExistsException;
import se.inera.certificate.integration.util.ResultOfCallUtil;
import se.inera.certificate.integration.validator.RegisterMedicalCertificateRequestValidator;
import se.inera.certificate.integration.validator.ValidationException;
import se.inera.certificate.logging.LogMarkers;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.StatisticsService;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.ObjectFactory;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;

import com.google.common.base.Throwables;

/**
 *
 */
public class RegisterMedicalCertificateLegacyResponderProvider implements RegisterMedicalCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RegisterMedicalCertificateLegacyResponderProvider.class);

    private static final String FK7263 = "fk7263";

    private Marshaller marshaller;
    private ObjectFactory objectFactory;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private StatisticsService statisticsService;

    @PostConstruct
    public void initializeJaxbContext() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class);
        marshaller = jaxbContext.createMarshaller();
        objectFactory = new ObjectFactory();
    }

    @Override
    public RegisterMedicalCertificateResponseType registerMedicalCertificate(AttributedURIType logicalAddress,
            RegisterMedicalCertificateType registerMedicalCertificate) {
        RegisterMedicalCertificateResponseType response = new RegisterMedicalCertificateResponseType();

        try {
            new RegisterMedicalCertificateRequestValidator(registerMedicalCertificate).validateAndCorrect();
            String xml = xmlToString(registerMedicalCertificate);
            // FK7263 is the only certificate using the legacy format so we can hard code the type.
            Certificate certificate = certificateService.storeCertificate(xml, FK7263);
            response.setResult(ResultOfCallUtil.okResult());
            String certificateId = registerMedicalCertificate.getLakarutlatande().getLakarutlatandeId();
            LOGGER.info(LogMarkers.MONITORING, certificateId + " registered");
            statisticsService.created(certificate);
        } catch (CertificateAlreadyExistsException e) {
            response.setResult(ResultOfCallUtil.infoResult("Certificate already exists"));
            String certificateId = registerMedicalCertificate.getLakarutlatande().getLakarutlatandeId();
            String issuedBy =  registerMedicalCertificate.getLakarutlatande().getSkapadAvHosPersonal().getEnhet().getEnhetsId().getExtension();
            LOGGER.warn(LogMarkers.VALIDATION, "Validation warning for intyg " + certificateId + " issued by " + issuedBy + ": Certificate already exists - ignored.");
        } catch (ValidationException e) {
            response.setResult(ResultOfCallUtil.failResult(e.getMessage()));
            LOGGER.error(LogMarkers.VALIDATION, e.getMessage());
        } catch (JAXBException e) {
            LOGGER.error("JAXB error in Webservice: ", e);
            Throwables.propagate(e);
        } catch (Exception e) {
            LOGGER.error("Error in Webservice: ", e);
            Throwables.propagate(e);
        }
        return response;
    }

    private String xmlToString(RegisterMedicalCertificateType registerMedicalCertificate) throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        JAXBElement<RegisterMedicalCertificateType> requestElement = objectFactory
                .createRegisterMedicalCertificate(registerMedicalCertificate);
        marshaller.marshal(requestElement, stringWriter);
        return stringWriter.toString();
    }
}
