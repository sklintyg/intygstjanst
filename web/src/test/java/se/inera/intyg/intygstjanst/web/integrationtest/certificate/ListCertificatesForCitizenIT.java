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
package se.inera.intyg.intygstjanst.web.integrationtest.certificate;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.matcher.RestAssuredMatchers.matchesXsd;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.stringtemplate.v4.*;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.response.Response;

import se.inera.intyg.intygstjanst.web.integrationtest.*;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil.IntegrationTestCertificateType;

public class ListCertificatesForCitizenIT extends BaseIntegrationTest {
    private static final String BASE = "Envelope.Body.ListCertificatesForCitizenResponse.";
    private static final String defaultType = "luse";

    private String intygsId = "123456";

    private String personId = "192703104322";

    private List<String> intygsId_alltypes = Arrays.asList("luae_na_1", "luse_1", "luae_fs_1", "lisjp_1");

    @Before
    public void setup() {
        cleanup();
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
    }

    private ST getRequestTemplate(boolean all) {
        STGroup templateGroup;
        if (all) {
            templateGroup = new STGroupFile("integrationtests/listcertificatesforcitizen/request_alltypes.stg");
        } else {
            templateGroup = new STGroupFile("integrationtests/listcertificatesforcitizen/requests.stg");
        }
        return templateGroup.getInstanceOf("request");
    }

    @Test
    public void listCertificatesForCitizenWithNoConsent() {
        ST requestTemplate = getRequestTemplate(false);
        requestTemplate.add("data", new ListParameters(personId, defaultType));

        given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-citizen/v2.0").then().statusCode(200)
                .rootPath(BASE).body("result.resultCode", is("INFO"))
                .body("result.resultText", is("NOCONSENT"));
    }

    @Test
    public void listCertificateNotExists() {
        ST requestTemplate = getRequestTemplate(true);
        IntegrationTestUtil.addConsent(personId);
        requestTemplate.add("data", new ListParameters(personId, defaultType));

        given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-citizen/v2.0").then()
                .statusCode(200)
                .rootPath(BASE).body("result.resultCode", is("OK")).body("intygsLista[0]", is(""));
    }

    @Test
    public void listMultipleCertificatesShowAll() {
        ST requestTemplate = getRequestTemplate(true);
        IntegrationTestUtil.addConsent(personId);
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId_alltypes.get(0), personId, IntegrationTestCertificateType.LUAENA);
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId_alltypes.get(1), personId, IntegrationTestCertificateType.LUSE);
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId_alltypes.get(2), personId, IntegrationTestCertificateType.LUAEFS);
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId_alltypes.get(3), personId, IntegrationTestCertificateType.LISJP);
        requestTemplate.add("data", new ListParameters(personId, defaultType));

        Response res = given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-citizen/v2.0").then()
                .statusCode(200)
                .rootPath(BASE).body("result.resultCode", is("OK")).body("intygsLista[0].intyg.size()", is(4)).extract().response();

        assertTrue(intygsId_alltypes.containsAll(extractIds(res)));
    }

    @Test
    public void listCertificatesRevokeConsent() {
        ST requestTemplate = getRequestTemplate(true);
        IntegrationTestUtil.addConsent(personId);
        IntegrationTestUtil.givenIntyg(intygsId_alltypes.get(0), "fk7263", personId, true);
        requestTemplate.add("data", new ListParameters(personId, defaultType));

        given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-citizen/v2.0").then().statusCode(200)
                .rootPath(BASE).body("result.resultCode", is("OK")).body("intygsLista[0].intyg.size()", is(1));

        // Eftersom intyget är markerat som borttaget av vården så ska detta städas bort då inte heller invånaren längre
        // har åtkomst till det. När invånaren åter ger samtycke så är intyget borta.
        IntegrationTestUtil.revokeConsent(personId);
        given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-citizen/v2.0").then().statusCode(200)
                .rootPath(BASE).body("result.resultCode", is("INFO"))
                .body("result.resultText", is("NOCONSENT"))
                .body("intygsLista[0].intyg.size()", is(0));
        IntegrationTestUtil.addConsent(personId);
        given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-citizen/v2.0").then().statusCode(200)
                .rootPath(BASE).body("result.resultCode", is("OK"))
                .body("intygsLista[0].intyg.size()", is(0));
    }

    @Test
    public void listMultipleCertificatesShowOnlyOneType() {
        ST requestTemplate = getRequestTemplate(false);
        IntegrationTestUtil.addConsent(personId);
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId_alltypes.get(0), personId, IntegrationTestCertificateType.LUAENA);
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId_alltypes.get(1), personId, IntegrationTestCertificateType.LUSE);
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId_alltypes.get(2), personId, IntegrationTestCertificateType.LUAEFS);
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId_alltypes.get(3), personId, IntegrationTestCertificateType.LISJP);
        requestTemplate.add("data", new ListParameters(personId, defaultType));

        Response res = given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-citizen/v2.0").then()
                .statusCode(200)
                .rootPath(BASE).body("result.resultCode", is("OK")).body("intygsLista[0].intyg.size()", is(1)).extract().response();

        assertTrue(intygsId_alltypes.containsAll(extractIds(res)));
    }

    @Test
    public void listCertificatesForCitizenWorks() {
        ST requestTemplate = getRequestTemplate(false);
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId, personId);
        IntegrationTestUtil.addConsent(personId);

        requestTemplate.add("data", new ListParameters(personId, defaultType));

        given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-citizen/v2.0").then().statusCode(200)
                .rootPath(BASE).body("result.resultCode", is("OK"));
    }

    @Test
    public void responseRespectsSchema() throws Exception {
        ST requestTemplate = getRequestTemplate(false);
        final InputStream inputstream = ClasspathResourceResolver.load(null,
                "interactions/ListCertificatesForCitizenInteraction/ListCertificatesForCitizenResponder_2.0.xsd");

        requestTemplate.add("data", new ListParameters(personId, defaultType));

        given().filter(
                new BodyExtractorFilter(ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:ListCertificatesForCitizenResponder:2"),
                        "soap:Envelope/soap:Body/lc:ListCertificatesForCitizenResponse"))
                .body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-citizen/v2.0").then()
                .body(matchesXsd(IOUtils.toString(inputstream)).with(new ClasspathResourceResolver()));
    }

    @Test
    public void faultTransformerTest() {
        ST requestTemplate = getRequestTemplate(false);
        requestTemplate.add("data", new ListParameters("<tag></tag>", defaultType));

        given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-citizen/v2.0").then().statusCode(200)
                .rootPath(BASE).body("result.resultCode", is("ERROR")).body("result.resultText", startsWith("Unmarshalling Error"))
                .body("intygsLista.intyg.size()", is(0));
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(intygsId);
        for (String intyg : intygsId_alltypes) {
            IntegrationTestUtil.deleteIntyg(intyg);
        }
        IntegrationTestUtil.revokeConsent(personId);
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
