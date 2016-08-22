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

public class GetMedicalCertificateForCareIT extends BaseIntegrationTest {

    private STGroup templateGroup;

    private static final String INTYG_ID = "getMedicalCertificateForCareITcertificateId";
    private static final String INTYG_TYP = "fk7263";

    @Override
    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/getmedicalcertificateforcare/requests.stg");
        cleanup();
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(INTYG_ID);
    }

    @Test
    public void getMedicalCertificateForCare() {
        final String personId = "190101010101";
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP, personId, false);

        givenRequest(INTYG_ID, personId).
                body("result.resultCode", is("OK")).
                body("meta.certificateId", is(INTYG_ID)).
                body("meta.status.type", is("RECEIVED")).
                body("lakarutlatande.patient.person-id.@extension", is(personId));
    }

    @Test
    public void getMedicalCertificateForCareCertificateDoesNotExist() {
        givenRequest("fit-intyg-finnsinte", "190101010101").
                body("result.resultCode", is("ERROR")).
                body("result.errorId", is("VALIDATION_ERROR")).
                body("result.resultText", is("Certificate 'fit-intyg-finnsinte' does not exist for user '416a6b845a3314138feda9649a016885b9c1cd16877dfa74abe3d2d5e6df9ba6'"));
    }

    @Test
    public void getMedicalCertificateForCareRevoked() {
        final String personId = "190101010101";
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP, personId, false);
        IntegrationTestUtil.revokeMedicalCertificate(INTYG_ID, personId, "");

        givenRequest(INTYG_ID, personId).
                body("meta.certificateId", is(INTYG_ID)).
                body("result.resultCode", is("ERROR")).
                body("result.errorId", is("REVOKED")).
                body("result.resultText", is("Certificate 'getMedicalCertificateForCareITcertificateId' has been revoked"));
    }

    @Test
    public void getMedicalCertificateForCareDeletedByCareGiver() {
        final String personId = "190101010101";
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP, personId, true);

        givenRequest(INTYG_ID, personId).
                body("result.resultCode", is("ERROR")).
                body("result.errorId", is("APPLICATION_ERROR")).
                body("result.resultText", is("Certificate 'getMedicalCertificateForCareITcertificateId' has been deleted by care giver"));
    }

    @Test
    public void faultTransformerTest() {
        givenRequest("</tag>", "190101010101").
                body("result.resultCode", is("ERROR")).
                body("result.resultText", startsWith("Unmarshalling Error"));
    }

    private ValidatableResponse givenRequest(String intygId, String personId) {
        ST requestTemplate = templateGroup.getInstanceOf("request");
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
