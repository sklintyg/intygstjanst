package se.inera.intyg.common.specifications.spec

import se.inera.intyg.common.specifications.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

public class KonfigureraFelsvarForTSStubbe extends RestClientFixture {

    Boolean aktiv
    private String url = System.getProperty("certificate.baseUrl");
    
	def respons
	public String respons() {
		respons
	}

    public void execute(){
        def restClient = createRestClient("${url}")
        def response = restClient.post(path: 'ts-certificate-stub/certificates', query: [fakeException: aktiv])
        respons = response.status
    }
}
