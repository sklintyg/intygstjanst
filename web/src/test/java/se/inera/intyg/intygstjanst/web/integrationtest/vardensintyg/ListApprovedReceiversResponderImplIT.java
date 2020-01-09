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
package se.inera.intyg.intygstjanst.web.integrationtest.vardensintyg;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.response.ValidatableResponse;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;

public class ListApprovedReceiversResponderImplIT extends BaseIntegrationTest {

    private STGroup templateGroup;

    private STGroup registerApprovedTemplateGroup;

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/listapprovedreceivers/requests.stg");
        registerApprovedTemplateGroup = new STGroupFile("integrationtests/registerapprovedreceivers/requests.stg");
    }

    @Test
    public void testListApprovedReceiversLisjp() {
        // First, register a receiver
        String intygsId = UUID.randomUUID().toString();

        ST requestTemplate = registerApprovedTemplateGroup.getInstanceOf("request");
        requestTemplate.add("intygsId", intygsId);
        requestTemplate.add("mottagare", "FBA");

        given().body(requestTemplate.render())
            .when().post("inera-certificate/register-approved-receivers/v1.0")
            .then().statusCode(200)
            .rootPath("Envelope.Body.RegisterApprovedReceiversResponse.");

        givenRequest(intygsId)
            .body("receiverList.find { it.receiverId == 'FKASSA' }.approvalStatus", is("YES"))
            .body("receiverList.find { it.receiverId == 'FBA' }.approvalStatus", is("YES"));
    }

    private ValidatableResponse givenRequest(String intygsId) {
        ST requestTemplate = templateGroup.getInstanceOf("request");
        requestTemplate.add("intygsId", intygsId);

        return given()
            .body(requestTemplate.render())
            .when().post("inera-certificate/list-approved-receivers/v1.0")
            .then().statusCode(200)
            .rootPath("Envelope.Body.ListApprovedReceiversResponse.");
    }

}
