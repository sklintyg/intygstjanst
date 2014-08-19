package se.inera.certificate.integration;

import static se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType.VALIDATION_ERROR;

import java.io.StringWriter;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.clinicalprocess.healthcond.certificate.registerMedicalCertificate.v1.RegisterMedicalCertificateResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerMedicalCertificate.v1.RegisterMedicalCertificateResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerMedicalCertificate.v1.RegisterMedicalCertificateType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ObjectFactory;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.exception.CertificateAlreadyExistsException;
import se.inera.certificate.integration.util.ResultTypeUtil;
import se.inera.certificate.integration.validator.ValidationException;
import se.inera.certificate.logging.LogMarkers;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.StatisticsService;

import com.google.common.base.Throwables;

@SchemaValidation
public class RegisterMedicalCertificateResponderImpl implements RegisterMedicalCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterMedicalCertificateResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    StatisticsService statisticsService;

    private JAXBContext jaxbContext;
    private ObjectFactory objectFactory;

    @PostConstruct
    public void initializeJaxbContext() throws JAXBException {
        jaxbContext = JAXBContext.newInstance(UtlatandeType.class);
        objectFactory = new ObjectFactory();
    }

    @Override
    public RegisterMedicalCertificateResponseType registerMedicalCertificate(String logicalAddress,
            RegisterMedicalCertificateType registerMedicalCertificate) {
        RegisterMedicalCertificateResponseType response = new RegisterMedicalCertificateResponseType();

        // Extract document type and xml
        String type = registerMedicalCertificate.getUtlatande().getTypAvUtlatande().getCode();

        try {
            String xml = xmlToString(registerMedicalCertificate);
            Certificate certificate = certificateService.storeCertificate(xml, type);
            response.setResult(ResultTypeUtil.okResult());
            String certificateId = registerMedicalCertificate.getUtlatande().getUtlatandeId().getRoot();
            LOGGER.info(LogMarkers.MONITORING, certificateId + " registered");
            statisticsService.created(certificate);
        } catch (CertificateAlreadyExistsException e) {
            response.setResult(ResultTypeUtil.infoResult("Certificate already exists"));
            String certificateId = registerMedicalCertificate.getUtlatande().getUtlatandeId().getRoot();
            String issuedBy =  registerMedicalCertificate.getUtlatande().getSkapadAv().getEnhet().getEnhetsId().getExtension();
            LOGGER.warn(LogMarkers.VALIDATION, "Validation warning for intyg " + certificateId +
                    " issued by " + issuedBy +": Certificate already exists - ignored.");
        } catch (ValidationException e) {
            response.setResult(ResultTypeUtil.errorResult(VALIDATION_ERROR, e.getMessage()));
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
        JAXBElement<UtlatandeType> utlatandeElement = objectFactory.createUtlatande(registerMedicalCertificate
                .getUtlatande());
        jaxbContext.createMarshaller().marshal(utlatandeElement, stringWriter);
        return stringWriter.toString();
    }
}
