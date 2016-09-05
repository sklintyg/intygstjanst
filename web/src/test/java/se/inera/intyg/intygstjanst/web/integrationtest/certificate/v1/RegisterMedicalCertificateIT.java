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

public class RegisterMedicalCertificateIT extends BaseIntegrationTest {

    private STGroup templateGroup;

    private static final String INTYG_ID = "registerMedicalCertificateITcertificateId";
    private static final String INTYG_TYP = "fk7263";

    @Override
    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/registermedicalcertificate/requests.stg");
        cleanup();
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(INTYG_ID);
    }

    @Test
    public void registerMedicalCertificate() {
        final String personId = "19010101-0101";

        getMedicalCertificateForCareRequest(INTYG_ID, personId).
                body("result.resultCode", is("ERROR")).
                body("result.resultText", is("Certificate 'registerMedicalCertificateITcertificateId' does not exist for user '416a6b845a3314138feda9649a016885b9c1cd16877dfa74abe3d2d5e6df9ba6'"));
        givenRequest(INTYG_ID, personId, false).
                body("result.resultCode", is("OK"));
        getMedicalCertificateForCareRequest(INTYG_ID, personId).
                body("result.resultCode", is("OK")).
                body("meta.certificateId", is(INTYG_ID)).
                body("lakarutlatande.skapadAvHosPersonal.enhet.enhets-id.@extension", is("EnhetsId")).
                body("lakarutlatande.skapadAvHosPersonal.enhet.vardgivare.vardgivare-id.@extension", is("Vardgivarid")).
                body("meta.status.type", is("RECEIVED"));

        // can not register again
        givenRequest(INTYG_ID, personId, false).
                body("result.resultCode", is("INFO")).
                body("result.infoText", is("Certificate already exists"));
    }

    @Test
    public void registerMedicalCertificateIdAlreadyExistOnOtherPerson() {
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP, "19020202-0202", false);

        givenRequest(INTYG_ID, "19010101-0101", false).
                body("result.resultCode", is("ERROR")).
                body("result.errorId", is("APPLICATION_ERROR")).
                body("result.errorText", is("Invalid certificate ID"));
    }

    @Test
    public void faultTransformerTest() {
        givenRequest("</tag>", "190101010101", false).
                body("result.resultCode", is("ERROR")).
                body("result.errorText", startsWith("Unmarshalling Error"));
    }

    private ValidatableResponse givenRequest(String intygId, String personId, boolean SmL) {
        ST requestTemplate = templateGroup.getInstanceOf(SmL ? "smLRequest" : "request");
        requestTemplate.add("intygId", intygId);
        requestTemplate.add("personId", personId);

        return given().body(requestTemplate.render()).
                when().
                post("inera-certificate/register-certificate/v3.0").
                then().
                statusCode(200).
                rootPath("Envelope.Body.RegisterMedicalCertificateResponse.");
    }

    private ValidatableResponse getMedicalCertificateForCareRequest(String intygId, String personId) {
        ST requestTemplate = new STGroupFile("integrationtests/getmedicalcertificateforcare/requests.stg").getInstanceOf("request");
        requestTemplate.add("intygId", intygId);
        requestTemplate.add("personId", personId);

        return given().body(requestTemplate.render()).
                when().
                post("inera-certificate/get-medical-certificate-for-care/v1.0").
                then().
                statusCode(200).
                rootPath("Envelope.Body.GetMedicalCertificateForCareResponse.");
    }
}
