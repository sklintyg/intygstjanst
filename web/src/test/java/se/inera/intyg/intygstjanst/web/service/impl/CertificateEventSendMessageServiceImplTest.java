/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.ws.soap.SOAPFaultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.inera.intyg.intygstjanst.web.integration.converter.ArendeConverter;
import se.inera.intyg.intygstjanst.web.integration.validator.CSSendMessageToRecipientValidator;
import se.inera.intyg.intygstjanst.web.service.ArendeService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

@ExtendWith(MockitoExtension.class)
class CertificateEventSendMessageServiceImplTest {

    @Mock
    private SendMessageToRecipientResponderInterface sendMessageToRecipientResponderInterface;
    @Mock
    private CSSendMessageToRecipientValidator csSendMessageToRecipientValidator;
    @Mock
    private MonitoringLogService monitoringLogService;
    @Mock
    private ArendeService arendeService;
    @Mock
    private Appender<ILoggingEvent> appender;

    @InjectMocks
    private CertificateEventSendMessageServiceImpl certificateEventSendMessageService;

    private static final String INFO_MESSAGE = "infoMessage";
    private static final String LOG_LEVEL_INFO = "INFO";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String MESSAGE_ID = "messageId";
    private static final String LOGICAL_ADDRESS = "logicalAddress";
    private static final String EXAMPLE_MESSAGE_FILE_PATH = "SendMessageToRecipientTest/sendmessagetorecipientv2.xml";

    private Arende arende;
    private String decodedXml;

    @BeforeEach
    void setUp() throws IOException, JAXBException {
        decodedXml = Resources.toString(Resources.getResource(EXAMPLE_MESSAGE_FILE_PATH), Charsets.UTF_8);

        final var sendMessageToRecipientType = (SendMessageToRecipientType) XmlMarshallerHelper.unmarshal(decodedXml).getValue();
        arende = ArendeConverter.convertSendMessageToRecipient(sendMessageToRecipientType);
    }

    @Nested
    class ResultNull {

        @BeforeEach
        void setUp() throws InvalidCertificateException {
            when(csSendMessageToRecipientValidator.validate(any(SendMessageToRecipientType.class), anyList())).thenReturn(false);
        }

        @Test
        void shouldThrowIllegalStateExceptionIfResponseIsNull() {
            when(sendMessageToRecipientResponderInterface.sendMessageToRecipient(eq(LOGICAL_ADDRESS),
                any(SendMessageToRecipientType.class))).thenReturn(null);

            assertThrows(IllegalStateException.class, () -> certificateEventSendMessageService.sendMessage(decodedXml));
        }

        @Test
        void shouldThrowIllegalStateExceptionIfResultIsNull() {
            when(sendMessageToRecipientResponderInterface.sendMessageToRecipient(eq(LOGICAL_ADDRESS),
                any(SendMessageToRecipientType.class))).thenReturn(createResponse(null));

            assertThrows(IllegalStateException.class, () -> certificateEventSendMessageService.sendMessage(decodedXml));
        }

        @Test
        void shouldThrowIllegalStateExceptionIfResultCodeIsNull() {
            when(sendMessageToRecipientResponderInterface.sendMessageToRecipient(eq(LOGICAL_ADDRESS),
                any(SendMessageToRecipientType.class))).thenReturn(createResponse(null, null));

            assertThrows(IllegalStateException.class, () -> certificateEventSendMessageService.sendMessage(decodedXml));
        }

        @Test
        void shouldStoreMessageIfResultCodeNull() {
            assertThrows(IllegalStateException.class, () -> certificateEventSendMessageService.sendMessage(decodedXml));
            verify(arendeService).processIncomingMessage(arende);
        }
    }

    @Nested
    class ResultOk {

