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
import static org.hamcrest.core.Is.is;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;

import se.inera.intyg.intygstjanst.web.integrationstest.util.IntegrationTestUtil;

public class RegisterCertificateScenarioIT {
    private String intygsId = "123456";
    private String personId = "192703104321";
    private static final String REGISTER_BASE = "Envelope.Body.RegisterCertificateResponse.";
    private static final String GET_BASE = "Envelope.Body.GetCertificateResponse.";

    @Before
    public void setup() {
        IntegrationTestUtil.deleteIntyg(intygsId);
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
    }

    @Test
    public void runScenarioRegisterCertificate() {
        IntegrationTestUtil.registerCertificate(intygsId, personId);
        IntegrationTestUtil.sendCertificateToRecipient(intygsId, personId);
        IntegrationTestUtil.revokeCertificate(intygsId, personId);
    }

    @Test
    public void runScenarioRegisterAndGetCertificate() {
        ST requestTemplateForRegister = getRequestTemplate("register/request_default.stg");
        requestTemplateForRegister.add("data", new IntygsData(intygsId, personId));

        given().body(requestTemplateForRegister.render()).when().post("inera-certificate/register-certificate-se/v2.0").then().statusCode(200)
                .rootPath(REGISTER_BASE).body("result.resultCode", is("OK"));

        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        STGroup templateGroupGet = new STGroupFile("integrationtests/getcertificate/requests.stg");
        ST requestTemplateGet = templateGroupGet.getInstanceOf("request");
        requestTemplateGet.add("data", new IntygsData(intygsId, personId));

        given().body(requestTemplateGet.render()).when().post("inera-certificate/get-certificate-se/v2.0").then().statusCode(200)
                .rootPath(GET_BASE).body("intyg.intygs-id.extension", is(intygsId));
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(intygsId);
    }

    private static ST getRequestTemplate(String path) {
        String base = "integrationtests/";
        STGroup templateGroup = new STGroupFile(base+path);
        ST registerTemplateForConsent = templateGroup.getInstanceOf("request");
        return registerTemplateForConsent;
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
