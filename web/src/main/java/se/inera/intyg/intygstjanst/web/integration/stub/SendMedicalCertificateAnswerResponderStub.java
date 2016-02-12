/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.intygstjanst.web.integration.stub;

import java.io.StringWriter;

import javax.xml.bind.*;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;

import com.google.common.base.Throwables;

import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswer.rivtabp20.v1.SendMedicalCertificateAnswerResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.*;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;


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
