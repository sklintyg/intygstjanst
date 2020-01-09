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

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.matcher.RestAssuredMatchers.matchesXsd;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.BodyExtractorFilter;
import se.inera.intyg.intygstjanst.web.integrationtest.ClasspathResourceResolver;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

public class SendCertificateToRecipientIT extends BaseIntegrationTest {

    private static final String REGISTER_BASE = "Envelope.Body.RegisterCertificateResponse.";
    private static final String RECIPIENT_BASE = "Envelope.Body.SendCertificateToRecipientResponse.";

    private ST requestTemplateRecipient;
    private ST requestTemplateRegister;

    private STGroup templateGroupRecipient;
    private STGroup templateGroupRegister;

    private String personId1 = "192703104321";
    private String intygsId = "123456";
    private String versionsId = "1.0";

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();

        templateGroupRecipient = new STGroupFile("integrationtests/sendcertificatetorecipient/requests.stg");
        requestTemplateRecipient = templateGroupRecipient.getInstanceOf("request");

        templateGroupRegister = new STGroupFile("integrationtests/register/request_default.stg");
        requestTemplateRegister = templateGroupRegister.getInstanceOf("request");

        IntegrationTestUtil.deleteIntyg(intygsId);
    }

    @Test
    public void sendCertificateToRecipientWorks() {

        requestTemplateRegister.add("data", new RegisterIntygsData(intygsId, versionsId, personId1));
        given().body(requestTemplateRegister.render()).when().post("inera-certificate/register-certificate-se/v3.0").then().statusCode(200)
            .rootPath(REGISTER_BASE).body("result.resultCode", is("OK"));

        requestTemplateRecipient.add("data", new SendIntygsData(intygsId, personId1));
        requestTemplateRecipient.add("mottagare", "FKASSA");

        given().body(requestTemplateRecipient.render()).when().post("inera-certificate/send-certificate-to-recipient/v2.0").then()
            .statusCode(200)
            .rootPath(RECIPIENT_BASE).body("result.resultCode", is("OK"));
    }

    @Test
    public void responseRespectsSchema() throws Exception {
        final String xsdString = Resources.toString(
            new ClassPathResource("interactions/SendCertificateToRecipientInteraction/SendCertificateToRecipientResponder_2.1.xsd")
                .getURL(), Charsets.UTF_8);

        requestTemplateRecipient.add("data", new SendIntygsData(intygsId, personId1));
        requestTemplateRecipient.add("mottagare", "FKASSA");

        given().filter(
            new BodyExtractorFilter(
                ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:SendCertificateToRecipientResponder:2"),
                "soap:Envelope/soap:Body/lc:SendCertificateToRecipientResponse"))
            .body(requestTemplateRecipient.render()).when().post("inera-certificate/send-certificate-to-recipient/v2.0").then()
            .body(matchesXsd(xsdString).with(new ClasspathResourceResolver()));
    }

    @Test
    public void sendCertificateToRecipientTS() {
        IntegrationTestUtil.givenIntyg(intygsId, "ts-bas", TS_BAS_VERSION, personId1, false);

        requestTemplateRecipient.add("data", new SendIntygsData(intygsId, personId1));
        requestTemplateRecipient.add("mottagare", "TRANSP");
        given().body(requestTemplateRecipient.render()).when().post("inera-certificate/send-certificate-to-recipient/v2.0").then()
            .statusCode(200)
            .rootPath(RECIPIENT_BASE).body("result.resultCode", is("OK"));
    }


    @Test
    public void faultTransformerTest() throws Exception {
        requestTemplateRecipient.add("data", new SendIntygsData("<tag></tag>", personId1));
        requestTemplateRecipient.add("mottagare", "FKASSA");

        given().body(requestTemplateRecipient.render()).when().post("inera-certificate/send-certificate-to-recipient/v2.0").then()
            .statusCode(200)
            .rootPath(RECIPIENT_BASE).body("result.resultCode", is("ERROR")).body("result.resultText", startsWith("Unmarshalling Error"));
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(intygsId);
    }

    @SuppressWarnings("unused")
    private class SendIntygsData {

        public final String intygsId;
        public final String personId;

        SendIntygsData(String intygsId, String personId) {
            this.intygsId = intygsId;
            this.personId = personId;
        }
    }
}
