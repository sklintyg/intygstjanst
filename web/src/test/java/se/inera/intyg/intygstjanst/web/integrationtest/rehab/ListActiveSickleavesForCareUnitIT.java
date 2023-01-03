/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringStartsWith.startsWith;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;

import se.inera.intyg.intygstjanst.web.integrationtest.BaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;

/**
 * Created by eriklupander on 2016-02-16.
 */
public class ListActiveSickleavesForCareUnitIT extends BaseIntegrationTest {

    private static final List<String> INTYG_IDS = Arrays.asList("listActiveSickleavesForCareUnitITcertificateId1",
        "listActiveSickleavesForCareUnitITcertificateId2", "listActiveSickleavesForCareUnitITcertificateId3",
        "listActiveSickleavesForCareUnitITcertificateId4");
    private static final String PERSON_ID = "19010101-0101";
    private static final String PERSON_ID_2 = "191212121212";
    private static final String PERSON_ID_2_DASHED = "19121212-1212";
    private static final String REGISTER_TEMPLATE_Fk7263 = "listActiveSickLeaves";
    private static final String CARE_UNIT_ID = "linkoping";

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        cleanup();
    }

    @After
    public void cleanup() {
        INTYG_IDS.stream().forEach(id -> IntegrationTestUtil.deleteIntyg(id));
        IntegrationTestUtil.deleteCertificatesForUnit(CARE_UNIT_ID);
    }

    private static final String REQUEST =
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:riv:itintegration:registry:1\" xmlns:urn1=\"urn:riv:clinicalprocess:healthcond:rehabilitation:ListActiveSickLeavesForCareUnitResponder:1\" xmlns:urn2=\"urn:riv:clinicalprocess:healthcond:certificate:types:2\">\n"
            +
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

    private static final String REQUEST_WITH_PATIENTID =
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:riv:itintegration:registry:1\" xmlns:urn1=\"urn:riv:clinicalprocess:healthcond:rehabilitation:ListActiveSickLeavesForCareUnitResponder:1\" xmlns:urn2=\"urn:riv:clinicalprocess:healthcond:certificate:types:2\">\n"
            +
            "   <soapenv:Header>\n" +
            "      <urn:LogicalAddress>1</urn:LogicalAddress>\n" +
            "   </soapenv:Header>\n" +
            "   <soapenv:Body>\n" +
            "      <urn1:ListActiveSickLeavesForCareUnit>\n" +
            "         <urn1:enhets-id>\n" +
            "            <urn2:root>1.2.752.129.2.1.4.1</urn2:root>\n" +
            "            <urn2:extension>{{careUnitHsaId}}</urn2:extension>\n" +
            "         </urn1:enhets-id>\n" +
            "         <urn1:person-id>\n" +
            "            <urn2:root></urn2:root>\n" +
            "            <urn2:extension>{{patientId}}</urn2:extension>\n" +
            "         </urn1:person-id>\n" +
            "         <!--You may enter ANY elements at this point-->\n" +
            "      </urn1:ListActiveSickLeavesForCareUnit>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    private static final String REQUEST_INCLUDING_EXPIRED_SICK_LEAVES_WITHIN_10_DAYS =
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:riv:itintegration:registry:1\" xmlns:urn1=\"urn:riv:clinicalprocess:healthcond:rehabilitation:ListActiveSickLeavesForCareUnitResponder:1\" xmlns:urn2=\"urn:riv:clinicalprocess:healthcond:certificate:types:2\">\n"
            +
            "   <soapenv:Header>\n" +
            "      <urn:LogicalAddress>1</urn:LogicalAddress>\n" +
            "   </soapenv:Header>\n" +
            "   <soapenv:Body>\n" +
            "      <urn1:ListActiveSickLeavesForCareUnit>\n" +
            "         <urn1:enhets-id>\n" +
            "            <urn2:root>1.2.752.129.2.1.4.1</urn2:root>\n" +
            "            <urn2:extension>{{careUnitHsaId}}</urn2:extension>\n" +
            "         </urn1:enhets-id>\n" +
            "         <urn1:max-dagar-sedan-avslut>10</urn1:max-dagar-sedan-avslut>\n" +
            "         <!--You may enter ANY elements at this point-->\n" +
            "      </urn1:ListActiveSickLeavesForCareUnit>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    private static final String BASE = "Envelope.Body.ListActiveSickLeavesForCareUnitResponse.";

    @Test
    public void testReadIntygsData() {
        IntegrationTestUtil.registerMedicalCertificate(INTYG_IDS.get(0), PERSON_ID, REGISTER_TEMPLATE_Fk7263);
        IntegrationTestUtil.registerMedicalCertificate(INTYG_IDS.get(1), PERSON_ID, REGISTER_TEMPLATE_Fk7263);
        IntegrationTestUtil.registerMedicalCertificate(INTYG_IDS.get(2), PERSON_ID, REGISTER_TEMPLATE_Fk7263);
        IntegrationTestUtil
            .registerCertificateWithDateParameters(INTYG_IDS.get(3), PERSON_ID_2, IntegrationTestUtil.IntegrationTestCertificateType.LISJP,
                -2, 2);

        given().with().body(REQUEST.replace("{{careUnitHsaId}}", CARE_UNIT_ID))
            .expect()
            .statusCode(200)
            .body(BASE + "resultCode", is("OK"))
            .body(BASE + "intygsLista.intygsData.size()", equalTo(4))
            .body(BASE + "intygsLista.intygsData[0].patient.personId.extension", is(PERSON_ID))
            .body(BASE + "intygsLista.intygsData[1].patient.personId.extension", is(PERSON_ID))
            .body(BASE + "intygsLista.intygsData[2].patient.personId.extension", is(PERSON_ID))
            .body(BASE + "intygsLista.intygsData[3].patient.personId.extension", is(PERSON_ID_2_DASHED))
            .when()
            .post("inera-certificate/list-active-sick-leaves-for-care-unit/v1.0");
    }

    @Test
    public void testReadIntygsDataWithPatient() {
        IntegrationTestUtil.registerMedicalCertificate(INTYG_IDS.get(0), PERSON_ID, REGISTER_TEMPLATE_Fk7263);
        IntegrationTestUtil.registerMedicalCertificate(INTYG_IDS.get(1), PERSON_ID, REGISTER_TEMPLATE_Fk7263);
        IntegrationTestUtil.registerMedicalCertificate(INTYG_IDS.get(2), PERSON_ID, REGISTER_TEMPLATE_Fk7263);
        IntegrationTestUtil
            .registerCertificateWithDateParameters(INTYG_IDS.get(3), PERSON_ID_2, IntegrationTestUtil.IntegrationTestCertificateType.LISJP,
                -2, 2);

        given().with().body(REQUEST_WITH_PATIENTID
            .replace("{{careUnitHsaId}}", CARE_UNIT_ID)
            .replace("{{patientId}}", PERSON_ID_2_DASHED))

            .expect()
            .statusCode(200)
            .body(BASE + "resultCode", is("OK"))
            .body(BASE + "intygsLista.intygsData.size()", equalTo(1))
            .body(BASE + "intygsLista.intygsData[0].patient.personId.extension", is(PERSON_ID_2_DASHED))
            .when()
            .post("inera-certificate/list-active-sick-leaves-for-care-unit/v1.0");
    }

    @Test
    public void testReadIntygsDataFailsOnMissingCareUnitId() {
        IntegrationTestUtil.registerMedicalCertificate(INTYG_IDS.get(0), PERSON_ID, REGISTER_TEMPLATE_Fk7263);
        IntegrationTestUtil.registerMedicalCertificate(INTYG_IDS.get(1), PERSON_ID, REGISTER_TEMPLATE_Fk7263);

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
        IntegrationTestUtil.givenIntyg(INTYG_IDS.get(0), "fk7263", FK7263_VERSION, PERSON_ID, false);
        IntegrationTestUtil.givenIntyg(INTYG_IDS.get(1), "luse", LUSE_VERSION, PERSON_ID, false);

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

    @Test
    public void testReadIntygsDataIncludingRecentlyExpiredSickLeaves() {
        IntegrationTestUtil
            .registerCertificateWithDateParameters(INTYG_IDS.get(3), PERSON_ID_2, IntegrationTestUtil.IntegrationTestCertificateType.LISJP,
                -14, -7);

        given().with().body(REQUEST_INCLUDING_EXPIRED_SICK_LEAVES_WITHIN_10_DAYS.replace("{{careUnitHsaId}}", CARE_UNIT_ID))
            .expect()
            .statusCode(200)
            .body(BASE + "resultCode", is("OK"))
            .body(BASE + "intygsLista.intygsData.size()", equalTo(1))
            .body(BASE + "intygsLista.intygsData[0].patient.personId.extension", is(PERSON_ID_2_DASHED))
            .when()
            .post("inera-certificate/list-active-sick-leaves-for-care-unit/v1.0");
    }

    @Test
    public void testReadIntygsDataNotIncludingSickLeavesExpiredForMoreThanTwoWeeks() {
        IntegrationTestUtil
            .registerCertificateWithDateParameters(INTYG_IDS.get(3), PERSON_ID_2, IntegrationTestUtil.IntegrationTestCertificateType.LISJP,
                -21, -14);

        given().with().body(REQUEST_INCLUDING_EXPIRED_SICK_LEAVES_WITHIN_10_DAYS.replace("{{careUnitHsaId}}", CARE_UNIT_ID))
            .expect()
            .statusCode(200)
            .body(BASE + "resultCode", is("OK"))
            .body(BASE + "intygsLista.intygsData.size()", equalTo(0))
            .when()
            .post("inera-certificate/list-active-sick-leaves-for-care-unit/v1.0");
    }

    @Test
    public void testReadIntygsDataNotIncludingSickLeavesActiveInTheFuture() {
        IntegrationTestUtil
            .registerCertificateWithDateParameters(INTYG_IDS.get(3), PERSON_ID_2, IntegrationTestUtil.IntegrationTestCertificateType.LISJP,
                14, 21);

        given().with().body(REQUEST_INCLUDING_EXPIRED_SICK_LEAVES_WITHIN_10_DAYS.replace("{{careUnitHsaId}}", CARE_UNIT_ID))
            .expect()
            .statusCode(200)
            .body(BASE + "resultCode", is("OK"))
            .body(BASE + "intygsLista.intygsData.size()", equalTo(0))
            .when()
            .post("inera-certificate/list-active-sick-leaves-for-care-unit/v1.0");
    }

}
