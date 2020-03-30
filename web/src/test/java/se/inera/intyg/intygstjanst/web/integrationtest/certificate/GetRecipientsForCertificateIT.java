/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

import static io.restassured.RestAssured.given;

import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.ValidatableResponse;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;

public class GetRecipientsForCertificateIT extends BaseIntegrationTest {

    private STGroup templateGroup;

    private STGroup registerApprovedTemplateGroup;

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/getrecipientsforcertificate/requests.stg");
        registerApprovedTemplateGroup = new STGroupFile("integrationtests/registerapprovedreceivers/requests.stg");
    }

    @Test
    public void getRecipientForCertificate() {
        // First, register a receiver
        String intygsId = UUID.randomUUID().toString();

        ST requestTemplate = registerApprovedTemplateGroup.getInstanceOf("request");
        requestTemplate.add("intygsId", intygsId);
        requestTemplate.add("mottagare", "FKASSA");

        given().body(requestTemplate.render())
            .when().post("inera-certificate/register-approved-receivers/v1.0")
            .then().statusCode(200)
            .rootPath("Envelope.Body.RegisterApprovedReceiversResponse.");

        givenRequest(intygsId)
            .body("recipient.size()", Matchers.is(1))
            .body("recipient.find { it.id == 'FKASSA' }.name", Matchers.is("Försäkringskassan"))
            .body("recipient.find { it.id == 'FKASSA' }.type", Matchers.is("HUVUDMOTTAGARE"));
    }

    @Test
    public void getRecipientsForCertificate() {
        // First, register a receiver
        String intygsId = UUID.randomUUID().toString();

        ST requestTemplate = registerApprovedTemplateGroup.getInstanceOf("request");
        requestTemplate.add("intygsId", intygsId);
        requestTemplate.add("mottagare", "FBA");

        given().body(requestTemplate.render())
            .when().post("inera-certificate/register-approved-receivers/v1.0")
            .then().statusCode(200)
            .rootPath("Envelope.Body.RegisterApprovedReceiversResponse.");

        givenRequest(intygsId)
            .body("recipient.size()", Matchers.is(2))
            .body("recipient.find { it.id == 'FKASSA' }.name", Matchers.is("Försäkringskassan"))
            .body("recipient.find { it.id == 'FKASSA' }.type", Matchers.is("HUVUDMOTTAGARE"))
            .body("recipient.find { it.id == 'FBA' }.name", Matchers.is("Försäkringsbolaget AB"))
            .body("recipient.find { it.id == 'FBA' }.type", Matchers.is("MOTTAGARE"));
    }

    private ValidatableResponse givenRequest(String intygsId) {
        ST requestTemplate = templateGroup.getInstanceOf("request");
        requestTemplate.add("intygsId", intygsId);

        return given()
            .body(requestTemplate.render())
            .when().post("inera-certificate/get-recipients-for-certificate/v1.1")
            .then().statusCode(200)
            .rootPath("Envelope.Body.GetRecipientsForCertificateResponse.");
    }

    /*
    private STGroup templateGroup;

    private static final int FK_RECIPIENT_SIZE = 1;
    private static final int TS_RECIPIENT_SIZE = 1;

    private static final String CITIZEN_CIVIC_REGISTRATION_NUMBER = "19010101-0101";

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/getrecipientsforcertificate/requests.stg");
    }

    @Test
    public void getRecipientsForCertificate() {
        givenRequest("1234567890")
                .body("result.resultCode", is("OK"))
                .body("recipient.size()", is(FK_RECIPIENT_SIZE))
                .body("recipient.id", is("FKASSA"))
                .body("recipient.name", is("Försäkringskassan"))
                .body("recipient.trusted", is("true"));
    }

    @Test
    public void getRecipientsForCertificateFk7263() {
        givenRequest("fk7263")
                .body("result.resultCode", is("OK"))
                .body("recipient.size()", is(FK_RECIPIENT_SIZE))
                .body("recipient.id", is("FKASSA"))
                .body("recipient.name", is("Försäkringskassan"))
                .body("recipient.trusted", is("true"));
    }

    @Test
    public void getRecipientsForCertificateLuse() {
        givenRequest("luse")
                .body("result.resultCode", is("OK"))
                .body("recipient.size()", is(FK_RECIPIENT_SIZE))
                .body("recipient.id", is("FKASSA"))
                .body("recipient.name", is("Försäkringskassan"))
                .body("recipient.trusted", is("true"));
    }

    @Test
    public void getRecipientsForCertificateLuaeFs() {
        givenRequest("luae_fs")
                .body("result.resultCode", is("OK"))
                .body("recipient.size()", is(FK_RECIPIENT_SIZE))
                .body("recipient.id", is("FKASSA"))
                .body("recipient.name", is("Försäkringskassan"))
                .body("recipient.trusted", is("true"));
    }

    @Test
    public void getRecipientsForCertificateTsBas() {
        givenRequest("ts-bas")
                .body("result.resultCode", is("OK"))
                .body("recipient.size()", is(TS_RECIPIENT_SIZE))
                .body("recipient.id", is("TRANSP"))
                .body("recipient.name", is("Transportstyrelsen"))
                .body("recipient.trusted", is("true"));
    }

    @Test
    public void getRecipientsForCertificateTsDiabetes() {
        givenRequest("ts-diabetes")
                .body("result.resultCode", is("OK"))
                .body("recipient.size()", is(TS_RECIPIENT_SIZE))
                .body("recipient.id", is("TRANSP"))
                .body("recipient.name", is("Transportstyrelsen"))
                .body("recipient.trusted", is("true"));
    }

    @Test
    public void getRecipientsForCertificateUnknownCertificateType() {
        givenRequest("unknown")
                .body("recipient.size()", is(0))
                .body("result.resultCode", is("ERROR"))
                .body("result.errorId", is("APPLICATION_ERROR"))
                .body("result.resultText", is("No recipients found for certificate type: unknown"));
    }
    */

}
