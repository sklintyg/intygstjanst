package se.inera.intyg.intygstjanst.web.integrationtest.certificate;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;

public class GetCertificateIT extends BaseIntegrationTest {

    private static final String BASE = "Envelope.Body.GetCertificateResponse.";

    private ST requestTemplate;

    private STGroup templateGroup;

    private String tolvansId = "191212121212";
    private String intygsId = "intyg-10";

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/getcertificate/requests.stg");
        requestTemplate = templateGroup.getInstanceOf("request");
    }

    @Test
    public void getCertificateWorks() {
        requestTemplate.add("data", new IntygsData(intygsId, tolvansId));

        given().body(requestTemplate.render()).
                when().
                post("inera-certificate/get-certificate-se/v2.0").
                then().
                statusCode(200).
                rootPath(BASE).
                body("intyg.intygs-id.extension", is("intyg-10"));
    }

    @Test
    public void getCertificateRespectsSchema() {
        requestTemplate.add("data", new IntygsData(intygsId, tolvansId));

        given().body(requestTemplate.render()).
                when().
                post("inera-certificate/get-certificate-se/v2.0").
                then().
                statusCode(200).
                rootPath(BASE).
                body("intyg.intygs-id.extension", is("intyg-10"));
    }

    @SuppressWarnings("unused")
    private static class IntygsData {
        public final String intygsId;
        public final String personId;

        public IntygsData(String intygsId, String personId) {
            this.intygsId = intygsId;
            this.personId = personId;
        }
    }

}
