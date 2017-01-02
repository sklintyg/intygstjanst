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
package se.inera.intyg.intygstjanst.web.integrationtest.arende;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;

import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.internal.matcher.xml.XmlXsdMatcher;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.BodyExtractorFilter;
import se.inera.intyg.intygstjanst.web.integrationtest.ClasspathResourceResolver;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

public class SendMessageToRecipientIT extends BaseIntegrationTest {

    private static final String BASE = "Envelope.Body.SendMessageToRecipientResponse.";
    private static final String INTYG_ID = "sendMessageToRecipientITcertificateId";
    private static final String PERSON_ID = "190101010101";

    private ST requestTemplate;

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();

        STGroup templateGroup = new STGroupFile("integrationtests/arende/request_recipient.stg");
        requestTemplate = templateGroup.getInstanceOf("request");
        cleanup();
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(INTYG_ID);
    }

    @Test
    public void sendMessageToRecipientOk() throws Exception {
        IntegrationTestUtil.givenIntyg(INTYG_ID, "luse", PERSON_ID, false);

        String enhetsId = "123456";
        requestTemplate.add("data", new ArendeData(INTYG_ID, "KOMPL", PERSON_ID, enhetsId));

        given().body(requestTemplate.render()).when().post("inera-certificate/send-message-to-recipient/v1.0").then().statusCode(200).rootPath(BASE)
                .body("result.resultCode", is("OK"));
    }

    @Test
    public void responseRespectsSchema() throws Exception {
        IntegrationTestUtil.givenIntyg(INTYG_ID, "luse", PERSON_ID, false);
        final InputStream inputstream = ClasspathResourceResolver.load(null,
                "interactions/SendMessageToRecipientInteraction/SendMessageToRecipientResponder_1.0.xsd");

        requestTemplate.add("data", new ArendeData(INTYG_ID, "KOMPL", PERSON_ID, "123456"));

        given().filter(
                new BodyExtractorFilter(ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:SendMessageToRecipientResponder:1"),
                        "soap:Envelope/soap:Body/lc:SendMessageToRecipientResponse"))
                .body(requestTemplate.render()).when().post("inera-certificate/send-message-to-recipient/v1.0").then()
                .body(matchesXsd(IOUtils.toString(inputstream)).with(new ClasspathResourceResolver()));
    }

    @Test
    public void messageForNonExistantCertificateIsNotAccepted() throws Exception {
        requestTemplate.add("data", new ArendeData("intyg-nonexistant", "KOMPL", PERSON_ID, "123456"));

        given().body(requestTemplate.render()).when().post("inera-certificate/send-message-to-recipient/v1.0").then().statusCode(200).rootPath(BASE)
                .body("result.resultCode", is("ERROR"));
    }

    @Test
    public void faultTransformerTest() throws Exception {
        String enhetsId = "<root>123456</root>"; // This brakes the XML Schema
        requestTemplate.add("data", new ArendeData(INTYG_ID, "KOMPL", PERSON_ID, enhetsId));

        given().body(requestTemplate.render()).when().post("inera-certificate/send-message-to-recipient/v1.0").then().statusCode(200).rootPath(BASE)
                .body("result.resultCode", is("ERROR")).body("result.resultText", startsWith("Unmarshalling Error"));
    }

    private XmlXsdMatcher matchesXsd(String xsd) {
        return XmlXsdMatcher.matchesXsd(xsd);
    }

    @SuppressWarnings("unused")
    private static class ArendeData {
        public final String intygsId;
        public final String arende;
        public final String personId;
        public final String enhetsId;
        public final String messageId = UUID.randomUUID().toString();

        public ArendeData(String intygsId, String arende, String personId, String enhetsId) {
            this.intygsId = intygsId;
            this.arende = arende;
            this.personId = personId;
            this.enhetsId = enhetsId;
        }
    }

}
