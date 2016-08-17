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
import static com.jayway.restassured.matcher.RestAssuredMatchers.matchesXsd;
import static org.hamcrest.core.Is.is;

import java.io.InputStream;

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

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.BodyExtractorFilter;
import se.inera.intyg.intygstjanst.web.integrationtest.ClasspathResourceResolver;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

public class RegisterCertificateIT extends BaseIntegrationTest {

    private static final String BASE = "Envelope.Body.RegisterCertificateResponse.";

    private ST requestTemplate;

    private String intygsId = "123456";

    private String personId1 = "192703104321";
    private String personId2 = "195206172339";

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
        requestTemplate.add("data", new IntygsData(intygsId, personId1));

        given().body(requestTemplate.render()).
                when().
                post("inera-certificate/register-certificate-se/v2.0").
                then().
                statusCode(200).
                rootPath(BASE).
                body("result.resultCode", is("OK"));
    }

    @Test
    public void responseRespectsSchema() throws Exception {
        final InputStream inputstream = ClasspathResourceResolver.load(null,
                "interactions/RegisterCertificateInteraction/RegisterCertificateResponder_2.0.xsd");

        requestTemplate.add("data", new IntygsData(intygsId, personId1));

        given().
                filter(new BodyExtractorFilter(ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:RegisterCertificateResponder:2"),
                        "soap:Envelope/soap:Body/lc:RegisterCertificateResponse")).
                body(requestTemplate.render()).
                when().
                post("inera-certificate/register-certificate-se/v2.0").
                then().
                body(matchesXsd(IOUtils.toString(inputstream)).with(new ClasspathResourceResolver()));
    }

    @Test
    public void registerSameCertificateTwiceReturnsInfoResult() {
        requestTemplate.add("data", new IntygsData(intygsId, personId1));

        given().body(requestTemplate.render()).
                when().
                post("inera-certificate/register-certificate-se/v2.0").
                then().
                statusCode(200).
                rootPath(BASE).
                body("result.resultCode", is("OK"));

        given().body(requestTemplate.render()).
                when().
                post("inera-certificate/register-certificate-se/v2.0").
                then().
                statusCode(200).
                rootPath(BASE).
                body("result.resultCode", is("INFO"));
    }

    @Test
    public void registerSameCertificateForDifferentPersonsReturnsErrorResult() {
        requestTemplate.add("data", new IntygsData(intygsId, personId1));

        given().body(requestTemplate.render()).
                when().
                post("inera-certificate/register-certificate-se/v2.0").
                then().
                statusCode(200).
                rootPath(BASE).
                body("result.resultCode", is("OK"));

        ST requestTemplate2 = templateGroup.getInstanceOf("request");
        requestTemplate2.add("data", new IntygsData(intygsId, personId2));

        given().body(requestTemplate2.render()).
                when().
                post("inera-certificate/register-certificate-se/v2.0").
                then().
                statusCode(200).
                rootPath(BASE).
                body("result.resultCode", is("ERROR"));
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(intygsId);
    }

    @SuppressWarnings("unused")
    private static class IntygsData {
        public final String intygsId;

        public final String personId;

        public IntygsData(String intygsId, String personId) {
            this.intygsId = intygsId;
            this.personId = personId;
        }
    }

}
