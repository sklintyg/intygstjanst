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
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;

import org.junit.*;
import org.stringtemplate.v4.*;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

public class GetCertificateIT extends BaseIntegrationTest {

    private static final String BASE = "Envelope.Body.GetCertificateResponse.";
    private static final String INTYG_ID = "getCertificateITcertificateId";
    private static final String PERSON_ID = "190101010101";

    private ST requestTemplate;

    private STGroup templateGroup;

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/getcertificate/requests.stg");
        requestTemplate = templateGroup.getInstanceOf("request");
        cleanup();
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(INTYG_ID);
    }

    @Test
    public void getCertificateWorks() {
        IntegrationTestUtil.givenIntyg(INTYG_ID, "luse", LUSE_VERSION, PERSON_ID, false);
        requestTemplate.add("data", new IntygsData(INTYG_ID, PERSON_ID));

        given().body(requestTemplate.render()).when().post("inera-certificate/get-certificate-se/v2.0").then().statusCode(200).rootPath(BASE)
                .body("intyg.intygs-id.extension", is(INTYG_ID));
    }

    @Test
    public void getCertificateDoesNotExist() {
        requestTemplate.add("data", new IntygsData("fit-intyg-finnsinte", PERSON_ID));

        given().body(requestTemplate.render()).when().post("inera-certificate/get-certificate-se/v2.0").then().statusCode(500)
                .rootPath("Envelope.Body.Fault").body("faultcode", is("soap:Server"))
                .body("faultstring", is("Certificate with id fit-intyg-finnsinte is invalid or does not exist"));
    }

    @Test
    public void faultTransformerTest() {
        requestTemplate.add("data", new IntygsData("<root></root>", PERSON_ID)); // This brakes the XML Schema

        // GetCertificate does not have a fault transformer, SoapFault is expected
        given().body(requestTemplate.render()).when().post("inera-certificate/get-certificate-se/v2.0").then().statusCode(500)
                .rootPath("Envelope.Body.Fault").body("faultcode", is("soap:Client")).body("faultstring", startsWith("Unmarshalling Error"));
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
