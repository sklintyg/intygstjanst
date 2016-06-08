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

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;

public class SetCertificateStatusIT extends BaseIntegrationTest {
    private ST requestTemplate;
    private String intygsId = "123456";
    private String personId = "192703104321";

    private static final String BASE = "Envelope.Body.SetCertificateStatusResponse.";

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();

        STGroup templateGroup = new STGroupFile("integrationtests/setcertificatestatus/requests.stg");
        requestTemplate = templateGroup.getInstanceOf("request");
        IntegrationTestUtil.deleteIntyg(intygsId);
    }

    @Test
    public void setCertificateStatusWorks() {
        IntegrationTestUtil.registerCertificate(intygsId, personId);
        requestTemplate.add("data", new IntygsData(intygsId));

        given().body(requestTemplate.render()).when().post("inera-certificate/set-certificate-status-rivta/v1.0").then().statusCode(200)
                .rootPath(BASE).body("result.resultCode", is("OK"));
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(intygsId);
    }

    private static class IntygsData {
        public final String intygsId;

        public IntygsData(String intygsId) {
            this.intygsId = intygsId;
        }
    }
}
