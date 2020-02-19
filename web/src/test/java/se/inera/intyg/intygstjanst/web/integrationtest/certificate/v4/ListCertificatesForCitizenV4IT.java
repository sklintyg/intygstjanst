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
package se.inera.intyg.intygstjanst.web.integrationtest.certificate.v4;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.matcher.RestAssuredMatchers.matchesXsd;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.jayway.restassured.response.Response;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.BodyExtractorFilter;
import se.inera.intyg.intygstjanst.web.integrationtest.ClasspathResourceResolver;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil.IntegrationTestCertificateType;

public class ListCertificatesForCitizenV4IT extends BaseIntegrationTest {

    private static final String BASE = "Envelope.Body.ListCertificatesForCitizenResponse.";
    private static final String defaultType = "luse";
    private static final String LUAE_NA_VERSION = "1.0";

    private String intygsId = "123456";

    private String personId = "196301022866";

    private List<String> intygsId_alltypes = Arrays.asList("luae_na_1", "luse_1", "luae_fs_1", "lisjp_1");

    @Before
    public void setup() {
        cleanup();
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
    }

    private ST getRequestTemplate(boolean all) {
        STGroup templateGroup;
        if (all) {
            templateGroup = new STGroupFile("integrationtests/listcertificatesforcitizen/v4/request_alltypes.stg");
        } else {
            templateGroup = new STGroupFile("integrationtests/listcertificatesforcitizen/v4/requests.stg");
        }
        return templateGroup.getInstanceOf("request");
    }

    @Test
    public void listCertificateNotExists() {
        ST requestTemplate = getRequestTemplate(true);
        requestTemplate.add("data", new ListParameters(personId, defaultType));

        given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-citizen/v4.0").then()
            .statusCode(200)
            .rootPath(BASE).body("intygsLista[0]", is(""));
    }

    @Test
    public void listMultipleCertificatesShowAll() {
        ST requestTemplate = getRequestTemplate(true);
        IntegrationTestUtil
            .registerCertificateFromTemplate(intygsId_alltypes.get(0), LUAE_NA_VERSION, personId, IntegrationTestCertificateType.LUAENA);
        IntegrationTestUtil
            .registerCertificateFromTemplate(intygsId_alltypes.get(1), LUAE_NA_VERSION, personId, IntegrationTestCertificateType.LUSE);
        IntegrationTestUtil
            .registerCertificateFromTemplate(intygsId_alltypes.get(2), LUAE_NA_VERSION, personId, IntegrationTestCertificateType.LUAEFS);
        IntegrationTestUtil
            .registerCertificateFromTemplate(intygsId_alltypes.get(3), LUAE_NA_VERSION, personId, IntegrationTestCertificateType.LISJP);
        requestTemplate.add("data", new ListParameters(personId, defaultType));

        Response res = given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-citizen/v4.0").then()
            .statusCode(200)
            .rootPath(BASE).body("intygsLista[0].intyg.size()", is(4)).extract().response();

        assertTrue(intygsId_alltypes.containsAll(extractIds(res)));
    }

    @Test
    public void listMultipleCertificatesShowOnlyOneType() {
        ST requestTemplate = getRequestTemplate(false);
        IntegrationTestUtil
            .registerCertificateFromTemplate(intygsId_alltypes.get(0), LUAE_NA_VERSION, personId, IntegrationTestCertificateType.LUAENA);
        IntegrationTestUtil
            .registerCertificateFromTemplate(intygsId_alltypes.get(1), LUAE_NA_VERSION, personId, IntegrationTestCertificateType.LUSE);
        IntegrationTestUtil
            .registerCertificateFromTemplate(intygsId_alltypes.get(2), LUAE_NA_VERSION, personId, IntegrationTestCertificateType.LUAEFS);
        IntegrationTestUtil
            .registerCertificateFromTemplate(intygsId_alltypes.get(3), LUAE_NA_VERSION, personId, IntegrationTestCertificateType.LISJP);
        requestTemplate.add("data", new ListParameters(personId, defaultType));

        Response res = given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-citizen/v4.0").then()
            .statusCode(200)
            .rootPath(BASE).body("intygsLista[0].intyg.size()", is(1)).extract().response();

        assertTrue(intygsId_alltypes.containsAll(extractIds(res)));
    }

    @Test
    public void listCertificatesForCitizenWorks() {
        ST requestTemplate = getRequestTemplate(false);
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId, LUAE_NA_VERSION, personId);

        requestTemplate.add("data", new ListParameters(personId, defaultType));

        given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-citizen/v4.0").then().statusCode(200);
    }

    @Test
    public void responseRespectsSchema() throws Exception {
        ST requestTemplate = getRequestTemplate(false);
        final String xsdString = Resources.toString(
            new ClassPathResource("interactions/ListCertificatesForCitizenInteraction/ListCertificatesForCitizenResponder_4.0.xsd")
                .getURL(),
            Charsets.UTF_8);

        requestTemplate.add("data", new ListParameters(personId, defaultType));

        given().filter(
            new BodyExtractorFilter(
                ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:ListCertificatesForCitizenResponder:4"),
                "soap:Envelope/soap:Body/lc:ListCertificatesForCitizenResponse"))
            .body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-citizen/v4.0").then()
            .body(matchesXsd(xsdString).with(new ClasspathResourceResolver()));
    }

    @Test
    public void faultTransformerTest() {
        ST requestTemplate = getRequestTemplate(false);
        requestTemplate.add("data", new ListParameters("<tag></tag>", defaultType));

        given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-citizen/v4.0").then().statusCode(200)
            .rootPath(BASE)
            .body("intygsLista.intyg.size()", is(0));
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(intygsId);
        for (String intyg : intygsId_alltypes) {
            IntegrationTestUtil.deleteIntyg(intyg);
        }
        IntegrationTestUtil.deleteCertificatesForCitizen(personId);
    }

    private List<String> extractIds(Response res) {
        List<String> result = new ArrayList<>();
        Integer size = res.xmlPath().get("Envelope.Body.ListCertificatesForCitizenResponse.intygsLista[0].intyg.size()");
        for (int i = 0; i < size; i++) {
            String path = "Envelope.Body.ListCertificatesForCitizenResponse.intygsLista[0].intyg[" + i + "].intygs-id.extension";
            result.add(res.xmlPath().get(path));
        }
        return result;
    }

    @SuppressWarnings("unused")
    private static class ListParameters {

        public final String personId;
        public final String type;

        public ListParameters(String personId, String type) {
            this.personId = personId;
            this.type = type;
        }
    }
}
