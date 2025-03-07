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

package se.inera.intyg.intygstjanst.web.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.integration.converter.util.ResultTypeUtil;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.intygstjanst.web.integration.converter.ArendeConverter;
import se.inera.intyg.intygstjanst.web.integration.validator.CSSendMessageToRecipientValidator;
import se.inera.intyg.intygstjanst.web.service.ArendeService;
import se.inera.intyg.intygstjanst.web.service.CertificateEventSendMessageService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

@Service
@Slf4j
public class CertificateEventSendMessageServiceImpl implements CertificateEventSendMessageService {

    private final SendMessageToRecipientResponderInterface sendMessageToRecipientResponderInterface;
    private final CSSendMessageToRecipientValidator csSendMessageToRecipientValidator;
    private final MonitoringLogService monitoringLogService;
    private final ArendeService arendeService;

    public CertificateEventSendMessageServiceImpl(
        @Qualifier("sendMessageToRecipientClient") SendMessageToRecipientResponderInterface sendMessageToRecipientResponderInterface,
        MonitoringLogService monitoringLogService,
        ArendeService arendeService, CSSendMessageToRecipientValidator csSendMessageToRecipientValidator) {
        this.sendMessageToRecipientResponderInterface = sendMessageToRecipientResponderInterface;
        this.csSendMessageToRecipientValidator = csSendMessageToRecipientValidator;
        this.monitoringLogService = monitoringLogService;
        this.arendeService = arendeService;
    }

    @Override
    @Transactional(rollbackFor = IllegalStateException.class)
    public void sendMessage(String xml) {
        try {
            log.info("TEMP_LOG xml: {}", xml);
            final var wsRequest =  (SendMessageToRecipientType) XmlMarshallerHelper.unmarshal(xml).getValue();
            final var certificateId = wsRequest.getIntygsId().getExtension();
            final var logicalAddress = wsRequest.getLogiskAdressMottagare();
            final var messageId = wsRequest.getMeddelandeId();
            final var topic = wsRequest.getAmne().getCode();

            final var isTestCertificate = validateMessageToRecipient(wsRequest, messageId, certificateId, topic, logicalAddress);
            storeValidatedMessage(wsRequest, messageId, certificateId, topic, logicalAddress);

            final var wsResponse = isTestCertificate ? getResultOk()
                : sendMessageToRecipientResponderInterface.sendMessageToRecipient(logicalAddress, wsRequest);
            handleResponse(wsResponse, messageId, certificateId, topic, logicalAddress);

        } catch (SOAPFaultException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean validateMessageToRecipient(SendMessageToRecipientType wsRequest, String messageId, String certificateId, String topic,
        String logicalAddress) {
        final var validationErrors = new ArrayList<String>();
        final var isTestCertificate = csSendMessageToRecipientValidator.validate(wsRequest, validationErrors);

        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(validationErrorMessage(messageId, certificateId, topic, logicalAddress,
                validationErrors));
        }

        return isTestCertificate;
    }

    private void handleResponse(SendMessageToRecipientResponseType wsResponse, String messageId, String certificateId, String topic,
        String recipient) {

        if (wsResponse == null || wsResponse.getResult() == null || wsResponse.getResult().getResultCode() == null) {
            throw new IllegalStateException(getResultNullMessage(messageId, topic, certificateId, recipient));
        }

        final var result = wsResponse.getResult().getResultCode();
        if (result == ResultCodeType.OK) {
            monitoringLogService.logSendMessageToRecipient(messageId, recipient);
        }

        final var wsResponseMessage = wsResponse.getResult().getResultText();
        if (result == ResultCodeType.INFO) {
            monitoringLogService.logSendMessageToRecipient(messageId, recipient);
            log.info("Received INFO result sending messageId '{}' with topic '{}' for certificateId '{}' to recipient '{}'. "
                + "INFO-message '{}'", messageId, topic, certificateId, recipient, wsResponseMessage);
        }

        if (result == ResultCodeType.ERROR) {
            throw new IllegalStateException(getResultErrorMessage(messageId, topic, certificateId, recipient, wsResponseMessage));
        }
    }

    private void storeValidatedMessage(SendMessageToRecipientType wsRequest, String certificateId, String messageId, String recipient,
        String topic) {

        try {
            final var message = ArendeConverter.convertSendMessageToRecipient(wsRequest);
            log.info("TEMP_LOG wsRequest: {}", JsonMapper.builder().addModule(new JavaTimeModule()).build().writeValueAsString(wsRequest));
            log.info("TEMP_LOG message: {}", message);
            log.info("TEMP_LOG message-data: {}", message.getMeddelande());
            arendeService.processIncomingMessage(message);
        } catch (JAXBException | IllegalArgumentException | JsonProcessingException e) {
            throw new IllegalArgumentException(persistenceFailureMessage(certificateId, messageId, recipient, topic), e);
        }
    }

    private static String persistenceFailureMessage(String certificateId, String messageId, String recipient, String topic) {
        return String.format("Failure storing messageId '%s' with topic '%s' for certificateId '%s' to recipient '%s'.", messageId, topic,
            certificateId, recipient);
    }

    private static String validationErrorMessage(String messageId, String certificateId, String topic, String logicalAddress,
        List<String> validationErrors) {
        return String.format("Received validation errors sending message to recipient for messageId '%s' with topic '%s' for "
            + "certificateId '%s' to recipient '%s'. Errors detected: %s", messageId, topic, certificateId, logicalAddress,
            validationErrors);
    }

    private static String getResultNullMessage(String messageid, String topic, String certificateId, String recipient) {
        return String.format("Received null result sending messageId '%s' with topic '%s' for certificateId '%s' to recipient '%s'.",
            messageid, topic, certificateId, recipient);
    }

    private static String getResultErrorMessage(String messageid, String topic, String certificateId, String recipient,
        String wsResponseMessage) {
        return String.format("Received ERROR result sending messageId '%s' with topic '%s' for certificateId '%s' to recipient '%s'. "
            + "ERROR-message '%s'", messageid, topic, certificateId, recipient, wsResponseMessage);
    }

    private static SendMessageToRecipientResponseType getResultOk() {
        final var response = new SendMessageToRecipientResponseType();
        response.setResult(ResultTypeUtil.okResult());
        return response;
    }

}
