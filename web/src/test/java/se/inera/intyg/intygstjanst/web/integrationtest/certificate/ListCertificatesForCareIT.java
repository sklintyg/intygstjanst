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

public class ListCertificatesForCareIT extends BaseIntegrationTest {
    private ST requestTemplate;
    private String personId1 = "192703104321";
    private List<String> intygsId = Arrays.asList("luae_na_1", "luse_1", "luae_fs_1", "lisu_1", "fk7263_deletedByCareGiver");
    private static final String BASE = "Envelope.Body.ListCertificatesForCareResponse.";

    @Override
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
    }

    @Test
    public void listCertificateForCareWorks() {

        requestTemplate.add("data", new IntygsData(personId1));

        given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-care/v2.0").then().statusCode(200)
                .rootPath(BASE).body("result.resultCode", is("OK"));
    }

    @Test
    public void listCertificateNotExists() {
        requestTemplate.add("data", new IntygsData(personId1));

        Response res = given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-care/v2.0").then().statusCode(200)
                .rootPath(BASE).body("result.resultCode", is("OK")).body("intygsLista[0]", is("")).extract().response();
        System.out.println("RES: " + res.xmlPath().get("intygsLista"));
    }

    @Test
    public void listMultipleCertificates() {
        IntegrationTestUtil.registerCertificate(intygsId.get(0), personId1, IntegrationTestCertificateType.LUAENA);
        IntegrationTestUtil.registerCertificate(intygsId.get(1), personId1, IntegrationTestCertificateType.LUSE);
        IntegrationTestUtil.registerCertificate(intygsId.get(2), personId1, IntegrationTestCertificateType.LUAEFS);
        IntegrationTestUtil.registerCertificate(intygsId.get(3), personId1, IntegrationTestCertificateType.LISU);
        // deletedByCareGiver should not be returned
        IntegrationTestUtil.givenIntyg(intygsId.get(4), "fk7263", personId1, true);
        requestTemplate.add("data", new IntygsData(personId1));

        Response res = given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-care/v2.0").then().statusCode(200)
                .rootPath(BASE).body("result.resultCode", is("OK")).body("intygsLista[0].intyg.size()", is(4)).extract().response();

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
        final InputStream inputstream = ClasspathResourceResolver.load(null,
                "interactions/ListCertificatesForCareInteraction/ListCertificatesForCareResponder_2.0.xsd");

        requestTemplate.add("data", new IntygsData(personId1));

        given().filter(
                new BodyExtractorFilter(ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:ListCertificatesForCareResponder:2"),
                        "soap:Envelope/soap:Body/lc:ListCertificatesForCareResponse"))
                .body(requestTemplate.render()).when().post("inera-certificate/list-certificates-for-care/v2.0").then()
                .body(matchesXsd(IOUtils.toString(inputstream)).with(new ClasspathResourceResolver()));
    }

    @SuppressWarnings("unused")
    private static class IntygsData {
        public final String personId;

        public IntygsData(String personId) {
            this.personId = personId;
        }
    }
}
