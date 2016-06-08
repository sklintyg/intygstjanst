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

public class SendCertificateToRecipientIT extends BaseIntegrationTest  {
    private static final String REGISTER_BASE = "Envelope.Body.RegisterCertificateResponse.";
    private static final String RECIPIENT_BASE = "Envelope.Body.SendCertificateToRecipientResponse.";

    private ST requestTemplateRecipient;
    private ST requestTemplateRegister;

    private STGroup templateGroupRecipient;
    private STGroup templateGroupRegister;
    private String personId1 = "192703104321";

    private String intygsId = "123456";

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroupRecipient = new STGroupFile("integrationtests/sendcertificatetorecipient/requests.stg");
        requestTemplateRecipient = templateGroupRecipient.getInstanceOf("request");

        templateGroupRegister = new STGroupFile("integrationtests/register/requests.stg");
        requestTemplateRegister = templateGroupRegister.getInstanceOf("request");

        IntegrationTestUtil.deleteIntyg(intygsId);
    }

    @Test
    public void sendCertificateToRecipientWorks() {

        requestTemplateRegister.add("data", new IntygsData(intygsId, personId1));
        given().body(requestTemplateRegister.render()).
                when().
                post("inera-certificate/register-certificate-se/v2.0").
                then().
                statusCode(200).
                rootPath(REGISTER_BASE).
                body("result.resultCode", is("OK"));

        requestTemplateRecipient.add("data", new IntygsData(intygsId, personId1));

        given().body(requestTemplateRecipient.render()).
                when().
                post("inera-certificate/send-certificate-to-recipient/v1.0").
                then().
                statusCode(200).
                rootPath(RECIPIENT_BASE).
                body("result.resultCode", is("OK"));
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(intygsId);
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
