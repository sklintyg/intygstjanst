package se.inera.certificate.spec

import se.inera.certificate.spec.util.WsClientFixture
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType
import se.riv.itintegration.monitoring.v1.PingForConfigurationType

/**
 *
 * @author andreaskaltenbach
 */
class KontrolleraVersion extends WsClientFixture {

    private PingForConfigurationResponderInterface pingResponder

	static String serviceUrl = System.getProperty("service.pingForConfigurationUrl")

    public KontrolleraVersion() {
		String url = serviceUrl ? serviceUrl : baseUrl + "ping-for-configuration/v1.0"
		pingResponder = createClient(PingForConfigurationResponderInterface.class, url)
    }

    public String version() {
        PingForConfigurationType pingType = new PingForConfigurationType()
        PingForConfigurationResponseType response = pingResponder.pingForConfiguration(null, pingType)

        response.version
    }
}
