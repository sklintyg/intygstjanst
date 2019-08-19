/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.BodyExtractorFilter;
import se.inera.intyg.intygstjanst.web.integrationtest.ClasspathResourceResolver;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

public class SetCertificateStatusIT extends BaseIntegrationTest {

    private ST requestTemplate;
    private ST requestTemplateResult;
    private String intygsId = "123456";
    private String personId = "192703104321";
    private String versionsId = "1.0";

    private String intygsIdNotExists = "123456t";
    private final String status = "RECEIV";

    private static final String BASE = "Envelope.Body.SetCertificateStatusResponse.";
    private static final String GET_BASE = "Envelope.Body.GetCertificateResponse.";

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();

        STGroup templateGroup = new STGroupFile("integrationtests/setcertificatestatus/requests.stg");
        requestTemplate = templateGroup.getInstanceOf("request");

        STGroup templateGroupResult = new STGroupFile("integrationtests/getcertificate/requests.stg");
        requestTemplateResult = templateGroupResult.getInstanceOf("request");
        IntegrationTestUtil.deleteIntyg(intygsId);
    }

    @Test
    public void setCertificateStatusWorks() {
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId, versionsId, personId);
        requestTemplate.add("data", new SetStatusIntygsData(intygsId));

        given().body(requestTemplate.render()).when().post("inera-certificate/set-certificate-status-rivta/v2.0").then().statusCode(200)
            .rootPath(BASE).body("result.resultCode", is("OK"));

        requestTemplateResult.add("data", new GetIntygsData(intygsId));

        given().body(requestTemplateResult.render()).when().post("inera-certificate/get-certificate-se/v2.0").then().statusCode(200)
            .rootPath(GET_BASE).body("intyg.status.status[0].code", is(status)).body("intyg.status.status[1].code", is(status)).extract()
            .response();
    }

    @Test
    public void setCertificateStatusIntygNotExists() {
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId, versionsId, personId);
        requestTemplate.add("data", new SetStatusIntygsData(intygsIdNotExists));

        given().body(requestTemplate.render()).when().post("inera-certificate/set-certificate-status-rivta/v2.0").then().statusCode(200)
            .rootPath(BASE).body("result.resultCode", is("ERROR"));

    }

    @Test
    public void responseRespectsSchema() throws Exception {
        final String xsdString = Resources.toString(
            new ClassPathResource("interactions/SetCertificateStatusInteraction/SetCertificateStatusResponder_2.0.xsd").getURL(),
            Charsets.UTF_8);

        requestTemplate.add("data", new SetStatusIntygsData(intygsId));

        given().filter(
            new BodyExtractorFilter(ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:SetCertificateStatusResponder:2"),
                "soap:Envelope/soap:Body/lc:SetCertificateStatusResponse"))
            .body(requestTemplate.render()).when().post("inera-certificate/set-certificate-status-rivta/v2.0").then()
            .body(matchesXsd(xsdString).with(new ClasspathResourceResolver()));
    }

    @Test
    public void faultTransformerTest() throws Exception {
        requestTemplate.add("data", new SetStatusIntygsData("<tag></tag>"));

        given().body(requestTemplate.render()).when().post("inera-certificate/set-certificate-status-rivta/v2.0").then().statusCode(200)
            .rootPath(BASE).body("result.resultCode", is("ERROR")).body("result.resultText", startsWith("Unmarshalling Error"));
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(intygsId);
    }

    @SuppressWarnings("unused")
    private class SetStatusIntygsData {

        public final String intygsId;

        SetStatusIntygsData(String intygsId) {
            this.intygsId = intygsId;
        }
    }

    @SuppressWarnings("unused")
    private class GetIntygsData {

        public final String intygsId;

        GetIntygsData(String intygsId) {
            this.intygsId = intygsId;
        }
    }
}
