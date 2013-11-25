package se.inera.certificate.spec

import se.inera.certificate.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

public class VerifieraRattatIntygHosFk extends RestClientFixture {

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
    
    public String rattat() {
        def row = response.data[id]
        if (row != null) {
            return row['Makulerad']
        } else {
            return "Nej"
        }
    }

    public String meddelande() {
        def row = response.data[id]
        if (row != null) {
            return row['Meddelande']
        } else {
            return null
        }
    }
}
