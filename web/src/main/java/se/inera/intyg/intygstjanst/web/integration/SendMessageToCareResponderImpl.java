/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
import se.inera.intyg.common.support.integration.converter.util.ResultTypeUtil;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.inera.intyg.intygstjanst.web.integration.converter.ArendeConverter;
import se.inera.intyg.intygstjanst.web.integration.validator.SendMessageToCareValidator;
import se.inera.intyg.intygstjanst.web.service.ArendeService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

@SchemaValidation
public class SendMessageToCareResponderImpl implements SendMessageToCareResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageToCareResponderImpl.class);

    @Autowired
    private MonitoringLogService logService;

    @Autowired
    private ArendeService arendeService;

    @Autowired
    protected StatisticsService statisticsService;

    @Autowired
    private SendMessageToCareValidator validator;

    @Autowired
    @Qualifier("sendMessageToCareClient")
    private SendMessageToCareResponderInterface sendMessageToCareResponder;

    @Override
    @PrometheusTimeMethod
    public SendMessageToCareResponseType sendMessageToCare(String logicalAddress, SendMessageToCareType parameters) {
        List<String> validationErrors = validator.validateSendMessageToCare(parameters);
        if (!validationErrors.isEmpty()) {
            SendMessageToCareResponseType response = new SendMessageToCareResponseType();
            String resultText = "Validation of SendMessageToCare failed for message with question id " + parameters.getMeddelandeId()
                + " and certificate id " + parameters.getIntygsId().getExtension() + ". " + validationErrors;
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.VALIDATION_ERROR, resultText));
            LOGGER.error(resultText);
            return response;
        }

        SendMessageToCareResponseType response = sendMessageToCareResponder.sendMessageToCare(parameters.getLogiskAdressMottagare(),
            parameters);

        if (response.getResult().getResultCode() != ResultCodeType.ERROR) {
            logService.logSendMessageToCareReceived(parameters.getMeddelandeId(), parameters.getLogiskAdressMottagare());
            try {
                Arende arende = ArendeConverter.convertSendMessageToCare(parameters);
                statisticsService.messageSent(arende.getMeddelande(), arende.getMeddelandeId(), arende.getAmne());
                arendeService.processIncomingMessage(arende);
            } catch (Exception e) {
                LOGGER.error("Could not save information about request of type SendMessageToCare with question id "
                        + "{} and certificate id {}. {}", parameters.getMeddelandeId(), parameters.getIntygsId().getExtension(),
                    e.getMessage());
            }
        } else {
            LOGGER.error("SendMessageToCare failed for message with question id {} and certificate id {}. {} {} {}",
                parameters.getMeddelandeId(),
                parameters.getIntygsId().getExtension(),
                response.getResult().getResultCode(),
                response.getResult().getErrorId(),
                response.getResult().getResultText());
        }

        return response;
    }

}
