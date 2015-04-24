package se.inera.certificate.spec

import se.inera.certificate.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

public class SimuleraFelHosTs extends RestClientFixture {

    Boolean fel
    private String url = System.getProperty("certificate.baseUrl");
    
    public void execute(){
        def restClient = createRestClient("${url}")
        def response = restClient.post(path: 'ts-certificate-stub/certificates', query: [fakeException: fel])
        assert response.status == 204
    }
}
