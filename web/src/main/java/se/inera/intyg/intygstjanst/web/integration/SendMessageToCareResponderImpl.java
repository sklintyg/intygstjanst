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

import java.util.List;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.v2.ResultTypeUtil;
import se.inera.intyg.intygstjanst.web.integration.converter.ArendeConverter;
import se.inera.intyg.intygstjanst.web.integration.validator.SendMessageToCareValidator;
import se.inera.intyg.intygstjanst.web.service.ArendeService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.*;
import se.riv.clinicalprocess.healthcond.certificate.v2.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;

@SchemaValidation
public class SendMessageToCareResponderImpl implements SendMessageToCareResponderInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageToCareResponderImpl.class);

    @Autowired
    private MonitoringLogService logService;

    @Autowired
    private ArendeService arendeService;

    @Autowired
    private SendMessageToCareValidator validator;

    @Autowired
    @Qualifier("sendMessageToCareClient")
    private SendMessageToCareResponderInterface sendMessageToCareResponder;

    @Override
    public SendMessageToCareResponseType sendMessageToCare(String logicalAddress, SendMessageToCareType parameters) {
        List<String> validationErrors = validator.validateSendMessageToCare(parameters);
        if (!validationErrors.isEmpty()) {
            SendMessageToCareResponseType response = new SendMessageToCareResponseType();
            String resultText = "Validation of SendMessageToCareType failed for message with meddelandeid " + parameters.getMeddelandeId() + ": "
                    + validationErrors.toString();
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.VALIDATION_ERROR, resultText));
            LOGGER.error(resultText);
            return response;
        }

        SendMessageToCareResponseType response = sendMessageToCareResponder.sendMessageToCare(parameters.getLogiskAdressMottagare(), parameters);

        if (response.getResult().getResultCode() != ResultCodeType.ERROR) {
            logService.logSendMessageToCareReceived(parameters.getMeddelandeId(), parameters.getLogiskAdressMottagare());
            try {
                arendeService.processIncomingMessage(ArendeConverter.convertSendMessageToCare(parameters));
            } catch (Exception e) {
                LOGGER.error("Could not save information about request of type SendMessageToCareType with meddelande id "
                        + parameters.getMeddelandeId() + ": " + e.getMessage());
            }
        }

        return response;
    }

}
