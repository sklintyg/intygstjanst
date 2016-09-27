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
package se.inera.intyg.intygstjanst.web.integrationtest.rehab;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringStartsWith.startsWith;

import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;

/**
 * Created by eriklupander on 2016-02-16.
 */
public class ListActiveSickleavesForCareUnitIT extends BaseIntegrationTest {

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
    }

    private static final String REQUEST = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:riv:itintegration:registry:1\" xmlns:urn1=\"urn:riv:clinicalprocess:healthcond:rehabilitation:ListActiveSickLeavesForCareUnitResponder:1\" xmlns:urn2=\"urn:riv:clinicalprocess:healthcond:certificate:types:2\">\n" +
            "   <soapenv:Header>\n" +
            "      <urn:LogicalAddress>1</urn:LogicalAddress>\n" +
            "   </soapenv:Header>\n" +
            "   <soapenv:Body>\n" +
            "      <urn1:ListActiveSickLeavesForCareUnit>\n" +
            "         <urn1:enhets-id>\n" +
            "            <urn2:root>1.2.752.129.2.1.4.1</urn2:root>\n" +
            "            <urn2:extension>{{careUnitHsaId}}</urn2:extension>\n" +
            "         </urn1:enhets-id>\n" +
            "         <!--You may enter ANY elements at this point-->\n" +
            "      </urn1:ListActiveSickLeavesForCareUnit>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    private static final String BASE = "Envelope.Body.ListActiveSickLeavesForCareUnitResponse.";

    @Test
    public void testReadIntygsDataForPrePopulatedIntyg() {

        given().with().body(REQUEST.replace("{{careUnitHsaId}}", "centrum-vast"))
                .expect()
                .statusCode(200)
                .body(BASE + "resultCode", is("OK"))
                .body(BASE + "intygsLista.intygsData.size()", equalTo(3))
                .body(BASE + "intygsLista.intygsData[0].patient.personId.extension", is("19121212-1212"))
                .when()
                .post("inera-certificate/list-active-sick-leaves-for-care-unit/v1.0");
    }

    @Test
    public void testReadIntygsDataFailsOnMissingCareUnitId() {

        given().with().body(REQUEST.replace("{{careUnitHsaId}}", ""))
                .expect()
                .statusCode(200)
                .body(BASE + "resultCode", is("ERROR"))
                .body(BASE + "comment", is("No careUnitHsaId specified in request."))
                .when()
                .post("inera-certificate/list-active-sick-leaves-for-care-unit/v1.0");
    }

    @Test
    public void testReadIntygsDataForUnknownUnit() {

        given().with().body(REQUEST.replace("{{careUnitHsaId}}", "unknown-unit"))
                .expect()
                .statusCode(200)
                .body(BASE + "resultCode", is("OK"))
                .body(BASE + "intygsLista.intygsData.size()", equalTo(0))
                .when()
                .post("inera-certificate/list-active-sick-leaves-for-care-unit/v1.0");
    }

    @Test
    public void faultTransformerTest() {
        given().with().body(REQUEST.replace("{{careUnitHsaId}}", "</tag>"))
                .expect()
                .statusCode(200)
                .body(BASE + "resultCode", is("ERROR"))
                .body(BASE + "comment", startsWith("Unmarshalling Error"))
                .when()
                .post("inera-certificate/list-active-sick-leaves-for-care-unit/v1.0");
    }

}
