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

public class RegisterCertificateIT extends BaseIntegrationTest {

    private static final String BASE = "Envelope.Body.RegisterCertificateResponse.";

    private ST requestTemplate;

    private String intygsId = "123456";

    private String personId1 = "191212121212";
    private String personId2 = "201212121212";
    private String versionsId = "1.0";

    private STGroup templateGroup;

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();

        templateGroup = new STGroupFile("integrationtests/register/request_default.stg");
        requestTemplate = templateGroup.getInstanceOf("request");

        IntegrationTestUtil.deleteIntyg(intygsId);
    }

    @Test
    public void registerCertificateWorks() {
        requestTemplate.add("data", new RegisterIntygsData(intygsId, versionsId, personId1));

        given().body(requestTemplate.render()).when().post("inera-certificate/register-certificate-se/v3.0").then().statusCode(200)
            .rootPath(BASE)
            .body("result.resultCode", is("OK"));
    }

    @Test
    public void registerCertificateUnsupportedIntygMajorVersion() {

        final String incorrectVersion = "99.3";

        requestTemplate.add("data", new RegisterIntygsData(intygsId, incorrectVersion, personId1));

        given()
            .body(requestTemplate.render())
            .when().post("inera-certificate/register-certificate-se/v3.0")
            .then().statusCode(200).rootPath(BASE).body("result.resultCode", is("ERROR"));
    }


    @Test
    public void registerCertificateSupportedIntygMajorVersionButIncorrectMinorVersion() {

        final String incorrectVersion = "1.337";
        final String errorMessage = "Certificate with type: LUSE does not support version: 1.337";

        requestTemplate.add("data", new RegisterIntygsData(intygsId, incorrectVersion, personId1));

        given()
            .body(requestTemplate.render())
            .when().post("inera-certificate/register-certificate-se/v3.0")
            .then().statusCode(200).rootPath(BASE)
            .body("result.resultCode", is("ERROR"))
            .body("result.resultText", is(errorMessage));
    }


    @Test
    public void responseRespectsSchema() throws Exception {
        final String xsdString = Resources.toString(
            new ClassPathResource("interactions/RegisterCertificateInteraction/RegisterCertificateResponder_3.1.xsd").getURL(),
            Charsets.UTF_8);

        requestTemplate.add("data", new RegisterIntygsData(intygsId, versionsId, personId1));

        given().filter(
            new BodyExtractorFilter(ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:RegisterCertificateResponder:3"),
                "soap:Envelope/soap:Body/lc:RegisterCertificateResponse")).body(requestTemplate.render()).when()
            .post("inera-certificate/register-certificate-se/v3.0").then()
            .body(matchesXsd(xsdString).with(new ClasspathResourceResolver()));
    }

    @Test
    public void registerSameCertificateTwiceReturnsInfoResult() {
        requestTemplate.add("data", new RegisterIntygsData(intygsId, versionsId, personId1));

        given().body(requestTemplate.render()).when().post("inera-certificate/register-certificate-se/v3.0").then().statusCode(200)
            .rootPath(BASE)
            .body("result.resultCode", is("OK"));

        given().body(requestTemplate.render()).when().post("inera-certificate/register-certificate-se/v3.0").then().statusCode(200)
            .rootPath(BASE)
            .body("result.resultCode", is("INFO"));
    }

    @Test
    public void registerSameCertificateForDifferentPersonsReturnsErrorResult() {
        requestTemplate.add("data", new RegisterIntygsData(intygsId, versionsId, personId1));

        given().body(requestTemplate.render()).when().post("inera-certificate/register-certificate-se/v3.0").then().statusCode(200)
            .rootPath(BASE)
            .body("result.resultCode", is("OK"));

        ST requestTemplate2 = templateGroup.getInstanceOf("request");
        requestTemplate2.add("data", new RegisterIntygsData(intygsId, versionsId, personId2));

        given().body(requestTemplate2.render()).when().post("inera-certificate/register-certificate-se/v3.0").then().statusCode(200)
            .rootPath(BASE)
            .body("result.resultCode", is("ERROR"));
    }

    @Test
    public void faultTransformerTest() {
        requestTemplate.add("data", new RegisterIntygsData("<tag></tag>", versionsId, personId1));

        given().body(requestTemplate.render()).when().post("inera-certificate/register-certificate-se/v3.0").then().statusCode(200)
            .rootPath(BASE)
            .body("result.resultCode", is("ERROR")).body("result.resultText", startsWith("Unmarshalling Error"));
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(intygsId);
    }

}
