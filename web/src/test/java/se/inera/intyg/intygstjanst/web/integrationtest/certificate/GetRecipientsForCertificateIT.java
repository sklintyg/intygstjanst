/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integrationtest.certificate;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.*;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.response.ValidatableResponse;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;

public class GetRecipientsForCertificateIT extends BaseIntegrationTest {

    private STGroup templateGroup;

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/getrecipientsforcertificate/requests.stg");
    }

    @Test
    public void getRecipientsForCertificateFk7263() {
        givenRequest("fk7263").body("result.resultCode", is("OK")).body("recipient.size()", is(1)).body("recipient.id", is("FK"))
                .body("recipient.name", is("Försäkringskassan"));
    }

    @Test
    public void getRecipientsForCertificateLuse() {
        givenRequest("luse").body("result.resultCode", is("OK")).body("recipient.size()", is(1)).body("recipient.id", is("FK")).body("recipient.name",
                is("Försäkringskassan"));
    }

    @Test
    public void getRecipientsForCertificateLuaeFs() {
        givenRequest("luae_fs").body("result.resultCode", is("OK")).body("recipient.size()", is(1)).body("recipient.id", is("FK"))
                .body("recipient.name", is("Försäkringskassan"));
    }

    @Test
    public void getRecipientsForCertificateTsBas() {
        givenRequest("ts-bas").body("result.resultCode", is("OK")).body("recipient.size()", is(1)).body("recipient.id", is("TS"))
                .body("recipient.name", is("Transportstyrelsen"));
    }

    @Test
    public void getRecipientsForCertificateTsDiabetes() {
        givenRequest("ts-diabetes").body("result.resultCode", is("OK")).body("recipient.size()", is(1)).body("recipient.id", is("TS"))
                .body("recipient.name", is("Transportstyrelsen"));
    }

    @Test
    public void getRecipientsForCertificateUnknownCertificateType() {
        givenRequest("unknown").body("recipient.size()", is(0)).body("result.resultCode", is("ERROR")).body("result.errorId", is("APPLICATION_ERROR"))
                .body("result.resultText", is("No recipients found for certificate type: unknown"));
    }

    private ValidatableResponse givenRequest(String intygTyp) {
        ST requestTemplate = templateGroup.getInstanceOf("request");
        requestTemplate.add("intygTyp", intygTyp);

        return given().body(requestTemplate.render()).when().post("inera-certificate/get-recipients-for-certificate/v1.0").then().statusCode(200)
                .rootPath("Envelope.Body.GetRecipientsForCertificateResponse.");
    }
}
