package se.inera.certificate.integration.stub;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswer.v1.rivtabp20.SendMedicalCertificateAnswerResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.ObjectFactory;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType;
import se.inera.ifv.insuranceprocess.healthreporting.utils.ResultOfCallUtil;

import com.google.common.base.Throwables;

/**
 * @author par.wenaker
 */
@Transactional
@SchemaValidation
public class SendMedicalCertificateAnswerResponderStub implements
        SendMedicalCertificateAnswerResponderInterface {

    private Logger logger = LoggerFactory
            .getLogger(SendMedicalCertificateQuestionResponderStub.class);

    private final JAXBContext jaxbContext;

    @Autowired
    private FkMedicalCertificatesStore fkMedicalCertificatesStore;

    public SendMedicalCertificateAnswerResponderStub() {
        try {
            jaxbContext = JAXBContext
                    .newInstance(SendMedicalCertificateAnswerType.class);
        } catch (JAXBException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public SendMedicalCertificateAnswerResponseType sendMedicalCertificateAnswer(
            AttributedURIType logicalAddress,
            SendMedicalCertificateAnswerType request) {

        SendMedicalCertificateAnswerResponseType response = new SendMedicalCertificateAnswerResponseType();

        try {
            String id = request.getAnswer().getLakarutlatande()
                    .getLakarutlatandeId();

            marshalCertificate(request);
            logger.info("STUB Received answer concerning certificate with id: " + id);
        } catch (JAXBException e) {
            response.setResult(ResultOfCallUtil.failResult("Unable to marshal certificate information"));
            return response;
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }

    private String marshalCertificate(SendMedicalCertificateAnswerType request)
            throws JAXBException {

        StringWriter stringWriter = new StringWriter();

        JAXBElement<SendMedicalCertificateAnswerType> jaxbElement = new ObjectFactory()
                .createSendMedicalCertificateAnswer(request);

        jaxbContext.createMarshaller().marshal(jaxbElement, stringWriter);

        return stringWriter.toString();
    }

}
