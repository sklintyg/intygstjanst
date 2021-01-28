/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import static io.restassured.RestAssured.given;
import static io.restassured.matcher.RestAssuredMatchers.matchesXsd;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;

import java.util.UUID;

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
import io.restassured.http.ContentType;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.BodyExtractorFilter;
import se.inera.intyg.intygstjanst.web.integrationtest.ClasspathResourceResolver;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

public class SendMessageToCareIT extends BaseIntegrationTest {

    private static final String BASE = "Envelope.Body.SendMessageToCareResponse.";
    private static final String INTYG_ID = "sendMessageToCareITcertificateId";
    private static final String INTYG_ID_NON_EXISTANT = "intyg-nonexistant";
    private static final String PERSON_ID = "190101010101";


    private ST requestTemplate;

    @Before
    public void setup() {
        STGroup templateGroup = new STGroupFile("integrationtests/arende/request_care.stg");
        requestTemplate = templateGroup.getInstanceOf("request");
        cleanup();
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(INTYG_ID);
        IntegrationTestUtil.deleteIntyg(INTYG_ID_NON_EXISTANT);
    }

    @Test
    public void sendMessageToCareOk() throws Exception {
        IntegrationTestUtil.givenIntyg(INTYG_ID, "luse", LUSE_VERSION, PERSON_ID, false);
        String enhetsId = "123456";

        requestTemplate.add("data", new ArendeData(INTYG_ID, "KOMPL", PERSON_ID, enhetsId));

        given().contentType(ContentType.XML).body(requestTemplate.render()).when().post("inera-certificate/send-message-to-care/v2.0")
            .then().statusCode(200).rootPath(BASE)
            .body("result.resultCode", is("OK"));
    }

    @Test
    public void responseRespectsSchema() throws Exception {
        IntegrationTestUtil.givenIntyg(INTYG_ID, "luse", LUSE_VERSION, PERSON_ID, false);
        final String xsdString = Resources.toString(
            new ClassPathResource("interactions/SendMessageToCareInteraction/SendMessageToCareResponder_2.0.xsd").getURL(), Charsets.UTF_8);

        requestTemplate.add("data", new ArendeData(INTYG_ID, "KOMPL", PERSON_ID, "123456"));

        given().contentType(ContentType.XML).filter(
            new BodyExtractorFilter(ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCareResponder:2"),
                "soap:Envelope/soap:Body/lc:SendMessageToCareResponse")).body(requestTemplate.render()).when()
            .post("inera-certificate/send-message-to-care/v2.0").then()
            .body(matchesXsd(xsdString).with(new ClasspathResourceResolver()));
    }

    @Test
    public void messageForNonExistantCertificateIsNotAccepted() throws Exception {
        requestTemplate.add("data", new ArendeData(INTYG_ID_NON_EXISTANT, "KOMPL", PERSON_ID, "123456"));

        given().contentType(ContentType.XML).body(requestTemplate.render()).when().post("inera-certificate/send-message-to-care/v2.0")
            .then().statusCode(200).rootPath(BASE)
            .body("result.resultCode", is("ERROR"));
    }

    @Test
    public void faultTransformerTest() throws Exception {
        String enhetsId = "<root>123456</root>"; // This brakes the XML Schema
        requestTemplate.add("data", new ArendeData(INTYG_ID, "KOMPL", PERSON_ID, enhetsId));

        given().contentType(ContentType.XML).body(requestTemplate.render()).when().post("inera-certificate/send-message-to-care/v2.0")
            .then().statusCode(200).rootPath(BASE)
            .body("result.resultCode", is("ERROR")).body("result.resultText", startsWith("Unmarshalling Error"));
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
