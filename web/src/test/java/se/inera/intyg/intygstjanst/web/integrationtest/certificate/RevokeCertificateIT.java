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
import static org.hamcrest.CoreMatchers.is;
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

public class RevokeCertificateIT extends BaseIntegrationTest {

    private String intygsId = "123456";
    private String personId1 = "191212121212";
    private String versionsId = "1.0";

    private String intygsIdNotExists = "123456t";
    private static final String REVOKE_BASE = "Envelope.Body.RevokeCertificateResponse.";

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        IntegrationTestUtil.deleteIntyg(intygsId);
        IntegrationTestUtil.deleteIntyg(intygsIdNotExists);
    }

    @Test
    public void revokeCertificateWorks() {
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId, versionsId, personId1);
        IntegrationTestUtil.revokeCertificate(intygsId, personId1);
    }

    @Test
    public void revokeCertificateNotExists() {
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId, versionsId, personId1);
        STGroup templateGroupForRevoke = new STGroupFile("integrationtests/revokecertificate/requests.stg");
        ST requestTemplateForRevoke = templateGroupForRevoke.getInstanceOf("request");
        requestTemplateForRevoke.add("data", new RevokeIntygsData(intygsIdNotExists, personId1));
        given().body(requestTemplateForRevoke.render()).when().post("inera-certificate/revoke-certificate-rivta/v2.0").then()
            .statusCode(200)
            .rootPath(REVOKE_BASE).body("result.resultCode", is("ERROR"));
    }

    @Test
    public void responseRespectsSchema() throws Exception {
        STGroup templateGroupForRevoke = new STGroupFile("integrationtests/revokecertificate/requests.stg");
        ST requestTemplateForRevoke = templateGroupForRevoke.getInstanceOf("request");
        final String xsdString = Resources.toString(
            new ClassPathResource("interactions/RevokeCertificateInteraction/RevokeCertificateResponder_2.1.xsd").getURL(), Charsets.UTF_8);

        requestTemplateForRevoke.add("data", new RevokeIntygsData(intygsIdNotExists, personId1));

        given().filter(
            new BodyExtractorFilter(ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:RevokeCertificateResponder:2"),
                "soap:Envelope/soap:Body/lc:RevokeCertificateResponse")).body(requestTemplateForRevoke.render()).when()
            .post("inera-certificate/revoke-certificate-rivta/v2.0").then()
            .body(matchesXsd(xsdString).with(new ClasspathResourceResolver()));
    }

    @Test
    public void faultTransformerTest() {
        ST requestTemplateForRevoke = new STGroupFile("integrationtests/revokecertificate/requests.stg").getInstanceOf("request");
        requestTemplateForRevoke.add("data", new RevokeIntygsData("<tag></tag>", personId1));
        given().body(requestTemplateForRevoke.render()).when().post("inera-certificate/revoke-certificate-rivta/v2.0").then()
            .statusCode(200)
            .rootPath(REVOKE_BASE).body("result.resultCode", is("ERROR")).body("result.resultText", startsWith("Unmarshalling Error"));
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(intygsId);
    }

}
