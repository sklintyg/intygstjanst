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

import jakarta.xml.bind.JAXBException;
import java.util.List;
import java.util.Map;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.intygstjanst.web.integration.converter.ArendeConverter;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

@SchemaValidation
public class SendMessageToRecipientResponderStub implements SendMessageToRecipientResponderInterface {

    private Logger logger = LoggerFactory.getLogger(SendMessageToCareResponderStub.class);

    @Autowired
    private SendMessageToCareStorage storage;

    @Override
    public SendMessageToRecipientResponseType sendMessageToRecipient(String logicalAddress, SendMessageToRecipientType parameters) {
        SendMessageToRecipientResponseType response = new SendMessageToRecipientResponseType();
        ResultType resultType = new ResultType();
        try {
            storeMessage(parameters, logicalAddress);
            logger.info("STUB Received question concerning certificate with id: " + parameters.getIntygsId().getExtension());
            resultType.setResultCode(ResultCodeType.OK);
        } catch (JAXBException e) {
            resultType.setResultCode(ResultCodeType.ERROR);
            resultType.setResultText("Error occurred when marshalling message to xml. " + e.getMessage());
            response.setResult(resultType);
            return response;
        } catch (Exception e) {
            logger.error("Error: {}", e);
            throw e;
        }
        response.setResult(resultType);
        return response;
    }

    private String marshalCertificate(SendMessageToRecipientType parameters) throws JAXBException {
        return ArendeConverter.convertToXmlString(parameters);
    }

    public void storeMessage(SendMessageToRecipientType sendMessageToCareType, String logicalAddress) throws JAXBException {
        String certificateId = sendMessageToCareType.getIntygsId().getExtension();
        String messageId = sendMessageToCareType.getMeddelandeId();
        String xmlBlob = marshalCertificate(sendMessageToCareType);
        storage.addMessage(certificateId, messageId, logicalAddress, xmlBlob);
    }

    public Map<SendMessageToCareStorage.MessageKey, String> getAllMessages() {
        return storage.getAllMessages();
    }

    public List<String> getMessagesForCertificateId(String intygsIdNo1) {
        return storage.getMessagesForCertificateId(intygsIdNo1);
    }

    // private static final Logger LOG = LoggerFactory.getLogger(SendMessageToRecipientResponderStub.class);
    //
    // @Override
    // public SendMessageToRecipientResponseType sendMessageToRecipient(String logicalAddress,
    // SendMessageToRecipientType parameters) {
    // LOG.info("\n*********************************************************************************\n"
    // + " SendMessageToRecipient to address '{}' recieved for intyg '{}' meddelandeId '{}'.\n"
    // + "*********************************************************************************",
    // logicalAddress, parameters.getIntygsId().getExtension(), parameters.getMeddelandeId());
    // SendMessageToRecipientResponseType response = new SendMessageToRecipientResponseType();
    // response.setResult(ResultTypeUtil.okResult());
    // return response;
    // }

}
