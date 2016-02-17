package se.inera.intyg.intygstjanst.web.integration.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.intygstjanst.web.integration.SendMessageToCareResponderImpl;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;

@RunWith(MockitoJUnitRunner.class)
public class SendMessageToCareResponderImplTest {
    private static String ENHET_1_ID = "ENHET_1_ID";
    
    @InjectMocks
    private SendMessageToCareResponderInterface responder = new SendMessageToCareResponderImpl();
    
    @Test
    public void testSendMessage()  throws Exception{
        SendMessageToCareType sendMessageToCareType = buildSendMessageToCareType();
        
        SendMessageToCareResponseType responseType = responder.sendMessageToCare(ENHET_1_ID, sendMessageToCareType);
        Assert.assertTrue(responseType.getResult().getResultCode() == ResultCodeType.OK);
    }

    private SendMessageToCareType buildSendMessageToCareType() {
        SendMessageToCareType sendMessageToCareType = new SendMessageToCareType(); 
        IntygId intygId = new IntygId();
        intygId.setRoot("1");
        intygId.setExtension("intygsidextension");
        sendMessageToCareType.setIntygsId(intygId);
        HsaId hsaId = new HsaId();
        hsaId.setRoot("hsaroot");
        hsaId.setExtension("enhetsid");
        sendMessageToCareType.setLogiskAdressMottagare(hsaId);
        sendMessageToCareType.setAmne("AVSTAMNINGSMOTE");
        sendMessageToCareType.setMeddelandeId("4");
        sendMessageToCareType.setPaminnelseMeddelandeId("56");
        return sendMessageToCareType;
    }

}
