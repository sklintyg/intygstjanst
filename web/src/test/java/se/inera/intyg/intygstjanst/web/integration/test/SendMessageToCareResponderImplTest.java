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
package se.inera.intyg.intygstjanst.web.integration.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.persistence.PersistenceException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import se.inera.intyg.intygstyper.fkparent.support.ResultTypeUtil;
import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.inera.intyg.intygstjanst.web.integration.SendMessageToCareResponderImpl;
import se.inera.intyg.intygstjanst.web.integration.validator.SendMessageToCareValidator;
import se.inera.intyg.intygstjanst.web.service.ArendeService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.*;
import se.riv.clinicalprocess.healthcond.certificate.v2.*;

@RunWith(MockitoJUnitRunner.class)
public class SendMessageToCareResponderImplTest {
    private static String ENHET_1_ID = "ENHET_1_ID";
    private static final String SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML = "SendMessageToCareTest/sendmessagetocare.xml";

    @Mock
    private SendMessageToCareValidator validator;

    @Mock
    private ArendeService sendMessageToCareService;

    @Mock
    private SendMessageToCareResponderInterface fwdResponder;

    @Mock
    private MonitoringLogService logService;

    @Mock
    private StatisticsService statisticsService;

    @InjectMocks
    private SendMessageToCareResponderImpl responder;

    @Test
    public void testSendMessage() throws Exception {
        when(fwdResponder.sendMessageToCare(any(String.class), any(SendMessageToCareType.class)))
                .thenReturn(createClientResponse(ResultTypeUtil.okResult()));

        SendMessageToCareResponseType responseType = responder.sendMessageToCare(ENHET_1_ID, buildSendMessageToCareType());
        assertEquals(ResultCodeType.OK, responseType.getResult().getResultCode());
        verify(fwdResponder).sendMessageToCare(any(String.class), any(SendMessageToCareType.class));
        verify(statisticsService, times(1)).messageSent(anyString(), anyString(), anyString());
        verify(sendMessageToCareService).processIncomingMessage((any(Arende.class)));
        verify(logService).logSendMessageToCareReceived(anyString(), anyString());
    }

    @Test
    public void testSendMessageClientInfo() throws Exception {
        final String clientInfoText = "info here";
        when(fwdResponder.sendMessageToCare(any(String.class), any(SendMessageToCareType.class)))
                .thenReturn(createClientResponse(ResultTypeUtil.infoResult(clientInfoText)));

        SendMessageToCareResponseType responseType = responder.sendMessageToCare(ENHET_1_ID, buildSendMessageToCareType());
        assertEquals(ResultCodeType.INFO, responseType.getResult().getResultCode());
        assertEquals(clientInfoText, responseType.getResult().getResultText());
        verify(fwdResponder).sendMessageToCare(any(String.class), any(SendMessageToCareType.class));
        verify(statisticsService, times(1)).messageSent(anyString(), anyString(), anyString());
        verify(sendMessageToCareService).processIncomingMessage((any(Arende.class)));
        verify(logService).logSendMessageToCareReceived(anyString(), anyString());
    }

    @Test
    public void testSendMessageValidationError() throws Exception {
        when(validator.validateSendMessageToCare(any(SendMessageToCareType.class))).thenReturn(Arrays.asList("fel"));

        SendMessageToCareResponseType responseType = responder.sendMessageToCare(ENHET_1_ID, buildSendMessageToCareType());
        assertEquals(ResultCodeType.ERROR, responseType.getResult().getResultCode());
        assertEquals(ErrorIdType.VALIDATION_ERROR, responseType.getResult().getErrorId());
        assertEquals("Validation of SendMessageToCareType failed for message with meddelandeid 4: [fel]", responseType.getResult().getResultText());
        verify(fwdResponder, never()).sendMessageToCare(any(String.class), any(SendMessageToCareType.class));
        verify(statisticsService, times(0)).messageSent(anyString(), anyString(), anyString());
        verify(sendMessageToCareService, never()).processIncomingMessage((any(Arende.class)));
        verify(logService, never()).logSendMessageToCareReceived(anyString(), anyString());
    }

    @Test
    public void testSendMessageClientError() throws Exception {
        final ErrorIdType clientErrorId = ErrorIdType.TECHNICAL_ERROR;
        final String clientErrorText = "fel";

        when(fwdResponder.sendMessageToCare(any(String.class), any(SendMessageToCareType.class)))
                .thenReturn(createClientResponse(ResultTypeUtil.errorResult(clientErrorId, clientErrorText)));

        SendMessageToCareResponseType responseType = responder.sendMessageToCare(ENHET_1_ID, buildSendMessageToCareType());
        assertEquals(ResultCodeType.ERROR, responseType.getResult().getResultCode());
        assertEquals(clientErrorId, responseType.getResult().getErrorId());
        assertEquals(clientErrorText, responseType.getResult().getResultText());
        verify(fwdResponder, times(1)).sendMessageToCare(any(String.class), any(SendMessageToCareType.class));
        verify(statisticsService, times(0)).messageSent(anyString(), anyString(), anyString());
        verify(sendMessageToCareService, never()).processIncomingMessage((any(Arende.class)));
        verify(logService, never()).logSendMessageToCareReceived(anyString(), anyString());
    }

    @Test
    public void testSendMessageProcessThrowsException() throws Exception {
        when(fwdResponder.sendMessageToCare(any(String.class), any(SendMessageToCareType.class)))
                .thenReturn(createClientResponse(ResultTypeUtil.okResult()));
        when(sendMessageToCareService.processIncomingMessage(any(Arende.class))).thenThrow(new PersistenceException());

        SendMessageToCareResponseType responseType = responder.sendMessageToCare(ENHET_1_ID, buildSendMessageToCareType());
        assertEquals(ResultCodeType.OK, responseType.getResult().getResultCode());
        verify(fwdResponder).sendMessageToCare(any(String.class), any(SendMessageToCareType.class));
        verify(statisticsService, times(1)).messageSent(anyString(), anyString(), anyString());
        verify(sendMessageToCareService).processIncomingMessage((any(Arende.class)));
        verify(logService).logSendMessageToCareReceived(anyString(), anyString());
    }

    private SendMessageToCareType buildSendMessageToCareType() throws Exception {
        SendMessageToCareType sendMessageToCareType = getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        return sendMessageToCareType;
    }

    private SendMessageToCareResponseType createClientResponse(ResultType resultType) {
        SendMessageToCareResponseType response = new SendMessageToCareResponseType();
        response.setResult(resultType);
        return response;
    }

    private SendMessageToCareType getSendMessageToCareTypeFromFile(String fileName) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(SendMessageToCareType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource(fileName).getInputStream()),
                SendMessageToCareType.class).getValue();
    }

}
