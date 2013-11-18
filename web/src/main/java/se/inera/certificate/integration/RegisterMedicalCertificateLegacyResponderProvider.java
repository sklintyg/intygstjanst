package se.inera.certificate.integration;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import java.io.StringWriter;

import com.google.common.base.Throwables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.exception.CertificateAlreadyExistsException;
import se.inera.certificate.integration.util.ResultOfCallUtil;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.StatisticsService;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.ObjectFactory;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;

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
            String xml = xmlToString(registerMedicalCertificate);
            Certificate certificate = certificateService.storeCertificate(xml, FK7263);
            response.setResult(ResultOfCallUtil.okResult());
            statisticsService.created(certificate);
        } catch (CertificateAlreadyExistsException e) {
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
        JAXBElement<RegisterMedicalCertificateType> requestElement = objectFactory
                .createRegisterMedicalCertificate(registerMedicalCertificate);
        marshaller.marshal(requestElement, stringWriter);
        return stringWriter.toString();
    }
}
