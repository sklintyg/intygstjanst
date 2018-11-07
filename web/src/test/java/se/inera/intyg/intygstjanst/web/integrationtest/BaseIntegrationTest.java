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

import com.jayway.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;

/**
 * Base class for REST / SOAP in-container tests for Intygstjänsten.
 *
 * @author eriklupander
 */
public abstract class BaseIntegrationTest {

    protected static final String LUSE_VERSION = "1.0";
    protected static final String FK7263_VERSION = "1.0";
    protected static final String TS_BAS_VERSION = "6.8";
    protected static final String TS_DIABETES_VERSION = "2.7";

    /**
     * Common setup for all tests
     */
    @Before
    public void setupBase() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI = System.getProperty("integration.tests.baseUrl");
    }

    @After
    public void cleanupBase() {
        RestAssured.reset();
    }


    @SuppressWarnings("unused")
    protected class RegisterIntygsData {
        public final String intygsId;
        public final String intygsVersion;
        public final String personId;

        public RegisterIntygsData(final String intygsId, final String intygsVersion, final String personId) {
            this.intygsId = intygsId;
            this.intygsVersion = intygsVersion;
            this.personId = personId;
        }
    }

    @SuppressWarnings("unused")
    protected class RevokeIntygsData {
        public final String intygsId;
        public final String personId;

        public RevokeIntygsData(String intygsId, String personId) {
            this.intygsId = intygsId;
            this.personId = personId;
        }
    }

}
