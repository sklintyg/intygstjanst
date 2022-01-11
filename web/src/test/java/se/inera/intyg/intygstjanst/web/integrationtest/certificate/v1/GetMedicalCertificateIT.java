/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.ValidatableResponse;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

public class GetMedicalCertificateIT extends BaseIntegrationTest {

    private STGroup templateGroup;

    private static final String INTYG_ID = "getMedicalCertificateITcertificateId";
    private static final String INTYG_TYP_FK7263 = "fk7263";

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/getmedicalcertificate/requests.stg");
        cleanup();
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(INTYG_ID);
    }

    @Test
    public void getMedicalCertificate() {
        final String personId = "190101010101";
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP_FK7263, FK7263_VERSION, personId, false);

        givenRequest(INTYG_ID, personId).body("result.resultCode", is("OK")).body("meta.certificateId", is(INTYG_ID))
            .body("meta.status.type", is("RECEIVED")).body("lakarutlatande.patient.person-id.@extension", is(personId));
    }

    @Test
    public void getMedicalCertificateCertificateDoesNotExist() {
        givenRequest("fit-intyg-finnsinte", "190101010101").body("result.resultCode", is("ERROR"))
            .body("result.errorId", is("VALIDATION_ERROR"))
            .body("result.resultText", is(
                "Certificate 'fit-intyg-finnsinte' does not exist for user '416a6b845a3314138feda9649a016885b9c1cd16877dfa74abe3d2d5e6df9ba6'"));
    }

    @Test
    public void getMedicalCertificateRevoked() {
        final String personId = "190101010101";
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP_FK7263, FK7263_VERSION, personId, false);
        IntegrationTestUtil.revokeMedicalCertificate(INTYG_ID, personId, "");

        givenRequest(INTYG_ID, personId).body("meta.certificateId", is(INTYG_ID)).body("result.resultCode", is("ERROR"))
            .body("result.errorId", is("REVOKED"))
            .body("result.resultText", is("Certificate 'getMedicalCertificateITcertificateId' has been revoked"));
    }

    @Test
    public void getMedicalCertificateDeletedByCareGiver() {
        final String personId = "190101010101";
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP_FK7263, FK7263_VERSION, personId, true);

        givenRequest(INTYG_ID, personId).body("result.resultCode", is("ERROR")).body("result.errorId", is("APPLICATION_ERROR"))
            .body("result.resultText", is("Certificate 'getMedicalCertificateITcertificateId' has been deleted by care giver"));
    }

    @Test
    public void faultTransformerTest() {
        givenRequest("</tag>", "190101010101").body("result.resultCode", is("ERROR"))
            .body("result.resultText", startsWith("Unmarshalling Error"));
    }

    private ValidatableResponse givenRequest(String intygId, String personId) {
        ST requestTemplate = templateGroup.getInstanceOf("request");
        requestTemplate.add("intygId", intygId);
        requestTemplate.add("personId", personId);

        return given().body(requestTemplate.render()).when().post("inera-certificate/get-medical-certificate/v1.0").then().statusCode(200)
            .rootPath("Envelope.Body.GetMedicalCertificateResponse.");
    }
}
