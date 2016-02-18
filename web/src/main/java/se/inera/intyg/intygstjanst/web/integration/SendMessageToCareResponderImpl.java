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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import se.inera.intyg.intygstjanst.persistence.model.dao.SendMessageToCare;
import se.inera.intyg.intygstjanst.web.integration.converter.SendMessageToCareConverter;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.SendMessageToCareService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultType;

public class SendMessageToCareResponderImpl implements SendMessageToCareResponderInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageToCareResponderImpl.class);
    private String errorMessage = "Could not convert message of type SendMessageToCareType to its ORM object. ";

    @Autowired
    private MonitoringLogService logService;

    @Autowired
    private SendMessageToCareConverter converter;

    @Autowired
    private SendMessageToCareService sendMessageToCareService;

    @Autowired
    @Qualifier("sendMessageToCareClient")
    private SendMessageToCareResponderInterface sendMessageToCareResponder;

    @Override
    public SendMessageToCareResponseType sendMessageToCare(String logicalAddress, SendMessageToCareType parameters) {
        SendMessageToCareResponseType response = new SendMessageToCareResponseType();
        SendMessageToCare sendMessage;
        ResultType resultType = new ResultType();
        try {
            sendMessage = converter.convertSendMessageToCare(parameters);
            sendMessageToCareResponder.sendMessageToCare(parameters.getLogiskAdressMottagare().getExtension(), parameters);
            resultType.setResultCode(ResultCodeType.OK);
            sendMessageToCareService.processIncomingSendMessageToCare(sendMessage);
            logService.logSendMessageToCareReceived(parameters.getIntygsId().getExtension(), parameters.getLogiskAdressMottagare().getExtension());
        } catch (JAXBException e) {
            LOGGER.error(errorMessage);
            resultType.setResultCode(ResultCodeType.ERROR);
            resultType.setResultText(errorMessage + "Error code " + e.getErrorCode() + " " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Could not convert message of type SendMessageToCareType to its ORM object. ");
            resultType.setResultCode(ResultCodeType.ERROR);
            resultType.setResultText(e.getMessage());
            e.printStackTrace();
        }
        response.setResult(resultType);
        return response;
    }

}
