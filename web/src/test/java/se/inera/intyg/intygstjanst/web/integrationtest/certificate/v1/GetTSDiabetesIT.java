/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

public class GetTSDiabetesIT extends BaseIntegrationTest {

    private STGroup templateGroup;

    private static final String INTYG_ID = "getTSDiabetesITcertificateId";
    private static final String INTYG_TYP = "ts-diabetes";

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/gettsdiabetes/requests.stg");
        cleanup();
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(INTYG_ID);
    }

    @Test
    public void getTSDiabetes() {
        final String personId = "190101010101";
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP, personId, false);

        givenRequest(INTYG_ID, personId).body("resultat.resultCode", is("OK")).body("intyg.intygsId", is(INTYG_ID))
                .body("meta.status.type", is("RECEIVED")).body("intyg.grundData.patient.personId.extension", is(personId));
    }

    @Test
    public void getTSDiabetesDoesNotExist() {
        givenRequest("fit-intyg-finnsinte", "190101010101").body("resultat.resultCode", is("ERROR")).body("resultat.errorId", is("VALIDATION_ERROR"))
                .body("resultat.resultText", is(
                        "Certificate 'fit-intyg-finnsinte' does not exist for user '416a6b845a3314138feda9649a016885b9c1cd16877dfa74abe3d2d5e6df9ba6'"));
    }

    @Test
    public void getTSDiabetesRevoked() {
        final String personId = "190101010101";
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP, personId, false);
        IntegrationTestUtil.revokeMedicalCertificate(INTYG_ID, personId, "");

        givenRequest(INTYG_ID, personId).body("intyg.intygsId", is(INTYG_ID)).body("resultat.resultCode", is("ERROR"))
                .body("resultat.errorId", is("REVOKED"))
                .body("resultat.resultText", is("Certificate 'getTSDiabetesITcertificateId' has been revoked"));
    }

    @Test
    public void getTSDiabetesDeletedByCareGiver() {
        final String personId = "190101010101";
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP, personId, true);

        givenRequest(INTYG_ID, personId).body("resultat.resultCode", is("ERROR")).body("resultat.errorId", is("APPLICATION_ERROR"))
                .body("resultat.resultText", is("Certificate 'getTSDiabetesITcertificateId' has been deleted by care giver"));
    }

    @Test
    public void getTSDiabetesWrongPerson() {
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP, "19020202-0202", false);

        givenRequest(INTYG_ID, "190101010101").body("resultat.resultCode", is("ERROR")).body("resultat.errorId", is("VALIDATION_ERROR")).body(
                "resultat.resultText",
                is("Certificate 'getTSDiabetesITcertificateId' does not exist for user '416a6b845a3314138feda9649a016885b9c1cd16877dfa74abe3d2d5e6df9ba6'"));
    }

    @Test
    public void faultTransformerTest() {
        givenRequest("</tag>", "190101010101").body("resultat.resultCode", is("ERROR")).body("resultat.resultText",
                startsWith("Unmarshalling Error"));
    }

    private ValidatableResponse givenRequest(String intygId, String personId) {
        ST requestTemplate = templateGroup.getInstanceOf("request");
        requestTemplate.add("intygId", intygId);
        requestTemplate.add("personId", personId);

        return given().body(requestTemplate.render()).when().post("inera-certificate/get-ts-diabetes/v1.0").then().statusCode(200)
                .rootPath("Envelope.Body.GetTSDiabetesResponse.");
    }
}
