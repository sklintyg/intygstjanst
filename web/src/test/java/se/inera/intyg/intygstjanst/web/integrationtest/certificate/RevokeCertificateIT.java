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

import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;

/**
 * @author katarinaolsson
 *
 */
public class RevokeCertificateIT extends BaseIntegrationTest {
    private static final String REVOKE_BASE = "Envelope.Body.RevokeCertificateResponse.";
    private static final String REGISTER_BASE = "Envelope.Body.RegisterCertificateResponse.";

    private ST requestTemplateForRevoke;
    private ST requestTemplateForRegister;

    private String intygsId = "123456";

    private String personId1 = "192703104321";
    private String personId2 = "195206172339";

    private STGroup templateGroupForRevoke;
    private STGroup templateGroupForRegister;

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();

        templateGroupForRevoke = new STGroupFile("integrationtests/revokecertificate/requests.stg");
        requestTemplateForRevoke = templateGroupForRevoke.getInstanceOf("request");

        templateGroupForRegister = new STGroupFile("integrationtests/register/requests.stg");
        requestTemplateForRegister = templateGroupForRegister.getInstanceOf("request");

        deleteIntyg(intygsId);
    }

    private void deleteIntyg(String id) {
        given().delete("inera-certificate/resources/certificate/" + id).then().statusCode(200);
    }

    @Test
    public void revokeCertificateWorks() {

        requestTemplateForRegister.add("data", new IntygsData(intygsId, personId1));
        given().body(requestTemplateForRegister.render()).
                when().
                post("inera-certificate/register-certificate-se/v2.0").
                then().
                statusCode(200).
                rootPath(REGISTER_BASE).
                body("result.resultCode", is("OK"));

        requestTemplateForRevoke.add("data", new IntygsData(intygsId, personId1));
        given().body(requestTemplateForRevoke.render()).
                when().
                post("inera-certificate/revoke-certificate-rivta/v1.0").
                then().
                statusCode(200).
                rootPath(REVOKE_BASE).
                body("result.resultCode", is("OK"));
    }


    private static class IntygsData {
        public final String intygsId;
        public final String personId;

        public IntygsData(String intygsId, String personId) {
            this.intygsId = intygsId;
            this.personId = personId;
        }
    }
}
