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

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseBuilder;
import com.jayway.restassured.filter.Filter;
import com.jayway.restassured.filter.FilterContext;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.FilterableRequestSpecification;
import com.jayway.restassured.specification.FilterableResponseSpecification;
import com.sun.org.apache.xerces.internal.dom.DOMInputImpl;

public class SendMessageToCareIT extends BaseIntegrationTest {

    private static final String BASE = "Envelope.Body.SendMessageToCareResponse.";

    private ST requestTemplate;

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();

        STGroup templateGroup = new STGroupFile("integrationtests/arende/requests.stg");
        requestTemplate = templateGroup.getInstanceOf("request");
    }

    @Test
    public void messageGoesToCorrectEndDestination() throws Exception {
        final InputStream inputstream = load(null, "interactions/SendMessageToCareInteraction/SendMessageToCareResponder_1.0.xsd");
        final String xsd = IOUtils.toString(inputstream);

        post("inera-certificate/send-message-to-care-stub-rest/clear");

        String enhetsId = "123456";
        String intygsId = "intyg-1";
        requestTemplate.add("data", new ArendeData(intygsId, "KOMPL", "191212121212", enhetsId));

        given().
                // filter(new BodyExtractorFilter()).
                body(requestTemplate.render()).
                when().
                post("inera-certificate/send-message-to-care/v1.0").
                then().
                statusCode(200).
                rootPath(BASE).
                body("result.resultCode", is("OK"));
        // body(matchesXsd(xsd).with(new ClasspathResourceResolver()));

        // Make sure that the final destination received the message
        given().
                param("address", enhetsId).
                when().get("inera-certificate/send-message-to-care-stub-rest/byLogicalAddress")
                .then()
                .body("messages[0].certificateId", is(intygsId));
    }

    @Test
    public void messageForNonExistantCertificateIsNotAccepted() throws Exception {
        String enhetsId = "123456";
        String intygsId = "intyg-nonexistant";
        requestTemplate.add("data", new ArendeData(intygsId, "KOMPL", "191212121212", enhetsId));

        given().
                body(requestTemplate.render()).
                when().
                post("inera-certificate/send-message-to-care/v1.0").
                then().
                statusCode(200).
                rootPath(BASE).
                body("result.resultCode", is("ERROR"));
    }

    @Test
    public void messageNotFollowingXSDIsNotAccepted() throws Exception {
        String enhetsId = "<root>123456</root>"; // This brakes the XML Schema
        String intygsId = "intyg-1";
        requestTemplate.add("data", new ArendeData(intygsId, "KOMPL", "191212121212", enhetsId));

        given().
                body(requestTemplate.render()).
                when().
                post("inera-certificate/send-message-to-care/v1.0").
                then().
                statusCode(200).
                rootPath(BASE).
                body("result.resultCode", is("ERROR"));
    }

    private static class ArendeData {
        public final String intygsId;
        public final String arende;
        public final String personId;
        public final String enhetsId;

        public ArendeData(String intygsId, String arende, String personId, String enhetsId) {
            this.intygsId = intygsId;
            this.arende = arende;
            this.personId = personId;
            this.enhetsId = enhetsId;
        }
    }

    public static class BodyExtractorFilter implements Filter {
        @Override
        public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            Response response = ctx.next(requestSpec, responseSpec);
            XPathExtractor extractor = new XPathExtractor(response.print(), ImmutableMap.of("soap", "http://schemas.xmlsoap.org/soap/envelope/",
                    "lc", "urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCareResponder:1"));
            String newBody = extractor.getFragmentFromXPath("soap:Envelope/soap:Body/lc:SendMessageToCareResponse");
            System.out.println(newBody);
            Response newResponse = new ResponseBuilder().clone(response).setBody(newBody).build();
            return newResponse;
        }
    }

    public static class ClasspathResourceResolver implements LSResourceResolver {
        @Override
        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
            return new DOMInputImpl(publicId, systemId, baseURI, load(baseURI, systemId), null);
        }
    }

    private static InputStream load(String baseURI, String name) {
        String localName = name.replaceAll("^(\\.\\./)+", "");
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(localName);
        // if (resourceAsStream == null) {
        // throw new RuntimeException("Could not find resource " + localName+ " with baseURI " + baseURI);
        // }
        return resourceAsStream;
    }

}
