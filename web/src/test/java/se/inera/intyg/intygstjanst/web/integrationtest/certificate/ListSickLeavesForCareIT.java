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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.BodyExtractorFilter;
import se.inera.intyg.intygstjanst.web.integrationtest.ClasspathResourceResolver;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.matcher.RestAssuredMatchers.matchesXsd;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;

public class ListSickLeavesForCareIT extends BaseIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListSickLeavesForCareIT.class);

    private ST requestTemplate;
    private static final String BASE = "Envelope.Body.ListSickLeavesForCareResponse.";
    private static final String CARE_UNIT_ID = "centrum-vast";
    
    private Params defaultParams;

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();

        STGroup templateGroup = new STGroupFile("integrationtests/listsickleavesforcare/requests.stg");
        requestTemplate = templateGroup.getInstanceOf("request");

        defaultParams = new Params(CARE_UNIT_ID, 0, Integer.MAX_VALUE, 5, null);
    }

    @Test
    public void listCertificateForCareWorks() {
        requestTemplate.add("data", defaultParams);

        given().body(requestTemplate.render()).when().post("inera-certificate/list-sickleaves-for-care/v1.0").then().statusCode(200)
                .rootPath(BASE).body("result.resultCode", is("OK"));

        
    }

   

    @Test
    public void responseRespectsSchema() throws Exception {
        final String xsdString = Resources.toString(
                new ClassPathResource("interactions/ListSickLeavesForCareInteraction/ListSickLeavesForCareResponder_1.0.xsd").getURL(), Charsets.UTF_8);
        LOGGER.error(xsdString);
        requestTemplate.add("data", defaultParams);

        given().filter(
                new BodyExtractorFilter(ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:ListSickLeavesForCareResponder:1"),
                        "soap:Envelope/soap:Body/lc:ListSickLeavesForCareResponse"))
                .body(requestTemplate.render()).when().post("inera-certificate/list-sickleaves-for-care/v1.0").then()
                .body(matchesXsd(xsdString).with(new ClasspathResourceResolver()));
    }

    @Test
    public void faultTransformerTest() {
        requestTemplate.add("data", new Params("<tag></tag>", 0, 100, 5, "<another-tag></another-tag"));

        given().body(requestTemplate.render()).when().post("inera-certificate/list-sickleaves-for-care/v1.0").then().statusCode(200).rootPath(BASE)
                .body("result.resultCode", is("ERROR")).body("result.resultText", startsWith("Unmarshalling Error"))
                .body("sjukfallLista.sjukfall.size()", is(0));
    }

    private static class Params {
        public final String enhetsId;
        public final int minstaSjukskrivningslangd;
        public final int maxSjukskrivningslangd;
        public final int maxDagarMellanIntyg;
        public final String personalId;

        public Params(String enhetsId, int minstaSjukskrivningslangd, int maxSjukskrivningslangd, int maxDagarMellanIntyg, String personalId) {
            this.enhetsId = enhetsId;
            this.minstaSjukskrivningslangd = minstaSjukskrivningslangd;
            this.maxSjukskrivningslangd = maxSjukskrivningslangd;
            this.maxDagarMellanIntyg = maxDagarMellanIntyg;
            this.personalId = personalId;
        }
    }
}