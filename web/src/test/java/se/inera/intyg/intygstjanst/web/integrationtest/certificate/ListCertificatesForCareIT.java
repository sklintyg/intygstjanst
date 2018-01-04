/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

public class ListCertificatesForCareIT extends BaseIntegrationTest {
    private static final String BASE = "Envelope.Body.ListCertificatesForCareResponse.";
    private static final String CARE_UNIT_ID = "SE2321000016-H489";
    private ST requestTemplate;
    private String personId1 = "192703104321";
    private List<String> intygsId = Arrays.asList("luae_na_1", "luse_1", "luae_fs_1", "lisjp_1", "fk7263_deletedByCareGiver");

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();

        STGroup templateGroup = new STGroupFile("integrationtests/listcertificatesforcare/requests.stg");
        requestTemplate = templateGroup.getInstanceOf("request");
        deleteIntyg();
    }

    @After
    public void deleteIntyg() {
        intygsId.stream().forEach(id -> IntegrationTestUtil.deleteIntyg(id));
        IntegrationTestUtil.deleteCertificatesForUnit(CARE_UNIT_ID);
    }

    @Test
    public void listCertificateForCareWorks() {
        requestTemplate.add("data", new IntygsData(personId1));

        given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-care/v3.0").then().statusCode(200);
    }

    @Test
    public void listCertificateNotExists() {
        requestTemplate.add("data", new IntygsData(personId1));

        given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-care/v3.0").then().statusCode(200)
                .rootPath(BASE)
                .body("intygsLista[0]", is("")).extract().response();
    }

    @Test
    public void listMultipleCertificates() {
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId.get(0), personId1, IntegrationTestCertificateType.LUAENA);
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId.get(1), personId1, IntegrationTestCertificateType.LUSE);
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId.get(2), personId1, IntegrationTestCertificateType.LUAEFS);
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId.get(3), personId1, IntegrationTestCertificateType.LISJP);
        // deletedByCareGiver should not be returned
        IntegrationTestUtil.givenIntyg(intygsId.get(4), "fk7263", personId1, true);
        requestTemplate.add("data", new IntygsData(personId1));

        Response res = given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-care/v3.0").then()
                .statusCode(200)
                .rootPath(BASE)
                .body("intygsLista[0].intyg.size()", is(4)).extract().response();

        assertTrue(intygsId.containsAll(extractIds(res)));
    }

    private List<String> extractIds(Response res) {
        List<String> result = new ArrayList<>();
        Integer size = res.xmlPath().get("Envelope.Body.ListCertificatesForCareResponse.intygsLista[0].intyg.size()");
        for (int i = 0; i < size; i++) {
            String path = "Envelope.Body.ListCertificatesForCareResponse.intygsLista[0].intyg[" + i + "].intygs-id.extension";
            result.add(res.xmlPath().get(path));
        }
        return result;
    }

    @Test
    public void responseRespectsSchema() throws Exception {
        final String xsdString = Resources.toString(
                new ClassPathResource("interactions/ListCertificatesForCareInteraction/ListCertificatesForCareResponder_3.0.xsd").getURL(),
                Charsets.UTF_8);

        requestTemplate.add("data", new IntygsData(personId1));

        given().filter(
                new BodyExtractorFilter(
                        ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:ListCertificatesForCareResponder:3"),
                        "soap:Envelope/soap:Body/lc:ListCertificatesForCareResponse"))
                .body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-care/v3.0").then()
                .body(matchesXsd(xsdString).with(new ClasspathResourceResolver()));
    }

    @Test
    public void faultTransformerTest() {
        requestTemplate.add("data", new IntygsData("<tag></tag>"));

        given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-care/v3.0").then().statusCode(500)
                .rootPath("Envelope.Body.Fault").body("faultcode", is("soap:Client"))
                .body("faultstring", startsWith("Unmarshalling Error"));
    }

    @SuppressWarnings("unused")
    private static class IntygsData {
        public final String personId;

        public IntygsData(String personId) {
            this.personId = personId;
        }
    }
}
