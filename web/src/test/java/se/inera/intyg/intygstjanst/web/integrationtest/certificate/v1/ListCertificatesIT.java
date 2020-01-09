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
package se.inera.intyg.intygstjanst.web.integrationtest.certificate.v1;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.response.ValidatableResponse;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

public class ListCertificatesIT extends BaseIntegrationTest {

    private STGroup templateGroup;

    private static final List<String> INTYG_IDS = Arrays
        .asList("listCertificatesITfk7263certificateId", "listCertificatesITlusecertificateId");
    private static final String PERSON_ID = "19010101-0101";

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        templateGroup = new STGroupFile("integrationtests/listcertificates/requests.stg");
        cleanup();
    }

    @After
    public void cleanup() {
        INTYG_IDS.stream().forEach(id -> IntegrationTestUtil.deleteIntyg(id));
        IntegrationTestUtil.deleteCertificatesForCitizen(PERSON_ID);
    }

    @Test
    public void listCertificates() {
        IntegrationTestUtil.givenIntyg(INTYG_IDS.get(0), "fk7263", FK7263_VERSION, PERSON_ID, false);
        IntegrationTestUtil.givenIntyg(INTYG_IDS.get(1), "luse", LUSE_VERSION, PERSON_ID, false);

        givenRequest(PERSON_ID).body("meta.size()", is(2)).body("meta[0].certificateId", anyOf(is(INTYG_IDS.get(0)), is(INTYG_IDS.get(1))))
            .body("meta[1].certificateId", anyOf(is(INTYG_IDS.get(0)), is(INTYG_IDS.get(1)))).body("result.resultCode", is("OK"));
    }

    @Test
    public void listCertificatesCertificateType() {
        IntegrationTestUtil.givenIntyg(INTYG_IDS.get(0), "fk7263", FK7263_VERSION, PERSON_ID, false);
        IntegrationTestUtil.givenIntyg(INTYG_IDS.get(1), "luse", LUSE_VERSION, PERSON_ID, false);

        givenCertificateTypeRequest(PERSON_ID, "fk7263").body("meta.size()", is(1)).body("meta[0].certificateId", is(INTYG_IDS.get(0)))
            .body("result.resultCode", is("OK"));
    }

    @Test
    public void listCertificatesDateInterval() {
        IntegrationTestUtil.givenIntyg(INTYG_IDS.get(0), "fk7263", FK7263_VERSION, PERSON_ID, false);
        IntegrationTestUtil.givenIntyg(INTYG_IDS.get(1), "luse", LUSE_VERSION, PERSON_ID, false);

        givenDateIntervalRequest(PERSON_ID, LocalDateTime.now(), LocalDateTime.now().plusDays(2)).body("meta.size()", is(0))
            .body("result.resultCode",
                is("OK"));
    }

    @Test
    public void faultTransformerTest() {
        givenRequest("</tag>").body("result.resultCode", is("ERROR")).body("result.errorText", startsWith("Unmarshalling Error"));
    }

    private ValidatableResponse givenRequest(String personId) {
        return givenRequest(personId, null, null, null, "request");
    }

    private ValidatableResponse givenCertificateTypeRequest(String personId, String certificateType) {
        return givenRequest(personId, certificateType, null, null, "requestCertificateType");
    }

    private ValidatableResponse givenDateIntervalRequest(String personId, LocalDateTime fromDate, LocalDateTime toDate) {
        return givenRequest(personId, null, fromDate, toDate, "requestDateInterval");
    }

    private ValidatableResponse givenRequest(String personId, String certificateType, LocalDateTime fromDate, LocalDateTime toDate,
        String template) {
        ST requestTemplate = templateGroup.getInstanceOf(template);
        requestTemplate.add("personId", personId);
        if (certificateType != null) {
            requestTemplate.add("certificateType", certificateType);
        }
        if (fromDate != null) {
            requestTemplate.add("fromDate", fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (toDate != null) {
            requestTemplate.add("toDate", toDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        return given().body(requestTemplate.render()).when().post("inera-certificate/list-certificates/v1.0").then().statusCode(200)
            .rootPath("Envelope.Body.ListCertificatesResponse.");
    }
}
