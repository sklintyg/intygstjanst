/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.intygstjanst.web.integrationtest.certificate.v1;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.response.ValidatableResponse;
import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertTrue;

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
    }

    @Test
    public void getCertificate() {
        IntegrationTestUtil.registerMedicalCertificate(INTYG_ID, PERSON_ID);

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

        givenRequest(INTYG_ID, PERSON_ID).body("result.resultCode", is("INFO")).body("result.infoText",
                is("Certificate 'getCertificateInsuranceProcessITcertificateId' has been revoked"));
    }

    @Test
    public void getCertificateWrongPerson() {
        IntegrationTestUtil.registerMedicalCertificate(INTYG_ID, "19020202-0202");

        givenRequest(INTYG_ID, PERSON_ID).body("result.resultCode", is("ERROR")).body("result.errorId", is("VALIDATION_ERROR")).body(
                "result.errorText",
                is("Certificate 'getCertificateInsuranceProcessITcertificateId' does not exist for user '416a6b845a3314138feda9649a016885b9c1cd16877dfa74abe3d2d5e6df9ba6'"));
    }

    @Test
    public void getCertificateCertificateDoesNotExist() {
        givenRequest("fit-intyg-finnsinte", PERSON_ID).body("result.resultCode", is("ERROR")).body("result.errorId", is("VALIDATION_ERROR")).body(
                "result.errorText",
                is("Certificate 'fit-intyg-finnsinte' does not exist for user '416a6b845a3314138feda9649a016885b9c1cd16877dfa74abe3d2d5e6df9ba6'"));
    }

    @Test
    public void getCertificateSmL() {
        IntegrationTestUtil.registerMedicalCertificate(INTYG_ID, PERSON_ID, "smLRequest");

        givenRequest(INTYG_ID, PERSON_ID).body("result.resultCode", is("OK")).body("meta.certificateId", is(INTYG_ID))
                .body("meta.status.type", is("RECEIVED"))
                .body("certificate.RegisterMedicalCertificate.lakarutlatande.lakarutlatande-id", is(INTYG_ID))
                .body("certificate.RegisterMedicalCertificate.lakarutlatande.patient.person-id.@extension", is(PERSON_ID));
    }

    @Test
    public void getCertificateTransformTest() throws Exception {
        String template = "transformRequest";

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
