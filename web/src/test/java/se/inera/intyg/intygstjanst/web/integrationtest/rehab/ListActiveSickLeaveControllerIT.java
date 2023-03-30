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
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.internal.mapping.Jackson2Mapper;
import io.restassured.mapper.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKod;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.infra.sjukfall.dto.Patient;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.dto.Vardenhet;
import se.inera.intyg.infra.sjukfall.dto.Vardgivare;
import se.inera.intyg.intygstjanst.web.integrationtest.InternalApiBaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveResponseDTO;

public class ListActiveSickLeaveControllerIT extends InternalApiBaseIntegrationTest {

    private static final String CERTIFICATE_ID_1 = "certificateId1";
    private static final String CERTIFICATE_ID_2 = "certificateId2";

    private static final String PATIENT_ID_1 = "191212121212";
    private static final String PATIENT_ID_2 = "190101010101";

    private static final String UNIT_ID = "TSTNMT2321000156-ALMC";
    private static final String ANOTHER_UNIT_ID = "TSTNMT2321000156-ALMP";
    private static final String CARE_UNIT_ID = "TSTNMT2321000156-ALMC";
    private static final String UNIT_NAME = "Alfa Medicincentrum";
    private static final String CARE_PROVIDER_ID = "TSTNMT2321000156-ALFA";
    private static final String EMPLOYEE_HSA_ID = "TSTNMT2321000156-DRAA";
    private static final String EMPLOYEE_NAME = "Ajla Doktor";
    private static final String ANOTHER_EMPLOYEE_NAME = "Arnold Johanssson";
    private static final String ANOTHER_EMPLOYEE_HSA_ID = "TSTNMT2321000156-1079";
    private static final String BASE_URI = "http://localhost:8180";
    private static final String API_ENDPOINT = "inera-certificate/internalapi/sickleave/active";
    private static final String REQUEST_TEMPLATE = "requestParameterizedLocal";
    private static final int STATUS_CODE = 200;

