package se.inera.intyg.intygstjanst.web.integrationtest.certificate;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;

public class GetCertificateIT extends BaseIntegrationTest {

    private static final String BASE = "Envelope.Body.GetCertificateResponse.";

    private ST requestTemplate;

    private STGroup templateGroup;

    //"soap:Envelope/soap:Body/lc:SendMessageToCareResponse"

    private String intygsId = "intyg-10";

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/getcertificate/requests.stg");
        requestTemplate = templateGroup.getInstanceOf("request");
    }

    @Test
    public void getCertificateWorks() {
        requestTemplate.add("data", new IntygsData(intygsId));

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
        requestTemplate.add("data", new IntygsData(intygsId));

        given().body(requestTemplate.render()).
                when().
                post("inera-certificate/get-certificate-se/v2.0").
                then().
                statusCode(200).
                rootPath(BASE).
                body("intyg.intygs-id.extension", is("intyg-10"));
    }

    private static class IntygsData {
        public final String intygsId;

        public IntygsData(String intygsId) {
            this.intygsId = intygsId;
        }
    }

}
