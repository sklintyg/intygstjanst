package se.inera.intyg.intygstjanst.web.integration.test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.mockito.Matchers.any;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;

import se.inera.intyg.intygstjanst.persistence.model.dao.SendMessageToCare;
import se.inera.intyg.intygstjanst.web.integration.SendMessageToCareResponderImpl;
import se.inera.intyg.intygstjanst.web.integration.converter.SendMessageToCareConverter;
import se.inera.intyg.intygstjanst.web.integration.validator.SendMessageToCareValidator;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.SendMessageToCareService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultType;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SendMessageToCareResponderImplTest {
    private static String ENHET_1_ID = "ENHET_1_ID";
    private static final String SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML = "SendMessageToCareTest/sendmessagetocare.xml";

    @Spy
    private SendMessageToCareConverter converter = spy(new SendMessageToCareConverter());

    @Mock
    private SendMessageToCareValidator validator;

    @Mock
    private SendMessageToCareService sendMessageToCareService;

    @Mock
    @Qualifier("sendMessageToCareClient")
    private SendMessageToCareResponderInterface fwdResponder;

    @Mock
    private MonitoringLogService logService;

    @InjectMocks
    private SendMessageToCareResponderInterface responder = new SendMessageToCareResponderImpl();

    @Test
    public void testSendMessage() throws Exception {
        SendMessageToCareType sendMessageToCareType = buildSendMessageToCareType();
        SendMessageToCareResponseType response = new SendMessageToCareResponseType();
        ResultType resultType = new ResultType();
        resultType.setResultCode(ResultCodeType.OK);
        response.setResult(resultType);
        when(fwdResponder.sendMessageToCare(any(String.class), any(SendMessageToCareType.class))).thenReturn(response);
        SendMessageToCareResponseType responseType = responder.sendMessageToCare(ENHET_1_ID, sendMessageToCareType);
        Assert.assertTrue(responseType.getResult().getResultCode() == ResultCodeType.OK);
        verify(fwdResponder, times(1)).sendMessageToCare(any(String.class), any(SendMessageToCareType.class));
        verify(sendMessageToCareService, times(1)).processIncomingSendMessageToCare((any(SendMessageToCare.class)));
    }

    private SendMessageToCareType buildSendMessageToCareType() throws Exception {
        SendMessageToCareType sendMessageToCareType = getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        return sendMessageToCareType;
    }

    private SendMessageToCareType getSendMessageToCareTypeFromFile(String fileName) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(SendMessageToCareType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource(fileName).getInputStream()),
                SendMessageToCareType.class).getValue();
    }

}
