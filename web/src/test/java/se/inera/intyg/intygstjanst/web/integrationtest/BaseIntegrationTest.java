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

package se.inera.intyg.intygstjanst.web.integrationtest;

import static com.jayway.restassured.RestAssured.given;

import org.junit.After;
import org.junit.Before;

import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;

import com.jayway.restassured.RestAssured;

/**
 * Base class for "REST-ish" integrationTests using RestAssured.
 * <p/>
 * Created by marced on 19/11/15.
 */
public abstract class BaseIntegrationTest {

    private static final String USER_JSON_FORM_PARAMETER = "userJsonDisplay";

    protected CustomObjectMapper objectMapper = new CustomObjectMapper();

    /**
     * Common setup for all tests
     */
    @Before
    public void setup() {
        RestAssured.reset();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI = System.getProperty("integration.tests.baseUrl");
    }

    /**
     * Common teardown for all tests
     */
    @After
    public void tearDown() {
        // TODO Remove all intyg and sjukfallintyg after each test
        // given().expect().statusCode(200).when().delete("testability/intyg");
    }


}
