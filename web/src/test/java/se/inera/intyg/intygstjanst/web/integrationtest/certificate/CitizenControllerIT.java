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

package se.inera.intyg.intygstjanst.web.integrationtest.certificate;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.http.ContentType;

import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.intygstjanst.web.integration.CitizenController.RequestObject;
import se.inera.intyg.intygstjanst.web.integration.CitizenController.ResponseObject;
import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

public class CitizenControllerIT extends BaseIntegrationTest {

    static final String PATH = "/inera-certificate/internalapi/citizens/certificates";

    static final String PID = "191212121212";

    @Before
    public void before() {
        IntegrationTestUtil.registerCertificateFromTemplate(UUID.randomUUID().toString(), "1", PID);
    }

    @After
    public void after() {
        IntegrationTestUtil.deleteCertificatesForCitizen(PID);
    }

    @Test
    public void listAndParseNonArchivedCertificates() {
        RequestObject params = RequestObject.of(PID, false);
        ResponseObject response = given().when()
            .contentType(ContentType.JSON)
            .body(params)
            .post(PATH)
            .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .extract()
            .as(ResponseObject[].class)[0];

        CertificateHolder ch = response.getCertificate();

        assertNotNull(ch.getId());
        assertNotNull(ch.getAdditionalInfo());
        assertNotNull(ch.getSigningDoctorName());
        assertNotNull(ch.getSignedDate());
        assertNull(ch.getOriginalCertificate());
    }


    @Test
    public void listArchivedCertificates() {
        RequestObject params = RequestObject.of(PID, true);
        given().when()
            .contentType(ContentType.JSON)
            .body(params)
            .post(PATH)
            .then()
            .statusCode(200)
            .body("size()", is(0));
    }

    @Test
    public void listCertificatesForUnkonwnUser() {
        RequestObject params = RequestObject.of("-", true);
        given().when()
            .contentType(ContentType.JSON)
            .body(params)
            .post(PATH)
            .then()
            .statusCode(200)
            .body("size()", is(0));
    }
}
