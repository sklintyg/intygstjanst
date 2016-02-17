package se.inera.intyg.intygstjanst.web.integration;

import org.springframework.beans.factory.annotation.Autowired;

import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultType;

public class SendMessageToCareResponderImpl implements SendMessageToCareResponderInterface{

    
    @Autowired
    private SendMessageToCareResponderInterface forwarder;
    
    @Override
    public SendMessageToCareResponseType sendMessageToCare(String logicalAddress, SendMessageToCareType parameters) {
        SendMessageToCareResponseType response = new SendMessageToCareResponseType();
        
        
       // forwarder.sendMessageToCare(parameters.getLogiskAdressMottagare().getExtension(), parameters);
        
        ResultType resultType = new ResultType();
        resultType.setResultCode(ResultCodeType.OK);
        response.setResult(resultType);
        return null;
    }

}
