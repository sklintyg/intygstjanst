package se.inera.intyg.intygstjanst.web.integrationtest.certificate.v1;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.*;
import org.stringtemplate.v4.*;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.response.ValidatableResponse;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

public class ListCertificatesIT extends BaseIntegrationTest {

    private STGroup templateGroup;

    private static final List<String> INTYG_IDS = Arrays.asList("listCertificatesITfk7263certificateId", "listCertificatesITlusecertificateId");
    private static final String PERSON_ID = "19010101-0101";

    @Override
    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/listcertificates/requests.stg");
        cleanup();
    }

    @After
    public void cleanup() {
        INTYG_IDS.stream().forEach(id -> IntegrationTestUtil.deleteIntyg(id));
        IntegrationTestUtil.revokeConsent(PERSON_ID);
    }

    @Test
    public void listCertificates() {
        IntegrationTestUtil.givenIntyg(INTYG_IDS.get(0), "fk7263", PERSON_ID, false);
        IntegrationTestUtil.givenIntyg(INTYG_IDS.get(1), "luse", PERSON_ID, false);
        IntegrationTestUtil.addConsent(PERSON_ID);

        givenRequest(PERSON_ID).
                body("meta.size()", is(2)).
                body("meta[0].certificateId", is(INTYG_IDS.get(0))).
                body("meta[1].certificateId", is(INTYG_IDS.get(1))).
                body("result.resultCode", is("OK"));
    }

    @Test
    public void listCertificatesCertificateType() {
        IntegrationTestUtil.givenIntyg(INTYG_IDS.get(0), "fk7263", PERSON_ID, false);
        IntegrationTestUtil.givenIntyg(INTYG_IDS.get(1), "luse", PERSON_ID, false);
        IntegrationTestUtil.addConsent(PERSON_ID);

        givenCertificateTypeRequest(PERSON_ID, "fk7263").
                body("meta.size()", is(1)).
                body("meta[0].certificateId", is(INTYG_IDS.get(0))).
                body("result.resultCode", is("OK"));
    }

    @Test
    public void listCertificatesDateInterval() {
        IntegrationTestUtil.givenIntyg(INTYG_IDS.get(0), "fk7263", PERSON_ID, false);
        IntegrationTestUtil.givenIntyg(INTYG_IDS.get(1), "luse", PERSON_ID, false);
        IntegrationTestUtil.addConsent(PERSON_ID);

        givenDateIntervalRequest(PERSON_ID, LocalDateTime.now(), LocalDateTime.now().plusDays(2)).
                body("meta.size()", is(0)).
                body("result.resultCode", is("OK"));
    }

    @Test
    public void listCertificatesNoConsent() {
        IntegrationTestUtil.givenIntyg(INTYG_IDS.get(0), "fk7263", PERSON_ID, false);
        IntegrationTestUtil.givenIntyg(INTYG_IDS.get(1), "luse", PERSON_ID, false);

        givenRequest(PERSON_ID).
                body("meta.size()", is(0)).
                body("result.resultCode", is("INFO")).
                body("result.infoText", is("NOCONSENT"));
    }

    @Test
    public void faultTransformerTest() {
        givenRequest("</tag>").
                body("result.resultCode", is("ERROR")).
                body("result.errorText", startsWith("Unmarshalling Error"));
    }

    private ValidatableResponse givenRequest(String personId) {
        return givenRequest(personId, null, null, null, "request");
    }

    private ValidatableResponse givenCertificateTypeRequest(String personId, String certificateType) {
        return givenRequest(personId, certificateType, null, null, "requestCertificateType");
    }

    private ValidatableResponse givenDateIntervalRequest(String personId, LocalDateTime fromDate, LocalDateTime toDate) {
        return givenRequest(personId, null, fromDate, toDate, "requestDateInterval");
    }

    private ValidatableResponse givenRequest(String personId, String certificateType, LocalDateTime fromDate, LocalDateTime toDate, String template) {
        ST requestTemplate = templateGroup.getInstanceOf(template);
        requestTemplate.add("personId", personId);
        if (certificateType != null) {
            requestTemplate.add("certificateType", certificateType);
        }
        if (fromDate != null) {
            requestTemplate.add("fromDate", fromDate.toString());
        }
        if (toDate != null) {
            requestTemplate.add("toDate", toDate.toString());
        }

        return given().body(requestTemplate.render()).
                when().
                post("inera-certificate/list-certificates/v1.0").
                then().
                statusCode(200).
                rootPath("Envelope.Body.ListCertificatesResponse.");
    }
}