/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import java.io.StringWriter;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.Amnetyp;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.rivtabp20.v1.SendMedicalCertificateQuestionResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.ObjectFactory;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.common.support.stub.MedicalCertificatesStore;

// CHECKSTYLE:OFF LineLength
// CHECKSTYLE:ON LineLength

/**
 * @author par.wenaker
 */
@Transactional
@SchemaValidation
public class SendMedicalCertificateQuestionResponderStub implements SendMedicalCertificateQuestionResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(SendMedicalCertificateQuestionResponderStub.class);

    private final JAXBContext jaxbContext;

    @Autowired
    private MedicalCertificatesStore medicalCertificatesStore;

    public SendMedicalCertificateQuestionResponderStub() {
        try {
            jaxbContext = JAXBContext.newInstance(SendMedicalCertificateQuestionType.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SendMedicalCertificateQuestionResponseType sendMedicalCertificateQuestion(AttributedURIType logicalAddress,
        SendMedicalCertificateQuestionType request) {

        SendMedicalCertificateQuestionResponseType response = new SendMedicalCertificateQuestionResponseType();

        try {
            String id = request.getQuestion().getLakarutlatande().getLakarutlatandeId();
            String meddelande = request.getQuestion().getFraga().getMeddelandeText();

            marshalCertificate(request);
            LOG.info("STUB Received question concerning certificate with id: " + id);
            if (request.getQuestion().getAmne().equals(Amnetyp.MAKULERING_AV_LAKARINTYG)) {
                medicalCertificatesStore.makulera(id, meddelande);
            }
        } catch (JAXBException e) {
            response.setResult(ResultOfCallUtil.failResult("Unable to marshal certificate information"));
            return response;
        } catch (Exception e) {
            LOG.error("STUB failed: {}", e);
            throw e;
        }
        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }

    private String marshalCertificate(SendMedicalCertificateQuestionType request) throws JAXBException {

        StringWriter stringWriter = new StringWriter();

        JAXBElement<SendMedicalCertificateQuestionType> jaxbElement = new ObjectFactory().createSendMedicalCertificateQuestion(request);

        jaxbContext.createMarshaller().marshal(jaxbElement, stringWriter);

        return stringWriter.toString();
    }

}
