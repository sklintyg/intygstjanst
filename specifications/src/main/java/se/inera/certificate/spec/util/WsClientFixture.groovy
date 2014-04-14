package se.inera.certificate.spec.util
import java.security.KeyStore
import java.security.cert.X509Certificate

import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

import org.apache.cxf.configuration.jsse.TLSClientParameters
import org.apache.cxf.configuration.security.FiltersType
import org.apache.cxf.endpoint.Client
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.frontend.ClientProxyFactoryBean
import org.apache.cxf.jaxws.JaxWsClientFactoryBean
import org.apache.cxf.message.Message
import org.apache.cxf.transport.http.HTTPConduit
import org.w3.wsaddressing10.AttributedURIType

import se.inera.certificate.integration.json.CustomObjectMapper
import se.inera.ifv.insuranceprocess.healthreporting.listcertificates.v1.rivtabp20.ListCertificatesResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum

class WsClientFixture {

	final static String LOGICAL_ADDRESS = "5565594230"

	private CustomObjectMapper jsonMapper = new CustomObjectMapper();
	protected AttributedURIType logicalAddress = new AttributedURIType()
	
	public WsClientFixture() {
		this(LOGICAL_ADDRESS)
	}
	
	public WsClientFixture(String address) {
		logicalAddress.setValue(address)
	}
	
	def asJson(def object) {
		StringWriter sw = new StringWriter()
		jsonMapper.writeValue(sw, object)
		return sw.toString()
	}
	
	def asErrorMessage(String s) {
		throw new Exception("message:<<${s.replace(System.getProperty('line.separator'), ' ')}>>")
	}
	
    static String baseUrl = System.getProperty("certificate.baseUrl", "http://localhost:8080/inera-certificate/")

	def setEndpoint(def responder, String serviceName, String url = baseUrl + serviceName) {
		if (!url) url = baseUrl + serviceName
		Client client = ClientProxy.getClient(responder)
		client.getRequestContext().put(Message.ENDPOINT_ADDRESS, url)
	}
	
	def createClient(def responderInterface, String url) {
		ClientProxyFactoryBean factory = new ClientProxyFactoryBean(new JaxWsClientFactoryBean());
		factory.setServiceClass( responderInterface );
		factory.setAddress(url);
		def responder = factory.create();
		if (url.startsWith("https:")) {
			setClientCertificate(responder)
		}
		return responder
	}
	
	def resultAsString(response) {
        String result = null
		if (response) {
	        switch (response.result.resultCode) {
	            case ResultCodeEnum.OK:
	                result = response.result.resultCode.toString()
                    break
	            case ResultCodeEnum.INFO:
	                result = "[${response.result.resultCode.toString()}] - ${response.result.infoText}"
                    break
                case ResultCodeEnum.ERROR:
					result = "[${response.result.errorId.toString()}] - ${response.result.errorText}"
                    break
	        }
		}
		return result
	}

	def setClientCertificate(def responder) {
		Client client = ClientProxy.getClient(responder)
		HTTPConduit httpConduit = (HTTPConduit)client.getConduit();
		TLSClientParameters tlsParams = new TLSClientParameters();
		tlsParams.setDisableCNCheck(true);

		KeyStore trustStore = KeyStore.getInstance("JKS");
		String trustpass = "password";//provide trust pass

		trustStore.load(WsClientFixture.class.getResourceAsStream("/truststore-ntjp.jks"), trustpass.toCharArray());
		TrustManagerFactory trustFactory =
				TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustFactory.init(trustStore);
		TrustManager[] tm = trustFactory.getTrustManagers();
		// TrustManager[] tm = [new TrustAllX509TrustManager()]
		tlsParams.setTrustManagers(tm);

		KeyStore certStore = KeyStore.getInstance("PKCS12");
        String certFile = System.getProperty("ws.certificate.file");
        String certPass = System.getProperty("ws.certificate.password");
		certStore.load(WsClientFixture.class.getResourceAsStream(certFile), certPass.toCharArray());
		KeyManagerFactory keyFactory =
				KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyFactory.init(certStore, certPass.toCharArray());
		KeyManager[] km = keyFactory.getKeyManagers();
		tlsParams.setKeyManagers(km);

		FiltersType filter = new FiltersType();
		filter.getInclude().add(".*_EXPORT_.*");
		filter.getInclude().add(".*_EXPORT1024_.*");
		filter.getInclude().add(".*_WITH_DES_.*");
        filter.getInclude().add(".*_WITH_AES_.*");
		filter.getInclude().add(".*_WITH_NULL_.*");
		filter.getExclude().add(".*_DH_anon_.*");
		tlsParams.setCipherSuitesFilter(filter);//set all the needed include filters.

		httpConduit.setTlsClientParameters(tlsParams);
	}
}
