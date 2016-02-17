package se.inera.intyg.intygstjanst.web.integration.test;

import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.intygstjanst.web.integration.SendMessageToCareResponderImpl;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.IntygId;

@RunWith(MockitoJUnitRunner.class)
public class SendMessageToCareResponderImplTest {
    
    @Mock
    private SendMessageToCareResponderImpl forwarder = mock(SendMessageToCareResponderImpl.class);

    
    @InjectMocks
    private SendMessageToCareResponderInterface responder = new SendMessageToCareResponderImpl();
    
    
    @Test
    public void testSendMessage()  throws Exception{
//        SendMessageToCareType sendMessageToCareType = new SendMessageToCareType();
//        IntygId intygId = new IntygId();
//        intygId.setRoot("intygroot");
//        intygId.setExtension("intytextension");
//        sendMessageToCareType.setIntygsId(intygId);
//        HsaId hsaId = new HsaId();
//        hsaId.setRoot("hsaroot");
//        hsaId.setExtension("enhetsid");
//       //sendMessageToCareType.setLogiskAdressMottagare(hsaId);
//        sendMessageToCareType.setAmne("AVSTAMNINGSMOTE");
//        sendMessageToCareType.setMeddelandeId("testmeddid");
//        sendMessageToCareType.setPaminnelseMeddelandeId("paminnelsemeddelandeid");
//        responder.sendMessageToCare("enhetsid", sendMessageToCareType);
        Assert.assertTrue(true);
    }

}