        @BeforeEach
        void setUp() throws InvalidCertificateException {
            when(csSendMessageToRecipientValidator.validate(any(SendMessageToRecipientType.class), anyList())).thenReturn(false);
            when(sendMessageToRecipientResponderInterface.sendMessageToRecipient(eq(LOGICAL_ADDRESS),
                any(SendMessageToRecipientType.class))).thenReturn(createResponse(ResultCodeType.OK, ""));
        }

        @Test
        void shouldNotThrowExceptionIfResultCodeOk() {
            assertDoesNotThrow(() -> certificateEventSendMessageService.sendMessage(decodedXml));
        }

        @Test
        void shouldMonitorLogIfResultCodeOk() {
            certificateEventSendMessageService.sendMessage(decodedXml);
            verify(monitoringLogService).logSendMessageToRecipient(MESSAGE_ID, LOGICAL_ADDRESS);
        }

        @Test
        void shouldStoreMessageIfResultCodeOk() {
            certificateEventSendMessageService.sendMessage(decodedXml);
            verify(arendeService).processIncomingMessage(arende);
        }
    }

    @Nested
    class ResultInfo {

        @BeforeEach
        void setUp() throws InvalidCertificateException {
            when(csSendMessageToRecipientValidator.validate(any(SendMessageToRecipientType.class), anyList())).thenReturn(false);
            when(sendMessageToRecipientResponderInterface.sendMessageToRecipient(eq(LOGICAL_ADDRESS),
                any(SendMessageToRecipientType.class))).thenReturn(createResponse(ResultCodeType.INFO, INFO_MESSAGE));
        }

        @Test
        void shouldNotThrowExceptionIfResultCodeInfo() {
            assertDoesNotThrow(() -> certificateEventSendMessageService.sendMessage(decodedXml));
        }

        @Test
        void shouldMonitorLogIfResultCodeInfo() {
            certificateEventSendMessageService.sendMessage(decodedXml);
            verify(monitoringLogService).logSendMessageToRecipient(MESSAGE_ID, LOGICAL_ADDRESS);
        }

        @Test
        void shouldStoreMessageIfResultCodeInfo() {
            certificateEventSendMessageService.sendMessage(decodedXml);
            verify(arendeService).processIncomingMessage(arende);
        }

        @Test
        void shouldLogInfoMessageIfResultCodeInfo() {
            final var captureLogMessage = ArgumentCaptor.forClass(ILoggingEvent.class);
            final var root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            root.addAppender(appender);

            certificateEventSendMessageService.sendMessage(decodedXml);

            verify(appender).doAppend(captureLogMessage.capture());
            assertTrue(captureLogMessage.getValue().getFormattedMessage().contains(INFO_MESSAGE));
            assertEquals(LOG_LEVEL_INFO, captureLogMessage.getValue().getLevel().levelStr);
        }
    }

    @Nested
    class ResultError {

        @BeforeEach
        void setUp() throws InvalidCertificateException {
            when(csSendMessageToRecipientValidator.validate(any(SendMessageToRecipientType.class), anyList())).thenReturn(false);
            when(sendMessageToRecipientResponderInterface.sendMessageToRecipient(eq(LOGICAL_ADDRESS),
                any(SendMessageToRecipientType.class))).thenReturn(createResponse(ResultCodeType.ERROR, ERROR_MESSAGE));
        }

        @Test
        void shouldThrowIllegalStateExceptionIfResultCodeError() {
            assertThrows(IllegalStateException.class, () -> certificateEventSendMessageService.sendMessage(decodedXml));
        }

        @Test
        void shouldNotMonitorLogIfResultCodeError() {
            assertThrows(IllegalStateException.class, () -> certificateEventSendMessageService.sendMessage(decodedXml));
            verifyNoInteractions(monitoringLogService);
        }

        @Test
        void shouldStoreMessageIfResultCodeError() {
            assertThrows(IllegalStateException.class, () -> certificateEventSendMessageService.sendMessage(decodedXml));
            verify(arendeService).processIncomingMessage(arende);
        }

