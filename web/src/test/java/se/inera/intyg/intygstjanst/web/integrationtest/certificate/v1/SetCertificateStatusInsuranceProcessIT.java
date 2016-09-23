package se.inera.intyg.intygstjanst.web.integrationtest.certificate.v1;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.*;
import org.stringtemplate.v4.*;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.response.ValidatableResponse;

import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

public class SetCertificateStatusInsuranceProcessIT extends BaseIntegrationTest {

    private STGroup templateGroup;

    private static final String INTYG_ID = "setCertificateStatusInsuranceprocessITcertificateId";
    private static final String INTYG_TYP = "fk7263";

    @Override
    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/setcertificatestatusinsuranceprocess/requests.stg");
        cleanup();
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(INTYG_ID);
    }

    @Test
    public void setCertificateStatus() {
        final String personId = "190101010101";
        final String status = CertificateState.SENT.name();
        IntegrationTestUtil.registerMedicalCertificate(INTYG_ID, personId);

        getMedicalCertificateForCareRequest(INTYG_ID, personId).body("meta.status.size()", is(1)).body("meta.status.type", is("RECEIVED"));
        givenRequest(INTYG_ID, personId, "FK", status, LocalDateTime.now()).body("result.resultCode", is("OK"));
        getMedicalCertificateForCareRequest(INTYG_ID, personId).body("meta.status.size()", is(2))
                .body("meta.status[0].type", anyOf(is("RECEIVED"), is(status))).body("meta.status[1].type", anyOf(is("RECEIVED"), is(status)));
    }

    @Test
    public void setCertificateStatusMultipleTimes() {
        final String personId = "190101010101";
        final String status = CertificateState.SENT.name();
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP, personId, false);

        getMedicalCertificateForCareRequest(INTYG_ID, personId).body("meta.status.size()", is(1)).body("meta.status.type", is("RECEIVED"));
        // mark as sent twice
        givenRequest(INTYG_ID, personId, "FK", status, LocalDateTime.now()).body("result.resultCode", is("OK"));
        givenRequest(INTYG_ID, personId, "FK", status, LocalDateTime.now()).body("result.resultCode", is("OK"));
        // find two status "sent"
        getMedicalCertificateForCareRequest(INTYG_ID, personId).body("meta.status.size()", is(3))
                .body("meta.status[0].type", anyOf(is("RECEIVED"), is(status))).body("meta.status[1].type", anyOf(is("RECEIVED"), is(status)))
                .body("meta.status[2].type", anyOf(is("RECEIVED"), is(status)));
    }

    @Test
    public void setCertificateStatusCertificateDoesNotExist() {
        givenRequest("fit-intyg-finnsinte", "190101010101", "FK", CertificateState.SENT.name(), LocalDateTime.now())
                .body("result.resultCode", is("ERROR")).body("result.errorId", is("VALIDATION_ERROR")).body("result.errorText", is(
                        "Certificate 'fit-intyg-finnsinte' does not exist for user '416a6b845a3314138feda9649a016885b9c1cd16877dfa74abe3d2d5e6df9ba6'"));
    }

    @Test
    public void setCertificateStatusWrongPerson() {
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP, "19020202-0202", false);
        givenRequest("fit-intyg-finnsinte", "190101010101", "FK", CertificateState.SENT.name(), LocalDateTime.now())
                .body("result.resultCode", is("ERROR")).body("result.errorId", is("VALIDATION_ERROR")).body("result.errorText", is(
                        "Certificate 'fit-intyg-finnsinte' does not exist for user '416a6b845a3314138feda9649a016885b9c1cd16877dfa74abe3d2d5e6df9ba6'"));
    }

    @Test
    public void faultTransformerTest() {
        givenRequest("</tag>", "190101010101", "FK", CertificateState.SENT.name(), LocalDateTime.now()).body("result.resultCode", is("ERROR"))
                .body("result.errorText", startsWith("Unmarshalling Error"));
    }

    private ValidatableResponse givenRequest(String intygId, String personId, String target, String status, LocalDateTime timestamp) {
        ST requestTemplate = templateGroup.getInstanceOf("request");
        requestTemplate.add("intygId", intygId);
        requestTemplate.add("personId", personId);
        requestTemplate.add("target", target);
        requestTemplate.add("status", status);
        requestTemplate.add("timestamp", timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return given().body(requestTemplate.render()).when().post("inera-certificate/set-certificate-status/v1.0").then().statusCode(200)
                .rootPath("Envelope.Body.SetCertificateStatusResponse.");
    }

    private ValidatableResponse getMedicalCertificateForCareRequest(String intygId, String personId) {
        ST requestTemplate = new STGroupFile("integrationtests/getmedicalcertificateforcare/requests.stg").getInstanceOf("request");
        requestTemplate.add("intygId", intygId);
        requestTemplate.add("personId", personId);

        return given().body(requestTemplate.render()).when().post("inera-certificate/get-medical-certificate-for-care/v1.0").then().statusCode(200)
                .rootPath("Envelope.Body.GetMedicalCertificateForCareResponse.");
    }
}
