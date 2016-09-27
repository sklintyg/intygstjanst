package se.inera.intyg.intygstjanst.web.integrationtest.certificate.v1;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertTrue;

import org.custommonkey.xmlunit.*;
import org.junit.*;
import org.stringtemplate.v4.*;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.response.ValidatableResponse;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

public class GetCertificateInsuranceProcessIT extends BaseIntegrationTest {

    private STGroup templateGroup;

    private static final String INTYG_ID = "getCertificateInsuranceProcessITcertificateId";
    private static final String PERSON_ID = "19010101-0101";

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/getcertificateinsuranceprocess/requests.stg");
        cleanup();
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(INTYG_ID);
        IntegrationTestUtil.revokeConsent(PERSON_ID);
    }

    @Test
    public void getCertificate() {
        IntegrationTestUtil.registerMedicalCertificate(INTYG_ID, PERSON_ID);
        IntegrationTestUtil.addConsent(PERSON_ID);

        givenRequest(INTYG_ID, PERSON_ID).body("meta.certificateId", is(INTYG_ID)).body("meta.status.type", is("RECEIVED"))
                .body("certificate.RegisterMedicalCertificate.lakarutlatande.lakarutlatande-id", is(INTYG_ID))
                .body("certificate.RegisterMedicalCertificate.lakarutlatande.patient.person-id.@extension", is(PERSON_ID))
                .body("certificate.RegisterMedicalCertificate.lakarutlatande.skapadAvHosPersonal.personal-id.@extension", is("Personal HSA-ID"))
                .body("certificate.RegisterMedicalCertificate.lakarutlatande.skapadAvHosPersonal.fullstandigtNamn", is("Abra Kadabra"));
    }

    @Test
    public void getCertificateRevoked() {
        IntegrationTestUtil.registerMedicalCertificate(INTYG_ID, PERSON_ID);
        IntegrationTestUtil.revokeMedicalCertificate(INTYG_ID, PERSON_ID, "meddelande");
        IntegrationTestUtil.addConsent(PERSON_ID);

        givenRequest(INTYG_ID, PERSON_ID).body("result.resultCode", is("INFO")).body("result.infoText",
                is("Certificate 'getCertificateInsuranceProcessITcertificateId' has been revoked"));
    }

    @Test
    public void getCertificateWithoutConsent() {
        IntegrationTestUtil.registerMedicalCertificate(INTYG_ID, PERSON_ID);

        givenRequest(INTYG_ID, PERSON_ID).body("result.resultCode", is("ERROR")).body("result.errorId", is("VALIDATION_ERROR"))
                .body("result.errorText", is("Consent required from user 416a6b845a3314138feda9649a016885b9c1cd16877dfa74abe3d2d5e6df9ba6"));
    }

    @Test
    public void getCertificateWrongPerson() {
        IntegrationTestUtil.registerMedicalCertificate(INTYG_ID, "19020202-0202");
        IntegrationTestUtil.addConsent(PERSON_ID);

        givenRequest(INTYG_ID, PERSON_ID).body("result.resultCode", is("ERROR")).body("result.errorId", is("VALIDATION_ERROR")).body(
                "result.errorText",
                is("Certificate 'getCertificateInsuranceProcessITcertificateId' does not exist for user '416a6b845a3314138feda9649a016885b9c1cd16877dfa74abe3d2d5e6df9ba6'"));
    }

    @Test
    public void getCertificateCertificateDoesNotExist() {
        IntegrationTestUtil.addConsent(PERSON_ID);
        givenRequest("fit-intyg-finnsinte", PERSON_ID).body("result.resultCode", is("ERROR")).body("result.errorId", is("VALIDATION_ERROR")).body(
                "result.errorText",
                is("Certificate 'fit-intyg-finnsinte' does not exist for user '416a6b845a3314138feda9649a016885b9c1cd16877dfa74abe3d2d5e6df9ba6'"));
    }

    @Test
    public void getCertificateSmL() {
        IntegrationTestUtil.registerMedicalCertificate(INTYG_ID, PERSON_ID, "smLRequest");
        IntegrationTestUtil.addConsent(PERSON_ID);

        givenRequest(INTYG_ID, PERSON_ID).body("result.resultCode", is("OK")).body("meta.certificateId", is(INTYG_ID))
                .body("meta.status.type", is("RECEIVED"))
                .body("certificate.RegisterMedicalCertificate.lakarutlatande.lakarutlatande-id", is(INTYG_ID))
                .body("certificate.RegisterMedicalCertificate.lakarutlatande.patient.person-id.@extension", is(PERSON_ID));
    }

    @Test
    public void getCertificateTransformTest() throws Exception {
        String template = "transformRequest";
        IntegrationTestUtil.addConsent(PERSON_ID);

        IntegrationTestUtil.registerMedicalCertificate(INTYG_ID, PERSON_ID, template);

        String originalRequest = getRegisterMedicalCertificateSubstring(registerRequest(INTYG_ID, PERSON_ID, template));
        String resultRequest = getRegisterMedicalCertificateSubstring(givenRequest(INTYG_ID, PERSON_ID).extract().asString());

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        Diff diff = XMLUnit.compareXML(originalRequest, resultRequest);
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier("id"));
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void getCertificateTransformSmLTest() throws Exception {
        String template = "transformSmLRequest";
        IntegrationTestUtil.addConsent(PERSON_ID);

        IntegrationTestUtil.registerMedicalCertificate(INTYG_ID, PERSON_ID, template);

        String originalRequest = getRegisterMedicalCertificateSubstring(registerRequest(INTYG_ID, PERSON_ID, template));
        String resultRequest = getRegisterMedicalCertificateSubstring(givenRequest(INTYG_ID, PERSON_ID).extract().asString());

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        Diff diff = XMLUnit.compareXML(originalRequest, resultRequest);
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier("id"));
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void faultTransformerTest() {
        givenRequest("</tag>", PERSON_ID).body("result.resultCode", is("ERROR")).body("result.errorText", startsWith("Unmarshalling Error"));
    }

    private String getRegisterMedicalCertificateSubstring(String originalRequest) {
        final String endOfRequest = "</ns3:RegisterMedicalCertificate>";
        int start = originalRequest.indexOf("<ns3:RegisterMedicalCertificate");
        int end = originalRequest.indexOf(endOfRequest) + endOfRequest.length();
        return originalRequest.substring(start, end);
    }

    private String registerRequest(String intygId, String personId, String template) {
        ST requestTemplate = new STGroupFile("integrationtests/registermedicalcertificate/requests.stg").getInstanceOf(template);
        requestTemplate.add("intygId", intygId);
        requestTemplate.add("personId", personId);
        return requestTemplate.render();
    }

    private ValidatableResponse givenRequest(String intygId, String personId) {
        ST requestTemplate = templateGroup.getInstanceOf("request");
        requestTemplate.add("intygId", intygId);
        requestTemplate.add("personId", personId);

        return given().body(requestTemplate.render()).when().post("inera-certificate/get-certificate/v1.0").then().statusCode(200)
                .rootPath("Envelope.Body.GetCertificateResponse.");
    }
}
