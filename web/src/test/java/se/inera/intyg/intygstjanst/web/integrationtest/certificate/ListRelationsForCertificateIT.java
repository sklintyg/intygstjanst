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

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class ListRelationsForCertificateIT extends BaseIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListRelationsForCertificateIT.class);

    private static final String LIST_RELATIONS_FOR_CERTIFICATE_URL = "inera-certificate/list-relations-for-certificate/v1.0";
    private ST requestTemplate;
    private STGroup templateGroup;

    @Before
    public void setup() throws IOException {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();

        // Setup String template resource
        templateGroup = new STGroupFile("integrationtests/listrelationsforcertificate/requests.stg");
        requestTemplate = templateGroup.getInstanceOf("request");
    }

    @Test
    public void requestWorks() {
        requestTemplate.add("data", new ListRelationsForCertificateRequestParameters("123456"));
        given().body(requestTemplate.render()).when().post(LIST_RELATIONS_FOR_CERTIFICATE_URL).then().statusCode(200);
    }

    @Test
    public void responseRespectsSchema() throws Exception {
        final String xsdString = Resources.toString(
            new ClassPathResource("interactions/ListRelationsForCertificateInteraction/ListRelationsForCertificateResponder_1.0.xsd")
                .getURL(),
            Charsets.UTF_8);

        requestTemplate.add("data", new ListRelationsForCertificateRequestParameters("123456"));

        given().filter(
            new BodyExtractorFilter(
                ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:ListRelationsForCertificateResponder:1"),
                "/soap:Envelope/soap:Body/*[local-name() = 'ListRelationsForCertificateResponse']"))
            .body(requestTemplate.render()).when().post(LIST_RELATIONS_FOR_CERTIFICATE_URL).then()
            .body(matchesXsd(xsdString).with(new ClasspathResourceResolver()));
    }

    @Test
    public void testMessageWithInvalidXMLFails() {
        ST brokenTemplate = templateGroup.getInstanceOf("brokenrequest");
        given().body(brokenTemplate.render())
            .when()
            .post(LIST_RELATIONS_FOR_CERTIFICATE_URL)
            .then()
            .statusCode(500);
    }

    private static class ListRelationsForCertificateRequestParameters {

        public final String intygsId;

        public ListRelationsForCertificateRequestParameters(String intygsId) {
            this.intygsId = intygsId;
        }
    }
}
