package se.inera.certificate.integration;

import javax.annotation.PostConstruct;
import javax.persistence.PersistenceException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

import com.google.common.base.Throwables;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerMedicalCertificate.v1.RegisterMedicalCertificateResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerMedicalCertificate.v1.RegisterMedicalCertificateResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.registerMedicalCertificate.v1.RegisterMedicalCertificateType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ObjectFactory;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType;
import se.inera.certificate.integration.util.ResultOfCallUtil;
import se.inera.certificate.service.CertificateService;

@SchemaValidation
public class RegisterMedicalCertificateResponderImpl implements RegisterMedicalCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterMedicalCertificateResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    private Marshaller marshaller;
    private ObjectFactory objectFactory;

    @PostConstruct
    public void initializeJaxbContext() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(UtlatandeType.class);
        marshaller = jaxbContext.createMarshaller();
        objectFactory = new ObjectFactory();
    }

    @Override
    public RegisterMedicalCertificateResponseType registerMedicalCertificate(AttributedURIType logicalAddress,
            RegisterMedicalCertificateType registerMedicalCertificate) {
        RegisterMedicalCertificateResponseType response = new RegisterMedicalCertificateResponseType();

        // Extract document type and xml
        String type = registerMedicalCertificate.getUtlatande().getTypAvUtlatande().getCode();

        try {
            String xml = xmlToString(registerMedicalCertificate);
            certificateService.storeCertificate(xml, type);
            response.setResult(ResultOfCallUtil.okResult());
        } catch (PersistenceException e) {
            response.setResult(ResultOfCallUtil.infoResult("Certificate already exists"));
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
        marshaller.marshal(utlatandeElement, stringWriter);
        return stringWriter.toString();
    }
}
