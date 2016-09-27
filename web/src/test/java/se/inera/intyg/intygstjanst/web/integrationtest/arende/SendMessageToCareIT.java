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
package se.inera.intyg.intygstjanst.web.integrationtest.arende;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.post;
import static com.jayway.restassured.matcher.RestAssuredMatchers.matchesXsd;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;

import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.*;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.http.ContentType;

import se.inera.intyg.intygstjanst.web.integrationtest.*;

public class SendMessageToCareIT extends BaseIntegrationTest {

    private static final String BASE = "Envelope.Body.SendMessageToCareResponse.";

    private ST requestTemplate;

    @Override
    @Before
    public void setup() {
        super.setup();
        STGroup templateGroup = new STGroupFile("integrationtests/arende/request_care.stg");
        requestTemplate = templateGroup.getInstanceOf("request");
    }

    @Test
    public void messageGoesToCorrectEndDestination() throws Exception {
        post("inera-certificate/send-message-to-care-stub-rest/clear");

        String enhetsId = "123456";
        String intygsId = "intyg-1";
        requestTemplate.add("data", new ArendeData(intygsId, "KOMPL", "191212121212", enhetsId));

        given().contentType(ContentType.XML).body(requestTemplate.render()).when().post("inera-certificate/send-message-to-care/v1.0").then().statusCode(200).rootPath(BASE)
                .body("result.resultCode", is("OK"));

        // Make sure that the final destination received the message
        given().contentType(ContentType.XML).param("address", enhetsId).when().get("inera-certificate/send-message-to-care-stub-rest/byLogicalAddress")
                .then()
                .body("messages[0].certificateId", is(intygsId));
    }

    @Test
    public void responseRespectsSchema() throws Exception {
        final InputStream inputstream = ClasspathResourceResolver.load(null,
                "interactions/SendMessageToCareInteraction/SendMessageToCareResponder_1.0.xsd");

        String enhetsId = "123456";
        String intygsId = "intyg-1";

        requestTemplate.add("data", new ArendeData(intygsId, "KOMPL", "191212121212", enhetsId));

        given().contentType(ContentType.XML).filter(new BodyExtractorFilter(ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCareResponder:1"),
                "soap:Envelope/soap:Body/lc:SendMessageToCareResponse")).body(requestTemplate.render()).when()
                .post("inera-certificate/send-message-to-care/v1.0").then()
                .body(matchesXsd(IOUtils.toString(inputstream)).with(new ClasspathResourceResolver()));
    }

    @Test
    public void messageForNonExistantCertificateIsNotAccepted() throws Exception {
        String enhetsId = "123456";
        String intygsId = "intyg-nonexistant";
        requestTemplate.add("data", new ArendeData(intygsId, "KOMPL", "191212121212", enhetsId));

        given().contentType(ContentType.XML).body(requestTemplate.render()).when().post("inera-certificate/send-message-to-care/v1.0").then().statusCode(200).rootPath(BASE)
                .body("result.resultCode", is("ERROR"));
    }

    @Test
    public void faultTransformerTest() throws Exception {
        String enhetsId = "<root>123456</root>"; // This brakes the XML Schema
        requestTemplate.add("data", new ArendeData("intyg-1", "KOMPL", "191212121212", enhetsId));

        given().contentType(ContentType.XML).body(requestTemplate.render()).when().post("inera-certificate/send-message-to-care/v1.0").then().statusCode(200).rootPath(BASE)
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
