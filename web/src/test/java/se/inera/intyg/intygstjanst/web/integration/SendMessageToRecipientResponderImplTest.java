package se.inera.intyg.intygstjanst.web.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.v2.ResultTypeUtil;
import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.inera.intyg.intygstjanst.web.integration.validator.SendMessageToRecipientValidator;
import se.inera.intyg.intygstjanst.web.service.ArendeService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v1.*;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v2.*;

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

    @InjectMocks
    private SendMessageToRecipientResponderImpl responder;

    @Test
    public void sendMessageToRecipientTest() throws Exception {
        ResultType clientResult = ResultTypeUtil.okResult();
        setupClientResponse(clientResult);
        SendMessageToRecipientResponseType res = responder.sendMessageToRecipient(LOGICAL_ADDRESS, createParameters());
        assertEquals(clientResult, res.getResult());
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
    public void sendMessageToRecipientResponderClientErrorTest() throws Exception {
        ResultType clientResult = ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, "something wrong");
        setupClientResponse(clientResult);
        SendMessageToRecipientResponseType res = responder.sendMessageToRecipient(LOGICAL_ADDRESS, createParameters());
        assertEquals(clientResult, res.getResult());
        assertInvocations(never(), never(), times(1));
    }

    @Test
    public void sendMessageToRecipientArendeServiceThrowsExceptionTest() throws Exception {
        ResultType clientResult = ResultTypeUtil.okResult();
        setupClientResponse(clientResult);
        when(arendeService.processIncomingMessage(any(Arende.class))).thenThrow(new RuntimeException("error"));
        SendMessageToRecipientResponseType res = responder.sendMessageToRecipient(LOGICAL_ADDRESS, createParameters());
        assertEquals(clientResult, res.getResult());
        assertInvocations(times(1), times(1), times(1));
    }

    private SendMessageToRecipientType createParameters() {
        SendMessageToRecipientType parameters = new SendMessageToRecipientType();
        parameters.setLogiskAdressMottagare(LOGICAL_ADDRESS_RECIPIENT);
        parameters.setAmne(new Amneskod());
        parameters.setIntygsId(new IntygId());
        return parameters;
    }

    private void setupValidatorError() {
        when(validator.validate(any(SendMessageToRecipientType.class))).thenReturn(Arrays.asList(""));
    }

    private void setupClientResponse(ResultType result) {
        SendMessageToRecipientResponseType response = new SendMessageToRecipientResponseType();
        response.setResult(result);
        when(sendMessageToRecipientResponder.sendMessageToRecipient(anyString(), any(SendMessageToRecipientType.class))).thenReturn(response);
    }

    private void assertInvocations(VerificationMode monitoringLogInvocation,
            VerificationMode arendeServiceInvocation,
            VerificationMode sendMessageToRecipientClientInvocation) throws JAXBException {
        verify(validator).validate(any(SendMessageToRecipientType.class)); // always call validator
        verify(arendeService, arendeServiceInvocation).processIncomingMessage(any(Arende.class));
        verify(monitoringLog, monitoringLogInvocation).logSendMessageToRecipient(anyString(), anyString());
        verify(sendMessageToRecipientResponder, sendMessageToRecipientClientInvocation).sendMessageToRecipient(eq(LOGICAL_ADDRESS_RECIPIENT),
                any(SendMessageToRecipientType.class));
    }
}
