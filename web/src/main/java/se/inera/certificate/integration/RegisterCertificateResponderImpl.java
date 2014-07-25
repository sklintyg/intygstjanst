package se.inera.certificate.integration;

import static se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType.VALIDATION_ERROR;

import java.io.StringWriter;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ObjectFactory;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeId;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.exception.CertificateAlreadyExistsException;
import se.inera.certificate.exception.CertificateValidationException;
import se.inera.certificate.integration.util.IdUtil;
import se.inera.certificate.integration.util.ResultTypeUtil;
import se.inera.certificate.logging.LogMarkers;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.StatisticsService;

import com.google.common.base.Throwables;

@SchemaValidation
public class RegisterCertificateResponderImpl implements RegisterCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterCertificateResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private StatisticsService statisticsService;

    private ObjectFactory objectFactory;

    private JAXBContext jaxbContext;

    @PostConstruct
    public void initializeJaxbContext() throws JAXBException {
        jaxbContext = JAXBContext.newInstance(UtlatandeType.class);
        objectFactory = new ObjectFactory();
    }

    @Override
    public RegisterCertificateResponseType registerCertificate(String logicalAddress,
            RegisterCertificateType registerCertificate) {
        RegisterCertificateResponseType response = new RegisterCertificateResponseType();

        // Extract document type and xml
        String type = registerCertificate.getUtlatande().getTypAvUtlatande().getCode();

        try {
            String xml = xmlToString(registerCertificate);
            Certificate certificate = certificateService.storeCertificate(xml, type, false);
            response.setResult(ResultTypeUtil.okResult());
            String certificateId = extractId(registerCertificate);
            LOGGER.info(LogMarkers.MONITORING, certificateId + " registered");
            statisticsService.created(certificate);

        } catch (CertificateAlreadyExistsException e) {
            response.setResult(ResultTypeUtil.infoResult("Certificate already exists"));
            String certificateId = extractId(registerCertificate);
            String issuedBy =  registerCertificate.getUtlatande().getSkapadAv().getEnhet().getEnhetsId().getExtension();
            LOGGER.warn(LogMarkers.VALIDATION, "Validation warning for intyg " + certificateId + " issued by " + issuedBy + ": Certificate already exists - ignored.");

        } catch (CertificateValidationException e) {
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

    private String extractId(RegisterCertificateType registerCertificate) {
        UtlatandeId utlatandeId = registerCertificate.getUtlatande().getUtlatandeId();
        return IdUtil.generateStringId(utlatandeId);
    }

    private String xmlToString(RegisterCertificateType registerCertificate) throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        JAXBElement<UtlatandeType> utlatandeElement = objectFactory.createUtlatande(registerCertificate.getUtlatande());
        jaxbContext.createMarshaller().marshal(utlatandeElement, stringWriter);
        return stringWriter.toString();
    }
}
