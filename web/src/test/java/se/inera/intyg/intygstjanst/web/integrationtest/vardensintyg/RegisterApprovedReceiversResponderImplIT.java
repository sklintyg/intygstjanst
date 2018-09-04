/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;

import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class RegisterApprovedReceiversResponderImplIT extends BaseIntegrationTest {

    private STGroup templateGroup;
    private STGroup listApprovedTemplateGroup;

    private static final String INTYG_TYP = "lisjp";

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        listApprovedTemplateGroup = new STGroupFile("integrationtests/listapprovedreceivers/requests.stg");
        templateGroup = new STGroupFile("integrationtests/registerapprovedreceivers/requests.stg");
    }

    @Test
    public void testRegisterApprovedReceivers() {
        givenRequest(UUID.randomUUID().toString(), INTYG_TYP, "AF")
                .body("result.resultCode", is("OK"));
    }

    @Test
    public void testRegisterAndThenUpdateApprovedReceivers() {
        String intygsId = UUID.randomUUID().toString();

        // Register AF
        givenRequest(intygsId, INTYG_TYP, "FBA").body("result.resultCode", is("OK"));

        // Check approved using ListApproved contract
        givenListApprovedRequest(intygsId)
                .body("receiverList.find { it.receiverId == 'FKASSA' }.approvalStatus", is("YES"))
                .body("receiverList.find { it.receiverId == 'FBA' }.approvalStatus", is("YES"));

        // Register FKASSA instead
        givenRequest(intygsId, INTYG_TYP, "FKASSA").body("result.resultCode", is("OK"));

        // Check approved using ListApproved contract
        givenListApprovedRequest(intygsId)
                .body("receiverList.find { it.receiverId == 'FKASSA' }.approvalStatus", is("YES"))
                .body("receiverList.find { it.receiverId == 'FBA' }.approvalStatus", is("NO"));

    }

    @Test
    public void testRegisterAndThenUpdateDoesNotUpdateApprovedReceivers() {
        String intygsId = UUID.randomUUID().toString();

        // Register AF
        givenRequest(intygsId, INTYG_TYP, "AF").body("result.resultCode", is("OK"));

        // Check approved using ListApproved contract
        givenListApprovedRequest(intygsId)
                .body("receiverList[0].receiverId", equalTo("FBA"));

        // Register FKASSA instead
        givenRequest(intygsId, INTYG_TYP, "NOT_A_RECEIVER_AT_ALL").body("result.resultCode", is("ERROR"));

        // Check approved using ListApproved contract
        givenListApprovedRequest(intygsId)
                .body("receiverList[0].receiverId", equalTo("FBA"));

    }

    private ValidatableResponse givenRequest(String intygsId, String intygsTyp, String mottagare) {
        ST requestTemplate = templateGroup.getInstanceOf("request");
        requestTemplate.add("intygsId", intygsId);
        requestTemplate.add("intygsTyp", intygsTyp);
        requestTemplate.add("mottagare", mottagare);

        return given()
                .body(requestTemplate.render())
                .when().post("inera-certificate/register-approved-receivers/v1.0")
                .then().statusCode(200)
                .rootPath("Envelope.Body.RegisterApprovedReceiversResponse.");
    }

    private ValidatableResponse givenListApprovedRequest(String intygsId) {
        ST requestTemplate = listApprovedTemplateGroup.getInstanceOf("request");
        requestTemplate.add("intygsId", intygsId);

        return given()
                .body(requestTemplate.render())
                .when().post("inera-certificate/list-approved-receivers/v1.0")
                .then().statusCode(200)
                .rootPath("Envelope.Body.ListApprovedReceiversResponse.");
    }
}
