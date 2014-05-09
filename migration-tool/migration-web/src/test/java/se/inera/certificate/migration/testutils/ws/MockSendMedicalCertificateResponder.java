package se.inera.certificate.migration.testutils.ws;

import java.util.HashMap;
import java.util.Map;

import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.v1.rivtabp20.SendMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ErrorIdEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;

/**
 * Mock WS responder for testing
 * 
 * @author nikpet
 *
 */
public class MockSendMedicalCertificateResponder implements
		SendMedicalCertificateResponderInterface {

	private Map<String, SendMedicalCertificateRequestType> requestMap = new HashMap<String, SendMedicalCertificateRequestType>();
	
	public SendMedicalCertificateResponseType sendMedicalCertificate(AttributedURIType logicalAddress, SendMedicalCertificateRequestType request) {
		
		ResultOfCall result = new ResultOfCall();
		
		if (request != null) {
			requestMap.put(request.getSend().getLakarutlatande().getLakarutlatandeId(), request);
			result.setResultCode(ResultCodeEnum.INFO);
			result.setInfoText("All went well");
		} else {
			result.setResultCode(ResultCodeEnum.ERROR);
			result.setErrorId(ErrorIdEnum.TECHNICAL_ERROR);
			result.setErrorText("Something went wrong");
		}
		
		SendMedicalCertificateResponseType response = new SendMedicalCertificateResponseType();
		response.setResult(result);
		
		return response ;
	}

	public int getNbrOfRequests() {
		return requestMap.size();
	}
	
}
