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

public class RegisterTSDiabetesIT extends BaseIntegrationTest {

    private STGroup templateGroup;

    private static final String INTYG_ID = "registerTSDiabetesITcertificateId";
    private static final String INTYG_TYP_TS_DIABETES = "ts-diabetes";

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/registertsdiabetes/requests.stg");
        cleanup();
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(INTYG_ID);
    }

    @Test
    public void registerTSDiabetes() {
        final String personId = "190101010101";

        givenRequest(INTYG_ID, personId).body("resultat.resultCode", is("OK"));

        getTsDiabetesRequest(INTYG_ID, personId).body("resultat.resultCode", is("OK")).body("meta.status.type", is("RECEIVED"));
    }

    @Test
    public void registerTSDiabetesAlreadyExists() {
        final String personId = "190101010101";
        IntegrationTestUtil.givenIntyg(INTYG_ID, INTYG_TYP_TS_DIABETES, TS_DIABETES_VERSION, personId, false);

        givenRequest(INTYG_ID, personId).body("resultat.resultCode", is("INFO"))
            .body("resultat.resultText", is("Certificate already exists"));
    }

    @Test
    public void faultTransformerTest() {
        givenRequest("</tag>", "190101010101").body("resultat.resultCode", is("ERROR")).body("resultat.resultText",
            startsWith("Unmarshalling Error"));
    }

    private ValidatableResponse getTsDiabetesRequest(String intygId, String personId) {
        ST requestTemplate = new STGroupFile("integrationtests/gettsdiabetes/requests.stg").getInstanceOf("request");
        requestTemplate.add("intygId", intygId);
        requestTemplate.add("personId", personId);

        return given().body(requestTemplate.render()).when().post("inera-certificate/get-ts-diabetes/v1.0").then().statusCode(200)
            .rootPath("Envelope.Body.GetTSDiabetesResponse.");
    }

    private ValidatableResponse givenRequest(String intygId, String personId) {
        ST requestTemplate = templateGroup.getInstanceOf("request");
        requestTemplate.add("intygId", intygId);
        requestTemplate.add("personId", personId);

        return given().body(requestTemplate.render()).when().post("inera-certificate/register-ts-diabetes/v1.0").then().statusCode(200)
            .rootPath("Envelope.Body.RegisterTSDiabetesResponse.");
    }
}
