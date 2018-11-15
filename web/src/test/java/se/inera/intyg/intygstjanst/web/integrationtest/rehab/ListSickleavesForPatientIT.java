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
package se.inera.intyg.intygstjanst.web.integrationtest.rehab;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class ListSickleavesForPatientIT extends BaseIntegrationTest {

    private static final String INTYG_ID = "listActiveSickleavesForCareUnitITcertificateId1";
    private static final String PERSON_ID = "201212121212";

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        cleanup();
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteIntyg(INTYG_ID);
        IntegrationTestUtil.deleteCertificatesForCitizen(PERSON_ID);
    }

    private static final String REQUEST_WITH_PATIENTID = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:riv:itintegration:registry:1\" xmlns:urn1=\"urn:riv:clinicalprocess:healthcond:rehabilitation:ListSickLeavesForPersonResponder:1\" xmlns:urn2=\"urn:riv:clinicalprocess:healthcond:certificate:types:2\">\n"
            +
            "   <soapenv:Header>\n" +
            "      <urn:LogicalAddress>1</urn:LogicalAddress>\n" +
            "   </soapenv:Header>\n" +
            "   <soapenv:Body>\n" +
            "      <urn1:ListSickLeavesForPerson>\n" +
            "         <urn1:person-id>\n" +
            "            <urn2:root></urn2:root>\n" +
            "            <urn2:extension>{{patientId}}</urn2:extension>\n" +
            "         </urn1:person-id>\n" +
            "         <!--You may enter ANY elements at this point-->\n" +
            "      </urn1:ListSickLeavesForPerson>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    private static final String BASE = "Envelope.Body.ListSickLeavesForPersonResponse.";

    @Test
    public void testListSickLeavesForPerson() {
        IntegrationTestUtil.registerCertificateWithDateParameters(INTYG_ID, PERSON_ID, IntegrationTestUtil.IntegrationTestCertificateType.LISJP,
                14, 21);

        given().with().body(REQUEST_WITH_PATIENTID.replace("{{patientId}}", PERSON_ID))
                .expect()
                .statusCode(200)
                .body(BASE + "result.resultCode", is("OK"))
                .body(BASE + "intygsLista.intygsData.size()", equalTo(1))
                .when()
                .post("inera-certificate/list-sick-leaves-for-person/v1.0");
    }

}