    @BeforeEach
    void beforeEach() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
    }

    @AfterEach
    void afterEach() {
        deleteDataForCareProviders();
    }

    private void deleteDataForCareProviders() {
        given().contentType(ContentType.JSON).body(List.of(CERTIFICATE_ID_1, CERTIFICATE_ID_2))
            .when().delete("inera-certificate/resources/certificate/deleteCertificates")
            .then().statusCode(200);
    }

    @Test
    void shouldReturnListOfActiveSickLeavesForUnit() {
        registerCertificateWithDateParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null);
        registerCertificateWithDateParameters(CERTIFICATE_ID_2, PATIENT_ID_2, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null);

        final var sickLeaveRequestDTO = getRequest(null, CARE_UNIT_ID, null, 0, 0);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(0, 5, List.of(CERTIFICATE_ID_1), CERTIFICATE_ID_1, PATIENT_ID_1, false),
            getExpectSjukfallEnhet(0, 5, List.of(CERTIFICATE_ID_2), CERTIFICATE_ID_2, PATIENT_ID_2, false));
        final var response = getResponse(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    @Test
    void shouldReturnListOfRecentlyCompletedSickLeavesForUnit() {
        registerCertificateWithDateParameters(CERTIFICATE_ID_1, PATIENT_ID_1, -5, -3,
            EMPLOYEE_HSA_ID, CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null);
        registerCertificateWithDateParameters(CERTIFICATE_ID_2, PATIENT_ID_2, -5, -3,
            EMPLOYEE_HSA_ID, CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null);

        final var sickLeaveRequestDTO = getRequest(null, CARE_UNIT_ID, null, 0, 3);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(-5, -3, List.of(CERTIFICATE_ID_1), CERTIFICATE_ID_1, PATIENT_ID_1, true),
            getExpectSjukfallEnhet(-5, -3, List.of(CERTIFICATE_ID_2), CERTIFICATE_ID_2, PATIENT_ID_2, true));
        final var response = getResponse(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    @Test
    void shouldNotIncludeRevokedCertificatesForUnit() {
        registerCertificateWithDateParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null);
        registerCertificateWithDateParameters(CERTIFICATE_ID_2, PATIENT_ID_2, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null);

        IntegrationTestUtil.revokeCertificate(CERTIFICATE_ID_1, PATIENT_ID_1);

        final var sickLeaveRequestDTO = getRequest(null, CARE_UNIT_ID, null, 0, 0);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(0, 5, List.of(CERTIFICATE_ID_2), CERTIFICATE_ID_2, PATIENT_ID_2, false));
        final var response = getResponse(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    @Test
    void shouldReturnListOfSickLeavesForUnitFilteredOnDoctorId() {
        registerCertificateWithDateParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 5,
            ANOTHER_EMPLOYEE_HSA_ID, CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null);
        registerCertificateWithDateParameters(CERTIFICATE_ID_2, PATIENT_ID_2, 0, 5,
            EMPLOYEE_HSA_ID, CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null);

        final var sickLeaveRequestDTO = getRequest(null, CARE_UNIT_ID, EMPLOYEE_HSA_ID, 0, 0);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(0, 5, List.of(CERTIFICATE_ID_2), CERTIFICATE_ID_2, PATIENT_ID_2, false));
        final var response = getResponse(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    @Test
    void shouldReturnListOfSickLeavesForUnitFilteredOnUnitId() {
        registerCertificateWithDateParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, ANOTHER_UNIT_ID, EMPLOYEE_NAME, null, null);
        registerCertificateWithDateParameters(CERTIFICATE_ID_2, PATIENT_ID_2, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null);

        final var sickLeaveRequestDTO = getRequest(UNIT_ID, CARE_UNIT_ID, null, 0, 0);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(0, 5, List.of(CERTIFICATE_ID_2), CERTIFICATE_ID_2, PATIENT_ID_2, false));
        final var response = getResponse(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    @Test
    void shouldReturnListOfSickLeavesForUnitWithinGap() {
        registerCertificateWithDateParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 5,
            EMPLOYEE_HSA_ID, CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null);
        registerCertificateWithDateParameters(CERTIFICATE_ID_2, PATIENT_ID_1, 10, 20,
            EMPLOYEE_HSA_ID, CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null);

        final var sickLeaveRequestDTO = getRequest(null, CARE_UNIT_ID, null, 5, 0);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(0, 20, List.of(CERTIFICATE_ID_1, CERTIFICATE_ID_2), CERTIFICATE_ID_1, PATIENT_ID_1, false));
        final var response = getResponse(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    @Test
    void shouldUpdateDoctorName() {
        registerCertificateWithDateParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 5,
            EMPLOYEE_HSA_ID, CARE_PROVIDER_ID, UNIT_ID, ANOTHER_EMPLOYEE_NAME, null, null);

        final var sickLeaveRequestDTO = getRequest(null, CARE_UNIT_ID, null, 0, 0);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(0, 5, List.of(CERTIFICATE_ID_1), CERTIFICATE_ID_1, PATIENT_ID_1, false)
        );
        final var response = getResponse(sickLeaveRequestDTO);

        assertEquals(expectedResponse.get(0).getLakare().getNamn(), response.get(0).getLakare().getNamn());
    }

    @Test
    void shouldReturnListOfComplementedSickLeavesForUnit() {
        registerCertificateWithDateParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null);
        registerCertificateWithDateParameters(CERTIFICATE_ID_2, PATIENT_ID_1, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, RelationKod.KOMPLT, CERTIFICATE_ID_1);

        final var sickLeaveRequestDTO = getRequest(null, CARE_UNIT_ID, null, 0, 0);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(0, 5, List.of(CERTIFICATE_ID_2), CERTIFICATE_ID_2, PATIENT_ID_1, false));
        final var response = getResponse(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    @Test
    void shouldReturnListOfReplacedSickLeavesForUnit() {
        registerCertificateWithDateParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null);
        registerCertificateWithDateParameters(CERTIFICATE_ID_2, PATIENT_ID_1, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, RelationKod.ERSATT, CERTIFICATE_ID_1);

        final var sickLeaveRequestDTO = getRequest(null, CARE_UNIT_ID, null, 0, 0);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(0, 5, List.of(CERTIFICATE_ID_2), CERTIFICATE_ID_2, PATIENT_ID_1, false));
        final var response = getResponse(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    private SjukfallEnhet getExpectSjukfallEnhet(int fromDays, int toDays, List<String> certificateIds, String activeCertificate,
        String patientId, boolean recentlyCompleted) {
        final var sjukfallEnhet = new SjukfallEnhet();
        sjukfallEnhet.setVardgivare(
            Vardgivare.create(CARE_PROVIDER_ID, null)
        );
        sjukfallEnhet.setVardenhet(
            Vardenhet.create(CARE_UNIT_ID, UNIT_NAME)
        );
        sjukfallEnhet.setLakare(
            Lakare.create(EMPLOYEE_HSA_ID, EMPLOYEE_NAME)
        );
        sjukfallEnhet.setPatient(
            Patient.create(formatPatientId(patientId), "")
        );
        sjukfallEnhet.setDiagnosKod(
            DiagnosKod.create("S47")
        );
        sjukfallEnhet.setBiDiagnoser(Collections.emptyList());
        sjukfallEnhet.setStart(LocalDate.now().plusDays(fromDays));
        sjukfallEnhet.setSlut(LocalDate.now().plusDays(toDays));
        sjukfallEnhet.setIntyg(certificateIds.size());
        sjukfallEnhet.setIntygLista(certificateIds);
        sjukfallEnhet.setAktivIntygsId(activeCertificate);
        sjukfallEnhet.setGrader(List.of(75));
        if (!recentlyCompleted) {
            sjukfallEnhet.setAktivGrad(75);
        }
        return sjukfallEnhet;
    }

    private String formatPatientId(String patientId) {
        return patientId.substring(0, 8) + "-" + patientId.substring(8);
    }

    private List<SjukfallEnhet> getResponse(SickLeaveRequestDTO sickLeaveRequestDTO) {
        return given()
            .baseUri(BASE_URI)
            .contentType(ContentType.JSON)
            .body(sickLeaveRequestDTO)
            .expect()
            .statusCode(STATUS_CODE)
            .when()
            .post(API_ENDPOINT)
            .then()
            .extract()
            .response()
            .as(SickLeaveResponseDTO.class, getObjectMapperForDeserialization()).getContent();
    }

    private SickLeaveRequestDTO getRequest(String unitId, String careUnitId, String doctorId, int maxCertificateGap,
        int maxDaysSinceSickLeaveCompleted) {
        final var sickLeaveRequestDTO = new SickLeaveRequestDTO();
        sickLeaveRequestDTO.setUnitId(unitId);
        sickLeaveRequestDTO.setCareUnitId(careUnitId);
        sickLeaveRequestDTO.setDoctorId(doctorId);
        sickLeaveRequestDTO.setMaxCertificateGap(maxCertificateGap);
        sickLeaveRequestDTO.setMaxDaysSinceSickLeaveCompleted(maxDaysSinceSickLeaveCompleted);
        return sickLeaveRequestDTO;
    }

    private void registerCertificateWithDateParameters(String certificateId, String patientId,
        int fromDays, int toDays, String doctorId, String careProviderId, String unitId, String doctorName, RelationKod relationKod,
        String relationsId) {
        final var sickLeaveITConfigProvider = new SickLeaveITConfigProvider(careProviderId, certificateId, patientId, fromDays, toDays,
            unitId, doctorId, doctorName, relationsId, relationKod);
        IntegrationTestUtil.registerCertificateWithSickLeaveConfig(sickLeaveITConfigProvider);
    }

    private ObjectMapper getObjectMapperForDeserialization() {
        return new Jackson2Mapper(((type, charset) -> new CustomObjectMapper()));
    }

    public class SickLeaveITConfigProvider {

        private final String careProviderId;
        private final String certificateId;
        private final String patientId;
        private final int fromDays;
        private final int toDays;
        private final String unitId;
        private final String doctorId;
        private final String doctorName;
        private final String relationsId;
        private final RelationKod relationKod;

        SickLeaveITConfigProvider(String careProviderId, String certificateId, String patientId, int fromDays, int toDays, String unitId,
            String doctorId, String doctorName, String relationsId, RelationKod relationKod) {
            this.careProviderId = careProviderId;
            this.certificateId = certificateId;
            this.patientId = patientId;
            this.fromDays = fromDays;
            this.toDays = toDays;
            this.unitId = unitId;
            this.doctorId = doctorId;
            this.doctorName = doctorName;
            this.relationsId = relationsId;
            this.relationKod = relationKod;
        }

        public String getCareProviderId() {
            return careProviderId;
        }

        public String getCertificateId() {
            return certificateId;
        }

        public String getPatientId() {
            return patientId;
        }

        public int getFromDays() {
            return fromDays;
        }

        public int getToDays() {
            return toDays;
        }

        public String getUnitId() {
            return unitId;
        }

        public String getDoctorId() {
            return doctorId;
        }

        public String getDoctorName() {
            return doctorName;
        }

        public String getRelationsId() {
            return relationsId;
        }

        public RelationKod getRelationKod() {
            return relationKod;
        }
    }
}
