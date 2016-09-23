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

public class SendMedicalCertificateIT extends BaseIntegrationTest {

    private STGroup templateGroup;

    private static final String INTYG_ID = "sendMedicalCertificateITcertificateId";
    private static final String INTYG_TYP = "fk7263";

    @Override
    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/sendmedicalcertificate/requests.stg");
        cleanup();
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(INTYG_ID);
    }

    @Test
    public void sendMedicalCertificate() {
        final String personId = "19010101-0101";
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP, personId, false);

        getMedicalCertificateForCareRequest(INTYG_ID, personId).body("meta.status.size()", is(1)).body("meta.status.type", is("RECEIVED"));
        givenRequest(INTYG_ID, personId).body("result.resultCode", is("OK"));
        getMedicalCertificateForCareRequest(INTYG_ID, personId).body("meta.status.size()", is(2))
                .body("meta.status[0].type", anyOf(is("RECEIVED"), is("SENT"))).body("meta.status[1].type", anyOf(is("RECEIVED"), is("SENT")));

        // can not send when already sent
        givenRequest(INTYG_ID, personId).body("result.resultCode", is("INFO")).body("result.infoText",
                is("Certificate 'sendMedicalCertificateITcertificateId' is already sent."));
    }

    @Test
    public void registerAndSendCertificateWithBlanksteg() {
        final String personId = "19010101-0101";
        IntegrationTestUtil.registerMedicalCertificate(INTYG_ID, personId, "blankstegRequest");

        givenRequest(INTYG_ID, personId).body("result.resultCode", is("OK"));

        getMedicalCertificateForCareRequest(INTYG_ID, personId).body("meta.status.size()", is(2))
                .body("meta.status[0].type", anyOf(is("RECEIVED"), is("SENT"))).body("meta.status[1].type", anyOf(is("RECEIVED"), is("SENT")));
    }

    /**
     * Vissa landsting skickar en obsolet <arbetsuppgift /> tagg utan <typAvArbetsuppgift>.
     * Detta är inte korrekt enligt schemat, men valideras bara om typAvSysselsattning är Nuvarande_arbete.
     * Detta gav upphov till bug INTYG-1413, då det ledde till att typAvSysselsattning felaktigt även sattes till
     * Nuvarande_arbete, trots att intyget angav typAvSysselsättning till Arbetsloshet.
     *
     * Detta test säkerställer att dessa intyg korrekt kan registreras och skickas vidare till FK.
     */
    @Test
    public void registerAndSendCertificateWithObsoletArbetsuppgift() {
        final String personId = "19010101-0101";
        IntegrationTestUtil.registerMedicalCertificate(INTYG_ID, personId, "obsoletArbetsuppgiftRequest");

        givenRequest(INTYG_ID, personId).body("result.resultCode", is("OK"));

        getMedicalCertificateForCareRequest(INTYG_ID, personId).body("meta.status.size()", is(2))
                .body("meta.status[0].type", anyOf(is("RECEIVED"), is("SENT"))).body("meta.status[1].type", anyOf(is("RECEIVED"), is("SENT")));
    }

    /**
     * INTYG-1420: Intyg med orimliga datum skall kunna registreras och skickas vidare till FK.
     */
    @Test
    public void registerAndSendCertificateWithOrimligaDatum() {
        final String personId = "19010101-0101";
        IntegrationTestUtil.registerMedicalCertificate(INTYG_ID, personId, "orimligaDatumRequest");

        givenRequest(INTYG_ID, personId).body("result.resultCode", is("OK"));

        getMedicalCertificateForCareRequest(INTYG_ID, personId).body("meta.status.size()", is(2))
                .body("meta.status[0].type", anyOf(is("RECEIVED"), is("SENT"))).body("meta.status[1].type", anyOf(is("RECEIVED"), is("SENT")));
    }

    @Test
    public void sendMedicalCertificateRevoked() {
        final String personId = "19010101-0101";
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP, personId, false);
        IntegrationTestUtil.revokeMedicalCertificate(INTYG_ID, personId, "meddelande");

        givenRequest(INTYG_ID, personId).body("result.resultCode", is("INFO")).body("result.infoText",
                is("Certificate 'sendMedicalCertificateITcertificateId' has been revoked."));
    }

    @Test
    public void sendMedicalCertificateDoesNotExist() {
        givenRequest(INTYG_ID, "19010101-0101").body("result.resultCode", is("ERROR")).body("result.errorText", is(
                "No certificate 'sendMedicalCertificateITcertificateId' found to send for patient '416a6b845a3314138feda9649a016885b9c1cd16877dfa74abe3d2d5e6df9ba6'."));
    }

    @Test
    public void sendMedicalCertificateWrongPerson() {
        final String personId = "19010101-0101";
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP, "19020202-0202", false);

        givenRequest(INTYG_ID, personId).body("result.resultCode", is("ERROR")).body("result.errorText", is(
                "No certificate 'sendMedicalCertificateITcertificateId' found to send for patient '416a6b845a3314138feda9649a016885b9c1cd16877dfa74abe3d2d5e6df9ba6'."));
    }

    @Test
    public void sendMedicalCertificateWithBlanksteg() {
        final String personId = "19010101-0101";
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP, personId, false);

        getMedicalCertificateForCareRequest(INTYG_ID, personId).body("meta.status.size()", is(1)).body("meta.status.type", is("RECEIVED"));
        givenRequest(INTYG_ID, personId, "blankstegRequest").body("result.resultCode", is("OK"));
        getMedicalCertificateForCareRequest(INTYG_ID, personId).body("meta.status.size()", is(2))
                .body("meta.status[0].type", anyOf(is("RECEIVED"), is("SENT"))).body("meta.status[1].type", anyOf(is("RECEIVED"), is("SENT")));
    }

    @Test
    public void faultTransformerTest() {
        givenRequest("</tag>", "190101010101").body("result.resultCode", is("ERROR")).body("result.errorText", startsWith("Unmarshalling Error"));
    }

    private ValidatableResponse givenRequest(String intygId, String personId) {
        return givenRequest(intygId, personId, "request");
    }

    private ValidatableResponse givenRequest(String intygId, String personId, String template) {
        ST requestTemplate = templateGroup.getInstanceOf(template);
        requestTemplate.add("intygId", intygId);
        requestTemplate.add("personId", personId);

        return given().body(requestTemplate.render()).when().post("inera-certificate/send-certificate/v1.0").then().statusCode(200)
                .rootPath("Envelope.Body.SendMedicalCertificateResponse.");
    }

    private ValidatableResponse getMedicalCertificateForCareRequest(String intygId, String personId) {
        ST requestTemplate = new STGroupFile("integrationtests/getmedicalcertificateforcare/requests.stg").getInstanceOf("request");
        requestTemplate.add("intygId", intygId);
        requestTemplate.add("personId", personId);

        return given().body(requestTemplate.render()).when().post("inera-certificate/get-medical-certificate-for-care/v1.0").then().statusCode(200)
                .rootPath("Envelope.Body.GetMedicalCertificateForCareResponse.");
    }
}
