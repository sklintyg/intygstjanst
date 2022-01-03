/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integrationtest.intyginfo;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import se.inera.intyg.intygstjanst.web.integrationtest.InternalApiBaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

public class IntygInfoControllerIT extends InternalApiBaseIntegrationTest {

    public static final int OK = HttpStatus.OK.value();
    public static final int NOT_FOUND = HttpStatus.NOT_FOUND.value();

    private String url = "/inera-certificate/internalapi/intygInfo/";

    private String intygsId = "123456";
    private String personId1 = "191212121212";
    private String versionsId = "1.0";

    @Before
    public void setup() {
        IntegrationTestUtil.registerCertificateFromTemplate(intygsId, versionsId, personId1);
    }

    @After
    public void cleanUp() {
        IntegrationTestUtil.deleteIntyg(intygsId);
    }

    @Test
    public void getInfo() {
        given().expect().statusCode(OK)
            .when()
            .get(url + intygsId)
            .then()
            .body(matchesJsonSchemaInClasspath("integrationtests/intyginfo/get-intyginfo-response-schema.json"));
    }

    @Test
    public void getInfoNotFound() {
        given().expect().statusCode(NOT_FOUND)
            .when()
            .get(url + "NOT_FOUND");
    }

}
