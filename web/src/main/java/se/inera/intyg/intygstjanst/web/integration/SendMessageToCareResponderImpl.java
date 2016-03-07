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
package se.inera.intyg.intygstjanst.web.integration;

import javax.xml.bind.JAXBException;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import se.inera.intyg.common.support.validate.CertificateValidationException;
import se.inera.intyg.intygstjanst.persistence.model.dao.SendMessageToCare;
import se.inera.intyg.intygstjanst.web.integration.converter.SendMessageToCareConverter;
import se.inera.intyg.intygstjanst.web.integration.validator.SendMessageToCareValidator;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.SendMessageToCareService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultType;

@SchemaValidation
public class SendMessageToCareResponderImpl implements SendMessageToCareResponderInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageToCareResponderImpl.class);

    @Autowired
    private MonitoringLogService logService;

    @Autowired
    private SendMessageToCareConverter converter;

    @Autowired
    private SendMessageToCareService sendMessageToCareService;

    @Autowired
    private SendMessageToCareValidator validator;

    @Autowired
    @Qualifier("sendMessageToCareClient")
    private SendMessageToCareResponderInterface sendMessageToCareResponder;

    @Override
    public SendMessageToCareResponseType sendMessageToCare(String logicalAddress, SendMessageToCareType parameters) {
        SendMessageToCareResponseType response = new SendMessageToCareResponseType();
        SendMessageToCare sendMessage;
        ResultType resultType = new ResultType();
        try {
            validator.validateSendMessageToCare(parameters);
            sendMessage = converter.convertSendMessageToCare(parameters);
            response = sendMessageToCareResponder
                    .sendMessageToCare(parameters.getLogiskAdressMottagare().getExtension(), parameters);
            if (response.getResult().getResultCode().equals(ResultCodeType.OK)) {
                LOGGER.debug("Converting to ORM object. " + sendMessage.toString());
                sendMessageToCareService.processIncomingSendMessageToCare(sendMessage);
                resultType.setResultCode(ResultCodeType.OK);
            }
            logService.logSendMessageToCareReceived(parameters.getMeddelandeId(), parameters.getLogiskAdressMottagare().getExtension());
        } catch (JAXBException e) {
            String errorMessage = "Could not marshal message of type SendMessageToCareType with meddelande id " + parameters.getMeddelandeId() + ": "
                    + e.getMessage() + "Error code " + e.getErrorCode() + " " + e.getMessage();
            setErrorResult(resultType, response, errorMessage);
        } catch (CertificateValidationException e) {
            String validationErrorMessage = "Validation of SendMessageToCareType failed for message with meddelandeid " + parameters.getMeddelandeId()
                    + ": " + e.getMessage();
            setErrorResult(resultType, response, validationErrorMessage);
        } catch (Exception e) {
            String generalErrorMessage = "Could not handle SendMessageToCareType with meddelande id " + parameters.getMeddelandeId() + ": "
                    + e.getMessage();
            setErrorResult(resultType, response, generalErrorMessage);
        }
        return response;
    }

    private void setErrorResult(ResultType resultType, SendMessageToCareResponseType response, String errorMessage) {
        LOGGER.error(errorMessage);
        resultType.setResultCode(ResultCodeType.ERROR);
        resultType.setResultText(errorMessage);
        response.setResult(resultType);
    }

}
