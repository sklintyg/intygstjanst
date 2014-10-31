package se.inera.certificate.spec;

import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.setconsent.v1.rivtabp20.SetConsentResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.setconsentresponder.v1.SetConsentRequestType
import se.inera.ifv.insuranceprocess.healthreporting.setconsentresponder.v1.SetConsentResponseType

public class Samtycke extends WsClientFixture {

	private SetConsentResponderInterface setConsentResponder

	static String serviceUrl = System.getProperty("service.setConsentUrl")

	public Samtycke() {
        this(WsClientFixture.LOGICAL_ADDRESS)
    }

    public Samtycke(String logiskAddress) {
        super(logiskAddress)
		String url = serviceUrl ? serviceUrl : baseUrl + "set-consent/v1.0"
		setConsentResponder = createClient(SetConsentResponderInterface.class, url)
	}

	String personnr
	private boolean samtycke

	public void setSamtycke(String value) {
		if (value != null && value.equalsIgnoreCase("ja")) {
			samtycke = true
		} else {
			samtycke = false
		}
	}

	public void execute() {
		SetConsentRequestType setConsentParameters = new SetConsentRequestType()
		setConsentParameters.personnummer = personnr
		setConsentParameters.consentGiven = samtycke
		SetConsentResponseType setConsentResponse = setConsentResponder.setConsent(logicalAddress, setConsentParameters)
	}

}
