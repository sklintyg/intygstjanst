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

import javax.xml.bind.JAXBException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.v2.ResultTypeUtil;
import se.inera.intyg.intygstjanst.web.integration.converter.ArendeConverter;
import se.inera.intyg.intygstjanst.web.integration.validator.SendMessageToRecipientValidator;
import se.inera.intyg.intygstjanst.web.service.ArendeService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v1.*;
import se.riv.clinicalprocess.healthcond.certificate.v2.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;

@SchemaValidation
public class SendMessageToRecipientResponderImpl implements SendMessageToRecipientResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(SendMessageToRecipientResponderImpl.class);

    @Autowired
    private MonitoringLogService monitoringLog;

    @Autowired
    private SendMessageToRecipientValidator validator;

    @Autowired
    private ArendeService arendeService;

    @Autowired
    @Qualifier("sendMessageToRecipientClient")
    private SendMessageToRecipientResponderInterface sendMessageToRecipientResponder;

    @Override
    public SendMessageToRecipientResponseType sendMessageToRecipient(String logicalAddress, SendMessageToRecipientType parameters) {
        LOG.debug("Send message to recipient request received. logicalAddress={} messageId={}", logicalAddress, parameters != null ? parameters.getMeddelandeId() : "N/A");

        List<String> validationErrors = validator.validate(parameters);
        if (CollectionUtils.isNotEmpty(validationErrors)) {
            LOG.warn("Invalid parameters: ", validationErrors.toString());
            SendMessageToRecipientResponseType resp = new SendMessageToRecipientResponseType();
            resp.setResult(ResultTypeUtil.errorResult(ErrorIdType.VALIDATION_ERROR, validationErrors.toString()));
            return resp;
        }

        SendMessageToRecipientResponseType response = sendMessageToRecipientResponder.sendMessageToRecipient(parameters.getLogiskAdressMottagare(), parameters);
        if (ResultCodeType.OK.equals(response.getResult().getResultCode())) {
            monitoringLog.logSendMessageToRecipient(parameters.getMeddelandeId(), parameters.getLogiskAdressMottagare());
            try {
                // try saving message in db, but always return response from recipient
                arendeService.processIncomingMessage(ArendeConverter.convertSendMessageToRecipient(parameters));
            } catch (JAXBException e) {
                LOG.error("JAXB error in SendMessageToRecipientResponder: {}", e.getMessage());
            } catch (Exception e) {
                LOG.error("Exception caught when saving messageToRecipient in db: {}", e.getMessage());
            }
        }
        return response;
    }

}