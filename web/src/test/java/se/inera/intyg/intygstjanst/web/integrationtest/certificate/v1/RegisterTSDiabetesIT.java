package se.inera.intyg.intygstjanst.web.integrationtest.certificate.v1;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;

import org.junit.*;
import org.stringtemplate.v4.*;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.response.ValidatableResponse;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

public class RegisterTSDiabetesIT extends BaseIntegrationTest {

    private STGroup templateGroup;

    private static final String INTYG_ID = "registerTSDiabetesITcertificateId";
    private static final String INTYG_TYP = "ts-diabetes";

    @Override
    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/registertsdiabetes/requests.stg");
        cleanup();
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(INTYG_ID);
    }

    @Test
    public void registerTSDiabetes() {
        final String personId = "190101010101";

        givenRequest(INTYG_ID, personId).
                body("resultat.resultCode", is("OK"));

        getTsDiabetesRequest(INTYG_ID, personId).
                body("resultat.resultCode", is("OK")).
                body("meta.status.type", is("RECEIVED"));
    }

    @Test
    public void registerTSDiabetesAlreadyExists() {
        final String personId = "190101010101";
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP, personId, false);

        givenRequest(INTYG_ID, personId).
                body("resultat.resultCode", is("INFO")).
                body("resultat.resultText", is("Certificate already exists"));
    }

    @Test
    public void faultTransformerTest() {
        givenRequest("</tag>", "190101010101").
                body("resultat.resultCode", is("ERROR")).
                body("resultat.resultText", startsWith("Unmarshalling Error"));
    }

    private ValidatableResponse getTsDiabetesRequest(String intygId, String personId) {
        ST requestTemplate = new STGroupFile("integrationtests/gettsdiabetes/requests.stg").getInstanceOf("request");
        requestTemplate.add("intygId", intygId);
        requestTemplate.add("personId", personId);

        return given().body(requestTemplate.render()).
                when().
                post("inera-certificate/get-ts-diabetes/v1.0").
                then().
                statusCode(200).
                rootPath("Envelope.Body.GetTSDiabetesResponse.");
    }

    private ValidatableResponse givenRequest(String intygId, String personId) {
        ST requestTemplate = templateGroup.getInstanceOf("request");
        requestTemplate.add("intygId", intygId);
        requestTemplate.add("personId", personId);

        return given().body(requestTemplate.render()).
                when().
                post("inera-certificate/register-ts-diabetes/v1.0").
                then().
                statusCode(200).
                rootPath("Envelope.Body.RegisterTSDiabetesResponse.");
    }
}