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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.ValidatableResponse;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;

public class ListPossibleReceiversResponderImplIT extends BaseIntegrationTest {

    private STGroup templateGroup;

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/listpossiblereceivers/requests.stg");
    }

    @Test
    public void testListPossibleReceiversLisjp() {
        givenRequest("af00213")
            .body("receiverList.receiverId", is("AF"))
            .body("receiverList.receiverType", is("HUVUDMOTTAGARE"));
    }

    private ValidatableResponse givenRequest(String intygTyp) {
        ST requestTemplate = templateGroup.getInstanceOf("request");
        requestTemplate.add("intygTyp", intygTyp);

        return given()
            .body(requestTemplate.render())
            .when().post("inera-certificate/list-possible-receivers/v1.0")
            .then().statusCode(200)
            .rootPath("Envelope.Body.ListPossibleReceiversResponse.");
    }
}
