package se.inera.intyg.intygstjanst.web.integrationtest.certificate.v1;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;

import org.junit.*;
import org.stringtemplate.v4.*;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.response.ValidatableResponse;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

public class RevokeMedicalCertificateIT extends BaseIntegrationTest {

    private STGroup templateGroup;

    private static final String INTYG_ID = "revokeMedicalCertificateITcertificateId";
    private static final String INTYG_TYP = "fk7263";

    @Override
    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/revokemedicalcertificate/requests.stg");
        cleanup();
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(INTYG_ID);
    }

    @Test
    public void revokeMedicalCertificate() {
        final String personId = "190101010101";
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP, personId, false);

        getMedicalCertificateForCareRequest(INTYG_ID, personId).
                body("meta.status.size()", is(1)).
                body("meta.status.type", is("RECEIVED"));
        givenRequest(INTYG_ID, personId, "meddelande").
                body("result.resultCode", is("OK"));
        getMedicalCertificateForCareRequest(INTYG_ID, personId).
                body("meta.status.size()", is(2)).
                body("meta.status[0].type", anyOf(is("RECEIVED"), is("CANCELLED"))).
                body("meta.status[1].type", anyOf(is("RECEIVED"), is("CANCELLED")));

        // can not revoke when already revoked
        givenRequest(INTYG_ID, personId, "meddelande").
                body("result.resultCode", is("INFO")).
                body("result.infoText", is("Certificate 'revokeMedicalCertificateITcertificateId' is already revoked."));
    }

    @Test
    public void revokeMedicalCertificateDoesNotExist() {
        final String personId = "190101010101";

        givenRequest("fit-intyg-finnsinte", personId, "meddelande").
                body("result.resultCode", is("ERROR")).
                body("result.errorId", is("VALIDATION_ERROR")).
                body("result.errorText", is("No certificate 'fit-intyg-finnsinte' found to revoke for patient '416a6b845a3314138feda9649a016885b9c1cd16877dfa74abe3d2d5e6df9ba6'."));
    }

    @Test
    public void revokeMedicalCertificateWithBlanksteg() {
        final String personId = "190101010101";
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP, personId, false);

        getMedicalCertificateForCareRequest(INTYG_ID, personId).
                body("meta.status.size()", is(1)).
                body("meta.status.type", is("RECEIVED"));
        givenRequest(INTYG_ID, personId, "meddelande", "blankstegRequest").
                body("result.resultCode", is("OK"));
        getMedicalCertificateForCareRequest(INTYG_ID, personId).
                body("meta.status.size()", is(2)).
                body("meta.status[0].type", anyOf(is("RECEIVED"), is("CANCELLED"))).
                body("meta.status[1].type", anyOf(is("RECEIVED"), is("CANCELLED")));
    }

    @Test
    public void faultTransformerTest() {
        givenRequest("</tag>", "190101010101", "").
                body("result.resultCode", is("ERROR")).
                body("result.errorText", startsWith("Unmarshalling Error"));
    }

    private ValidatableResponse givenRequest(String intygId, String personId, String meddelande) {
        return givenRequest(intygId, personId, meddelande, "request");
    }

    private ValidatableResponse givenRequest(String intygId, String personId, String meddelande, String template) {
        ST requestTemplate = templateGroup.getInstanceOf(template);
        requestTemplate.add("intygsId", intygId);
        requestTemplate.add("personId", personId);
        requestTemplate.add("meddelande", meddelande);

        return given().body(requestTemplate.render()).
                when().
                post("inera-certificate/revoke-certificate/v1.0").
                then().
                statusCode(200).
                rootPath("Envelope.Body.RevokeMedicalCertificateResponse.");
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