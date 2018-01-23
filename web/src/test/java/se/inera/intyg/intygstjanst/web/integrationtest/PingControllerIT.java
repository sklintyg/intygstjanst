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
package se.inera.intyg.intygstjanst.web.integrationtest;

import static com.jayway.restassured.RestAssured.given;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;

/**
 * Basic
 *
 * Created by eriklupander on 2016-02-15.
 */
public class PingControllerIT extends BaseIntegrationTest {

    @Test
    public void testPostDebugLog() {

        given().contentType(ContentType.JSON).
                expect().statusCode(200)
                .body(Matchers.containsString("OK"))
                .when().get("inera-certificate/health-check/ping");
    }
}
