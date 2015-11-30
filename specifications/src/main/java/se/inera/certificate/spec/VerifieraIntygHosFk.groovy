package se.inera.intyg.common.specifications.spec

import se.inera.intyg.common.specifications.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

public class VerifieraIntygHosFk extends RestClientFixture {

    String id
    def response
    
    public void execute() {
        def restClient = createRestClient()
        response = restClient.get(
                path: 'fk/certificates',
                requestContentType: JSON,
                contentType: 'application/json'
        )
    }

    
    public String status() {
        if (response.data[id] != null)
            return "skickat"
        else
            return "ej skickat"
    }
}
