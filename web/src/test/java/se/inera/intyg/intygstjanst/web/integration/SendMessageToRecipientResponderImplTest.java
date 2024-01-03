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
package se.inera.intyg.intygstjanst.web.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

import se.inera.intyg.common.support.integration.converter.util.ResultTypeUtil;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.inera.intyg.intygstjanst.web.integration.validator.SendMessageToRecipientValidator;
import se.inera.intyg.intygstjanst.web.service.ArendeService;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

@RunWith(MockitoJUnitRunner.class)
public class SendMessageToRecipientResponderImplTest {

    private static final String LOGICAL_ADDRESS = "123";
    private static final String LOGICAL_ADDRESS_RECIPIENT = "456";

    @Mock
    private MonitoringLogService monitoringLog;

    @Mock
    private SendMessageToRecipientValidator validator;

    @Mock
    private ArendeService arendeService;

    @Mock
    private SendMessageToRecipientResponderInterface sendMessageToRecipientResponder;

    @Mock
    private CertificateService certificateService;

    @InjectMocks
    private SendMessageToRecipientResponderImpl responder;

    @Before
    public void setup() throws InvalidCertificateException {
        when(certificateService.isTestCertificate(any())).thenReturn(false);
    }

    @Test
    public void sendMessageToRecipientTest() throws Exception {
        setupClientResponse(ResultTypeUtil.okResult());
        SendMessageToRecipientResponseType res = responder.sendMessageToRecipient(LOGICAL_ADDRESS, createParameters());
        assertEquals(ResultCodeType.OK, res.getResult().getResultCode());
        assertInvocations(times(1), times(1), times(1));
    }

    @Test
    public void sendMessageToRecipientOnTestCertificate() throws Exception {
        when(certificateService.isTestCertificate(any())).thenReturn(true);
        setupClientResponse(ResultTypeUtil.okResult());
        SendMessageToRecipientResponseType res = responder.sendMessageToRecipient(LOGICAL_ADDRESS, createParameters());
        assertEquals(ResultCodeType.OK, res.getResult().getResultCode());
        assertInvocations(times(1), times(1), times(0));
    }

    @Test
    public void sendMessageToRecipientClientInfoTest() throws Exception {
        final String clientInfoText = "info here";
        setupClientResponse(ResultTypeUtil.infoResult(clientInfoText));
        SendMessageToRecipientResponseType res = responder.sendMessageToRecipient(LOGICAL_ADDRESS, createParameters());
        assertEquals(ResultCodeType.INFO, res.getResult().getResultCode());
        assertEquals(clientInfoText, res.getResult().getResultText());
        assertInvocations(times(1), times(1), times(1));
    }

    @Test
    public void sendMessageToRecipientValidationErrorTest() throws Exception {
        setupValidatorError();
        SendMessageToRecipientResponseType res = responder.sendMessageToRecipient(LOGICAL_ADDRESS, createParameters());
        assertEquals(ResultCodeType.ERROR, res.getResult().getResultCode());
        assertEquals(ErrorIdType.VALIDATION_ERROR, res.getResult().getErrorId());
        assertInvocations(never(), never(), never());
    }

    @Test
    public void sendMessageToRecipientCertificateDoesNotExistTest() throws Exception {
        when(validator.validate(any(SendMessageToRecipientType.class))).thenThrow(new InvalidCertificateException("intygId", null));
        SendMessageToRecipientResponseType res = responder.sendMessageToRecipient(LOGICAL_ADDRESS, createParameters());
        assertEquals(ResultCodeType.ERROR, res.getResult().getResultCode());
        assertEquals(ErrorIdType.APPLICATION_ERROR, res.getResult().getErrorId());
        assertInvocations(never(), never(), never());
    }

    @Test
    public void sendMessageToRecipientResponderClientErrorTest() throws Exception {
        final String clientErrorText = "something wrong";
        setupClientResponse(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, clientErrorText));
        SendMessageToRecipientResponseType res = responder.sendMessageToRecipient(LOGICAL_ADDRESS, createParameters());
        assertEquals(ResultCodeType.ERROR, res.getResult().getResultCode());
        assertEquals(ErrorIdType.APPLICATION_ERROR, res.getResult().getErrorId());
        assertEquals(clientErrorText, res.getResult().getResultText());
        assertInvocations(never(), never(), times(1));
    }

    @Test
    public void sendMessageToRecipientArendeServiceThrowsExceptionTest() throws Exception {
        setupClientResponse(ResultTypeUtil.okResult());

        when(arendeService.processIncomingMessage(or(isNull(), any(Arende.class)))).thenThrow(new RuntimeException("error"));

        SendMessageToRecipientResponseType res = responder.sendMessageToRecipient(LOGICAL_ADDRESS, createParameters());
        assertEquals(ResultCodeType.OK, res.getResult().getResultCode());
        assertInvocations(times(1), times(1), times(1));
    }

    private SendMessageToRecipientType createParameters() {
        SendMessageToRecipientType parameters = new SendMessageToRecipientType();
        parameters.setLogiskAdressMottagare(LOGICAL_ADDRESS_RECIPIENT);
        parameters.setAmne(new Amneskod());
        parameters.setIntygsId(new IntygId());
        return parameters;
    }

    private void setupValidatorError() throws InvalidCertificateException {
        when(validator.validate(any(SendMessageToRecipientType.class))).thenReturn(Arrays.asList(""));
    }

    private void setupClientResponse(ResultType result) {
        SendMessageToRecipientResponseType response = new SendMessageToRecipientResponseType();
        response.setResult(result);
        when(sendMessageToRecipientResponder.sendMessageToRecipient(anyString(), any(SendMessageToRecipientType.class)))
            .thenReturn(response);
    }

    private void assertInvocations(
        VerificationMode monitoringLogInvocation,
        VerificationMode arendeServiceInvocation,
        VerificationMode sendMessageToRecipientClientInvocation) throws JAXBException, InvalidCertificateException {

        verify(validator).validate(any(SendMessageToRecipientType.class)); // always call validator
        verify(arendeService, arendeServiceInvocation).processIncomingMessage(any(Arende.class));
        verify(monitoringLog, monitoringLogInvocation).logSendMessageToRecipient(or(isNull(), anyString()), anyString());
        verify(sendMessageToRecipientResponder, sendMessageToRecipientClientInvocation)
            .sendMessageToRecipient(eq(LOGICAL_ADDRESS_RECIPIENT), any(SendMessageToRecipientType.class));
    }
}