        @Test
        void shouldIncludeErrorMessageInExceptionIfResultCodeError() {
            assertThrows(IllegalStateException.class, () -> certificateEventSendMessageService.sendMessage(decodedXml));

            final var e = assertThrows(IllegalStateException.class,
                () -> certificateEventSendMessageService.sendMessage(decodedXml));

            assertTrue(e.getMessage().contains(ERROR_MESSAGE));
        }
    }

    @Nested
    class TestCertificate {

        @Test
        void shouldCallResponderInterfaceIfNotTestCertificate() {
            when(csSendMessageToRecipientValidator.validate(any(SendMessageToRecipientType.class), anyList())).thenReturn(false);
            when(sendMessageToRecipientResponderInterface.sendMessageToRecipient(eq(LOGICAL_ADDRESS),
                any(SendMessageToRecipientType.class))).thenReturn(createResponse(ResultCodeType.OK, ""));

            certificateEventSendMessageService.sendMessage(decodedXml);
            verify(sendMessageToRecipientResponderInterface).sendMessageToRecipient(eq(LOGICAL_ADDRESS),
                any(SendMessageToRecipientType.class));
        }

        @Test
        void shouldNotCallResponderInterfaceIfTestCertificate() {
            when(csSendMessageToRecipientValidator.validate(any(SendMessageToRecipientType.class), anyList())).thenReturn(true);

            certificateEventSendMessageService.sendMessage(decodedXml);
            verifyNoInteractions(sendMessageToRecipientResponderInterface);
        }
    }

    @Nested
    class MessageValidation {

        @Test
        void shouldThrowIllegalArgumentExceptionIfValidationErrors() {
            doAnswer(i -> {
                final List<String> validationErrors = i.getArgument(1);
                validationErrors.add("validationMessage");
                return null;
            }).when(csSendMessageToRecipientValidator).validate(any(SendMessageToRecipientType.class), anyList());

            assertThrows(IllegalArgumentException.class, () -> certificateEventSendMessageService.sendMessage(decodedXml));
        }
    }

    @Nested
    class StoreMessage {

        @Test
        void shouldThrowIllegalArgumentExceptionIfJAXBException() {
            try (MockedStatic<ArendeConverter> mock = Mockito.mockStatic(ArendeConverter.class)) {
                mock.when(() -> ArendeConverter.convertSendMessageToRecipient(any(SendMessageToRecipientType.class)))
                    .thenThrow(JAXBException.class);

                assertThrows(IllegalArgumentException.class, () -> certificateEventSendMessageService.sendMessage(decodedXml));
            }
        }

        @Test
        void shouldThrowOptimisticLockException() {
            when(arendeService.processIncomingMessage(arende)).thenThrow(OptimisticLockingFailureException.class);
            assertThrows(OptimisticLockingFailureException.class, () -> certificateEventSendMessageService.sendMessage(decodedXml));
        }
    }

    @Test
    void shouldThrowIllegalStateExceptionIfSoapFault() {
        when(csSendMessageToRecipientValidator.validate(any(SendMessageToRecipientType.class), anyList())).thenReturn(false);
        when(sendMessageToRecipientResponderInterface.sendMessageToRecipient(eq(LOGICAL_ADDRESS),
            any(SendMessageToRecipientType.class))).thenThrow(SOAPFaultException.class);
        assertThrows(IllegalStateException.class, () -> certificateEventSendMessageService.sendMessage(decodedXml));
    }

    private SendMessageToRecipientResponseType createResponse(ResultCodeType resultCode, String resultText) {
        final var resultType = new ResultType();
        resultType.setResultCode(resultCode);
        resultType.setResultText(resultText);
        return createResponse(resultType);
    }

    private SendMessageToRecipientResponseType createResponse(ResultType resultType) {
        final var responseType = new SendMessageToRecipientResponseType();
        responseType.setResult(resultType);
        return responseType;
    }

}
