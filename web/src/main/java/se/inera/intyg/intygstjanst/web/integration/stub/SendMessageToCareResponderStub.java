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

import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.intygstjanst.web.integration.converter.SendMessageToCareConverter;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultType;

@Component
@Transactional
@SchemaValidation
public class SendMessageToCareResponderStub implements SendMessageToCareResponderInterface {
    private Logger logger = LoggerFactory.getLogger(SendMessageToCareResponderStub.class);

    @Autowired
    private SendMessageToCareConverter converter;

    @Autowired
    private SendMessageToCareStorage storage;

    @Override
    public SendMessageToCareResponseType sendMessageToCare(String logicalAddress, SendMessageToCareType parameters) {
        SendMessageToCareResponseType response = new SendMessageToCareResponseType();
        ResultType resultType = new ResultType();
        try {
            storeMessage(parameters);
            logger.info("STUB Received question concerning certificate with id: " + parameters.getIntygsId().getExtension());
            resultType.setResultCode(ResultCodeType.OK);
        } catch (JAXBException e) {
            resultType.setResultCode(ResultCodeType.ERROR);
            resultType.setResultText("Error occurred when marshalling message to xml. " + e.getMessage());
            response.setResult(resultType);
            return response;
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
        response.setResult(resultType);
        return response;
    }

    private String marshalCertificate(SendMessageToCareType parameters) throws JAXBException {
        return converter.convertToXmlString(parameters);
    }

    public void storeMessage(SendMessageToCareType sendMessageToCareType) throws JAXBException {
        String certificateId = sendMessageToCareType.getIntygsId().getExtension();
        String messageId = sendMessageToCareType.getMeddelandeId();
        String xmlBlob = marshalCertificate(sendMessageToCareType);
        storage.addMessage(certificateId, messageId, xmlBlob);
    }

    public Map<Pair<String, String>, String> getAllMessages() {
        return storage.getAllMessages();
    }

    public List<String> getMessagesForCertificateId(String intygsIdNo1) {
        return storage.getMessagesForCertificateId(intygsIdNo1);
    }

}