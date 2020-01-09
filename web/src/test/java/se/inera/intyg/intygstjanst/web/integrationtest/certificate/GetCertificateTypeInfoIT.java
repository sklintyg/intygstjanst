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
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

public class GetCertificateTypeInfoIT extends BaseIntegrationTest {

    private static final String BASE = "Envelope.Body.GetCertificateTypeInfoResponse.";
    private static final String INTYG_ID = "getCertificateITcertificateId";
    private static final String PERSON_ID = "190101010101";

    private ST requestTemplate;

    private STGroup templateGroup;

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/getcertificatetypeinfo/requests.stg");
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
        requestTemplate.add("data", new IntygsData(INTYG_ID));

        given().body(requestTemplate.render()).when().post("inera-certificate/get-certificate-type-info/v1.0").then().statusCode(200)
            .rootPath(BASE)
            .body("typ.code", is("luse"))
            .body("typVersion", is(LUSE_VERSION));
    }

    @Test
    public void getCertificateDoesNotExist() {
        requestTemplate.add("data", new IntygsData("fit-intyg-finnsinte"));
        String expected = "Failed to get certificate's type. Certificate with id fit-intyg-finnsinte is invalid or does not exist";

        given().body(requestTemplate.render()).when().post("inera-certificate/get-certificate-type-info/v1.0").then().statusCode(500)
            .rootPath("Envelope.Body.Fault").body("faultcode", is("soap:Server"))
            .body("faultstring", is(expected));
    }

    @Test
    public void faultTransformerTest() {
        requestTemplate.add("data", new IntygsData("<root></root>")); // This breakes the XML Schema

        // GetCertificateType does not have a fault transformer, SoapFault is expected
        given().body(requestTemplate.render()).when().post("inera-certificate/get-certificate-type-info/v1.0").then().statusCode(500)
            .rootPath("Envelope.Body.Fault").body("faultcode", is("soap:Client")).body("faultstring", startsWith("Unmarshalling Error"));
    }

    @SuppressWarnings("unused")
    private static class IntygsData {

        public final String intygsId;

        public IntygsData(String intygsId) {
            this.intygsId = intygsId;
        }
    }

}
