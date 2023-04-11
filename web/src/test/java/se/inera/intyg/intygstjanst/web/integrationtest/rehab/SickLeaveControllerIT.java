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

import io.restassured.http.ContentType;
import io.restassured.internal.mapping.Jackson2Mapper;
import io.restassured.mapper.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKapitel;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKod;
import se.inera.intyg.infra.sjukfall.dto.Formaga;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.infra.sjukfall.dto.Patient;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.dto.SjukfallIntyg;
import se.inera.intyg.infra.sjukfall.dto.Vardenhet;
import se.inera.intyg.infra.sjukfall.dto.Vardgivare;
import se.inera.intyg.infra.sjukfall.engine.LocalDateInterval;
import se.inera.intyg.infra.sjukfall.engine.SjukfallLangdCalculator;
import se.inera.intyg.intygstjanst.web.integrationtest.InternalApiBaseIntegrationTest;
import se.inera.intyg.intygstjanst.web.integrationtest.util.IntegrationTestUtil;
import se.inera.intyg.intygstjanst.web.service.dto.PopulateFiltersRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.PopulateFiltersResponseDTO;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveResponseDTO;

public class SickLeaveControllerIT extends InternalApiBaseIntegrationTest {

    private static final String CERTIFICATE_ID_1 = "certificateId1";
    private static final String CERTIFICATE_ID_2 = "certificateId2";
    private static final String PATIENT_ID_1 = "191212121212";
    private static final String PATIENT_ID_2 = "190101010101";
    private static final String UNIT_ID = "TSTNMT2321000156-ALMC";
    private static final String ANOTHER_UNIT_ID = "TSTNMT2321000156-ALMP";
    private static final String CARE_UNIT_ID = "TSTNMT2321000156-ALMC";
    private static final String ANOTHER_CARE_UNIT_ID = "TSTNMT2321000156-ALMP";
    private static final String UNIT_NAME = "Alfa Medicincentrum";
    private static final String CARE_PROVIDER_ID = "TSTNMT2321000156-ALFA";
    private static final String EMPLOYEE_HSA_ID = "TSTNMT2321000156-DRAA";
    private static final String EMPLOYEE_NAME = "Ajla Doktor";
    private static final String ANOTHER_EMPLOYEE_NAME = "Arnold Johansson";
    private static final String ANOTHER_EMPLOYEE_HSA_ID = "TSTNMT2321000156-1079";
    private static final String API_ENDPOINT_ACTIVE_SICK_LEAVE_FOR_UNIT = "inera-certificate/internalapi/sickleave/active";
    private static final String API_ENDPOINT_POPULATE_FILTERS = "inera-certificate/internalapi/sickleave/filters";
    private static final String REQUEST_LISJP_SIGN_DATE = "2015-12-07T15:48:05";
    private static final int STATUS_CODE = 200;
    private static final String DIAGNOSIS_CODE = "S47";
    private static final String ANOTHER_DIAGNOSIS_CODE = "B56";
    private static final String DIAGNOSIS_CHAPTER = "S00-T98Skador, förgiftningar och vissa andra följder av yttre orsaker";
    private static final String ANOTHER_DIAGNOSIS_CHAPTER = "A00-B99Vissa infektionssjukdomar och parasitsjukdomar";

    @Before
    public void beforeEach() {
        deleteDataForCareProviders();
    }

    @After
    public void afterEach() {
        deleteDataForCareProviders();
    }

    private void deleteDataForCareProviders() {
        given().contentType(ContentType.JSON).body(List.of(CERTIFICATE_ID_1, CERTIFICATE_ID_2))
            .when().delete("inera-certificate/resources/certificate/deleteCertificates")
            .then().statusCode(200);
    }

    @Test
    public void shouldReturnListOfActiveSickLeavesForUnit() {
        registerCertificateWithParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null, null);
        registerCertificateWithParameters(CERTIFICATE_ID_2, PATIENT_ID_2, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null, null);

        final var sickLeaveRequestDTO = getSickLeaveRequest(null, CARE_UNIT_ID, null, 0, 0, null, null, null);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(List.of(0), List.of(5), List.of(CERTIFICATE_ID_1), CERTIFICATE_ID_1, PATIENT_ID_1, false, 0),
            getExpectSjukfallEnhet(List.of(0), List.of(5), List.of(CERTIFICATE_ID_2), CERTIFICATE_ID_2, PATIENT_ID_2, false, 0));
        final var response = getResponseActiveSickLeaves(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void shouldReturnListOfRecentlyCompletedSickLeavesForUnit() {
        registerCertificateWithParameters(CERTIFICATE_ID_1, PATIENT_ID_1, -5, -3,
            EMPLOYEE_HSA_ID, CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null, null);
        registerCertificateWithParameters(CERTIFICATE_ID_2, PATIENT_ID_2, -5, -3,
            EMPLOYEE_HSA_ID, CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null, null);

        final var sickLeaveRequestDTO = getSickLeaveRequest(null, CARE_UNIT_ID, null, 0, 3, null, null, null);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(List.of(-5), List.of(-3), List.of(CERTIFICATE_ID_1), CERTIFICATE_ID_1, PATIENT_ID_1, true, 0),
            getExpectSjukfallEnhet(List.of(-5), List.of(-3), List.of(CERTIFICATE_ID_2), CERTIFICATE_ID_2, PATIENT_ID_2, true, 0));
        final var response = getResponseActiveSickLeaves(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void shouldNotIncludeRevokedCertificatesForUnit() {
        registerCertificateWithParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null, null);
        registerCertificateWithParameters(CERTIFICATE_ID_2, PATIENT_ID_2, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null, null);

        IntegrationTestUtil.revokeCertificate(CERTIFICATE_ID_1, PATIENT_ID_1);

        final var sickLeaveRequestDTO = getSickLeaveRequest(null, CARE_UNIT_ID, null, 0, 0, null, null, null);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(List.of(0), List.of(5), List.of(CERTIFICATE_ID_2), CERTIFICATE_ID_2, PATIENT_ID_2, false, 0));
        final var response = getResponseActiveSickLeaves(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void shouldReturnListOfSickLeavesForUnitFilteredOnDoctorId() {
        registerCertificateWithParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 5,
            ANOTHER_EMPLOYEE_HSA_ID, CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null, null);
        registerCertificateWithParameters(CERTIFICATE_ID_2, PATIENT_ID_2, 0, 5,
            EMPLOYEE_HSA_ID, CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null, null);

        final var sickLeaveRequestDTO = getSickLeaveRequest(null, CARE_UNIT_ID, EMPLOYEE_HSA_ID, 0, 0, null, null, null);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(List.of(0), List.of(5), List.of(CERTIFICATE_ID_2), CERTIFICATE_ID_2, PATIENT_ID_2, false, 0));
        final var response = getResponseActiveSickLeaves(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void shouldReturnListOfSickLeavesForUnitFilteredOnUnitId() {
        registerCertificateWithParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, ANOTHER_UNIT_ID, EMPLOYEE_NAME, null, null, null);
        registerCertificateWithParameters(CERTIFICATE_ID_2, PATIENT_ID_2, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null, null);

        final var sickLeaveRequestDTO = getSickLeaveRequest(UNIT_ID, CARE_UNIT_ID, null, 0, 0, null, null, null);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(List.of(0), List.of(5), List.of(CERTIFICATE_ID_2), CERTIFICATE_ID_2, PATIENT_ID_2, false, 0));
        final var response = getResponseActiveSickLeaves(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void shouldReturnListOfSickLeavesForUnitFilteredOnDays() {
        registerCertificateWithParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 10, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null, null);
        registerCertificateWithParameters(CERTIFICATE_ID_2, PATIENT_ID_2, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null, null);

        final var sickLeaveRequestDTO = getSickLeaveRequest(null, CARE_UNIT_ID, null, 0, 0, 2, 6, null);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(List.of(0), List.of(5), List.of(CERTIFICATE_ID_2), CERTIFICATE_ID_2, PATIENT_ID_2, false, 0));
        final var response = getResponseActiveSickLeaves(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void shouldReturnListOfSickLeavesForUnitFilteredOnDiagnosis() {
        registerCertificateWithParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null, ANOTHER_DIAGNOSIS_CODE);
        registerCertificateWithParameters(CERTIFICATE_ID_2, PATIENT_ID_2, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null, DIAGNOSIS_CODE);

        final var sickLeaveRequestDTO = getSickLeaveRequest(null, CARE_UNIT_ID, null, 0, 0, null, null,
            List.of(new DiagnosKapitel(ANOTHER_DIAGNOSIS_CHAPTER)));
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(List.of(0), List.of(5), List.of(CERTIFICATE_ID_2), CERTIFICATE_ID_2, PATIENT_ID_2, false, 0));
        final var response = getResponseActiveSickLeaves(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void shouldReturnListOfSickLeavesForUnitWithinGap() {
        registerCertificateWithParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 5,
            EMPLOYEE_HSA_ID, CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null, null);
        registerCertificateWithParameters(CERTIFICATE_ID_2, PATIENT_ID_1, 10, 20,
            EMPLOYEE_HSA_ID, CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null, null);

        final var sickLeaveRequestDTO = getSickLeaveRequest(null, CARE_UNIT_ID, null, 5, 0, null, null, null);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(List.of(0, 10), List.of(5, 20), List.of(CERTIFICATE_ID_1, CERTIFICATE_ID_2), CERTIFICATE_ID_1,
                PATIENT_ID_1, false,
                sickLeaveRequestDTO.getMaxCertificateGap()));
        final var response = getResponseActiveSickLeaves(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void shouldUpdateDoctorName() {
        registerCertificateWithParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 5,
            EMPLOYEE_HSA_ID, CARE_PROVIDER_ID, UNIT_ID, ANOTHER_EMPLOYEE_NAME, null, null, null);

        final var sickLeaveRequestDTO = getSickLeaveRequest(null, CARE_UNIT_ID, null, 0, 0, null, null, null);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(List.of(0), List.of(5), List.of(CERTIFICATE_ID_1), CERTIFICATE_ID_1, PATIENT_ID_1, false, 0)
        );
        final var response = getResponseActiveSickLeaves(sickLeaveRequestDTO);

        assertEquals(expectedResponse.get(0).getLakare().getNamn(), response.get(0).getLakare().getNamn());
    }

    @Test
    public void shouldReturnListOfComplementedSickLeavesForUnit() {
        registerCertificateWithParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null, null);
        registerCertificateWithParameters(CERTIFICATE_ID_2, PATIENT_ID_1, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, RelationKod.KOMPLT, CERTIFICATE_ID_1, null);

        final var sickLeaveRequestDTO = getSickLeaveRequest(null, CARE_UNIT_ID, null, 0, 0, null, null, null);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(List.of(0), List.of(5), List.of(CERTIFICATE_ID_2), CERTIFICATE_ID_2, PATIENT_ID_1, false, 0));
        final var response = getResponseActiveSickLeaves(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void shouldReturnListOfReplacedSickLeavesForUnit() {
        registerCertificateWithParameters(CERTIFICATE_ID_1, PATIENT_ID_1, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, null, null, null);
        registerCertificateWithParameters(CERTIFICATE_ID_2, PATIENT_ID_1, 0, 5, EMPLOYEE_HSA_ID,
            CARE_PROVIDER_ID, UNIT_ID, EMPLOYEE_NAME, RelationKod.ERSATT, CERTIFICATE_ID_1, null);

        final var sickLeaveRequestDTO = getSickLeaveRequest(null, CARE_UNIT_ID, null, 0, 0, null, null, null);
        final var expectedResponse = List.of(
            getExpectSjukfallEnhet(List.of(0), List.of(5), List.of(CERTIFICATE_ID_2), CERTIFICATE_ID_2, PATIENT_ID_1, false, 0));
        final var response = getResponseActiveSickLeaves(sickLeaveRequestDTO);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void shouldReturnListOfDoctorsWithActiveSickLeaves() {
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, EMPLOYEE_HSA_ID, EMPLOYEE_NAME, DIAGNOSIS_CODE,
            0, 5, CERTIFICATE_ID_1, PATIENT_ID_1, null, null);
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, ANOTHER_EMPLOYEE_HSA_ID, ANOTHER_EMPLOYEE_NAME,
            DIAGNOSIS_CODE, 0, 5, CERTIFICATE_ID_2, PATIENT_ID_1, null, null);

        final var populateFiltersRequest = getPopulateFiltersRequest(CARE_UNIT_ID, 5, null);
        final var expectedResult = List.of(Lakare.create(EMPLOYEE_HSA_ID, EMPLOYEE_NAME),
            Lakare.create(ANOTHER_EMPLOYEE_HSA_ID, ANOTHER_EMPLOYEE_NAME));

        final var response = getResponsePopulateFiltersActiveDoctors(populateFiltersRequest);

        assertEquals(expectedResult, response);
    }

    @Test
    public void shouldReturnListOfDoctorsWithRecentlyCompletedSickLeaves() {
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, EMPLOYEE_HSA_ID, EMPLOYEE_NAME, DIAGNOSIS_CODE,
            -10, -5, CERTIFICATE_ID_1, PATIENT_ID_1, null, null);
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, ANOTHER_EMPLOYEE_HSA_ID, ANOTHER_EMPLOYEE_NAME,
            DIAGNOSIS_CODE, -10, -8, CERTIFICATE_ID_2, PATIENT_ID_2, null, null);

        final var populateFiltersRequest = getPopulateFiltersRequest(CARE_UNIT_ID, 5, null);
        final var expectedResult = List.of(Lakare.create(EMPLOYEE_HSA_ID, EMPLOYEE_NAME));

        final var response = getResponsePopulateFiltersActiveDoctors(populateFiltersRequest);

        assertEquals(expectedResult, response);
    }

    @Test
    public void shouldNotReturnListOfDoctorsWithReplacedSickLeaves() {
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, EMPLOYEE_HSA_ID, EMPLOYEE_NAME, DIAGNOSIS_CODE,
            0, 5, CERTIFICATE_ID_1, PATIENT_ID_1, null, null);
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, ANOTHER_EMPLOYEE_HSA_ID, ANOTHER_EMPLOYEE_NAME,
            DIAGNOSIS_CODE, 0, 5, CERTIFICATE_ID_2, PATIENT_ID_1, CERTIFICATE_ID_1, RelationKod.ERSATT);

        final var populateFiltersRequest = getPopulateFiltersRequest(CARE_UNIT_ID, 5, null);
        final var expectedResult = List.of(Lakare.create(ANOTHER_EMPLOYEE_HSA_ID, ANOTHER_EMPLOYEE_NAME));

        final var response = getResponsePopulateFiltersActiveDoctors(populateFiltersRequest);

        assertEquals(expectedResult, response);
    }

    @Test
    public void shouldNotReturnListOfDoctorsWithComplementedSickLeaves() {
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, EMPLOYEE_HSA_ID, EMPLOYEE_NAME, DIAGNOSIS_CODE,
            0, 5, CERTIFICATE_ID_1, PATIENT_ID_1, null, null);
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, ANOTHER_EMPLOYEE_HSA_ID, ANOTHER_EMPLOYEE_NAME,
            DIAGNOSIS_CODE, 0, 5, CERTIFICATE_ID_2, PATIENT_ID_1, CERTIFICATE_ID_1, RelationKod.KOMPLT);

        final var populateFiltersRequest = getPopulateFiltersRequest(CARE_UNIT_ID, 5, null);
        final var expectedResult = List.of(Lakare.create(ANOTHER_EMPLOYEE_HSA_ID, ANOTHER_EMPLOYEE_NAME));

        final var response = getResponsePopulateFiltersActiveDoctors(populateFiltersRequest);

        assertEquals(expectedResult, response);
    }

    @Test
    public void shouldReturnListOfDiagnosis() {
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, EMPLOYEE_HSA_ID, EMPLOYEE_NAME, DIAGNOSIS_CODE,
            0, 5, CERTIFICATE_ID_1, PATIENT_ID_1, null, null);
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, ANOTHER_EMPLOYEE_HSA_ID, ANOTHER_EMPLOYEE_NAME,
            ANOTHER_DIAGNOSIS_CODE, 0, 5, CERTIFICATE_ID_2, PATIENT_ID_1, null, null);

        final var populateFiltersRequest = getPopulateFiltersRequest(CARE_UNIT_ID, 5, null);
        final var expectedResult = List.of(new DiagnosKapitel(DIAGNOSIS_CHAPTER), new DiagnosKapitel(ANOTHER_DIAGNOSIS_CHAPTER));

        final var response = getResponsePopulateFiltersDiagnosis(populateFiltersRequest);

        assertEquals(expectedResult, response);
    }

    @Test
    public void shouldReturnDistictListOfDiagnosis() {
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, EMPLOYEE_HSA_ID, EMPLOYEE_NAME, DIAGNOSIS_CODE,
            0, 5, CERTIFICATE_ID_1, PATIENT_ID_1, null, null);
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, ANOTHER_EMPLOYEE_HSA_ID, ANOTHER_EMPLOYEE_NAME,
            DIAGNOSIS_CODE, 0, 5, CERTIFICATE_ID_2, PATIENT_ID_1, null, null);

        final var populateFiltersRequest = getPopulateFiltersRequest(CARE_UNIT_ID, 5, null);
        final var expectedResult = List.of(new DiagnosKapitel(DIAGNOSIS_CHAPTER));

        final var response = getResponsePopulateFiltersDiagnosis(populateFiltersRequest);

        assertEquals(expectedResult, response);
    }

    @Test
    public void shouldReturnListOfDiagnosisForRecentlyCompletedSickLeaves() {
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, EMPLOYEE_HSA_ID, EMPLOYEE_NAME,
            ANOTHER_DIAGNOSIS_CODE,
            -10, -5, CERTIFICATE_ID_1, PATIENT_ID_1, null, null);
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, ANOTHER_EMPLOYEE_HSA_ID, ANOTHER_EMPLOYEE_NAME,
            DIAGNOSIS_CODE, -10, -8, CERTIFICATE_ID_2, PATIENT_ID_2, null, null);

        final var populateFiltersRequest = getPopulateFiltersRequest(CARE_UNIT_ID, 5, null);
        final var expectedResult = List.of(new DiagnosKapitel(ANOTHER_DIAGNOSIS_CHAPTER));

        final var response = getResponsePopulateFiltersDiagnosis(populateFiltersRequest);

        assertEquals(expectedResult, response);
    }

    @Test
    public void shouldNotReturnListOfDiagnosisForReplacedSickLeaves() {
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, EMPLOYEE_HSA_ID, EMPLOYEE_NAME,
            ANOTHER_DIAGNOSIS_CODE,
            0, 5, CERTIFICATE_ID_1, PATIENT_ID_1, null, null);
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, ANOTHER_EMPLOYEE_HSA_ID, ANOTHER_EMPLOYEE_NAME,
            DIAGNOSIS_CODE, 0, 5, CERTIFICATE_ID_2, PATIENT_ID_1, CERTIFICATE_ID_1, RelationKod.ERSATT);

        final var populateFiltersRequest = getPopulateFiltersRequest(CARE_UNIT_ID, 5, null);
        final var expectedResult = List.of(new DiagnosKapitel(DIAGNOSIS_CHAPTER));

        final var response = getResponsePopulateFiltersDiagnosis(populateFiltersRequest);

        assertEquals(expectedResult, response);
    }

    @Test
    public void shouldNotReturnListOfDiagnosisForComplementedSickLeaves() {
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, EMPLOYEE_HSA_ID, EMPLOYEE_NAME,
            ANOTHER_DIAGNOSIS_CODE,
            0, 5, CERTIFICATE_ID_1, PATIENT_ID_1, null, null);
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, ANOTHER_EMPLOYEE_HSA_ID, ANOTHER_EMPLOYEE_NAME,
            DIAGNOSIS_CODE, 0, 5, CERTIFICATE_ID_2, PATIENT_ID_1, CERTIFICATE_ID_1, RelationKod.KOMPLT);

        final var populateFiltersRequest = getPopulateFiltersRequest(CARE_UNIT_ID, 5, null);
        final var expectedResult = List.of(new DiagnosKapitel(DIAGNOSIS_CHAPTER));

        final var response = getResponsePopulateFiltersDiagnosis(populateFiltersRequest);

        assertEquals(expectedResult, response);
    }

    @Test
    public void shouldReturnListOfDoctorsFilteredOnUnitId() {
        registerCertificateWithParametersDoctorAndDiagnosis(CARE_UNIT_ID, CARE_PROVIDER_ID, EMPLOYEE_HSA_ID, EMPLOYEE_NAME, DIAGNOSIS_CODE,
            0, 5, CERTIFICATE_ID_1, PATIENT_ID_1, null, null);
        registerCertificateWithParametersDoctorAndDiagnosis(ANOTHER_CARE_UNIT_ID, CARE_PROVIDER_ID, ANOTHER_EMPLOYEE_HSA_ID,
            ANOTHER_EMPLOYEE_NAME, DIAGNOSIS_CODE, 0, 5, CERTIFICATE_ID_2, PATIENT_ID_2, null, null);

        final var populateFiltersRequest = getPopulateFiltersRequest(CARE_UNIT_ID, 5, UNIT_ID);
        final var expectedResult = List.of(Lakare.create(EMPLOYEE_HSA_ID, EMPLOYEE_NAME));

        final var response = getResponsePopulateFiltersActiveDoctors(populateFiltersRequest);

        assertEquals(expectedResult, response);
    }

    private SjukfallEnhet getExpectSjukfallEnhet(List<Integer> fromDays, List<Integer> toDays, List<String> certificateIds,
        String activeCertificate,
        String patientId, boolean recentlyCompleted, Integer maxGap) {
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
        sjukfallEnhet.setStart(LocalDate.now().plusDays(fromDays.get(0)));
        sjukfallEnhet.setSlut(LocalDate.now().plusDays(toDays.get(toDays.size() - 1)));
        sjukfallEnhet.setIntyg(certificateIds.size());
        sjukfallEnhet.setIntygLista(certificateIds);
        sjukfallEnhet.setAktivIntygsId(activeCertificate);
        sjukfallEnhet.setGrader(List.of(75));
        sjukfallEnhet.setDagar(
            maxGap > 0 ? getEffectiveNumberOfSickDaysByIntyg(fromDays, toDays, maxGap) : toDays.get(0) - fromDays.get(0) + 1);
        if (!recentlyCompleted) {
            sjukfallEnhet.setAktivGrad(75);
        }

        return sjukfallEnhet;
    }

    private int getEffectiveNumberOfSickDaysByIntyg(List<Integer> fromDays, List<Integer> toDays, int gap) {
        final var sjukfallIntygs = new ArrayList<SjukfallIntyg>();
        sjukfallIntygs.add(
            createIntyg(
                LocalDateTime.parse(REQUEST_LISJP_SIGN_DATE), gap,
                new LocalDateInterval(LocalDate.now().plusDays(fromDays.get(0)), LocalDate.now().plusDays(toDays.get(0)))));
        sjukfallIntygs.add(
            createIntyg(
                LocalDateTime.parse(REQUEST_LISJP_SIGN_DATE), gap,
                new LocalDateInterval(LocalDate.now().plusDays(fromDays.get(1)), LocalDate.now().plusDays(toDays.get(1)))));
        return SjukfallLangdCalculator.getEffectiveNumberOfSickDaysByIntyg(sjukfallIntygs, gap);
    }

    private SjukfallIntyg createIntyg(LocalDateTime signeringsTidpunkt, int gap, LocalDateInterval... intervals) {
        final List<Formaga> formagor = new ArrayList<>();

        for (LocalDateInterval i : intervals) {
            int nedsattning = 100;
            formagor.add(new Formaga(i.getStartDate(), i.getEndDate(), nedsattning));
        }

        IntygData intygData = new IntygData();
        intygData.setFormagor(formagor);
        intygData.setSigneringsTidpunkt(signeringsTidpunkt);

        SjukfallIntyg.SjukfallIntygBuilder builder = new SjukfallIntyg.SjukfallIntygBuilder(intygData, LocalDate.now(), gap);
        return builder.build();
    }

    private String formatPatientId(String patientId) {
        return patientId.substring(0, 8) + "-" + patientId.substring(8);
    }

    private List<SjukfallEnhet> getResponseActiveSickLeaves(SickLeaveRequestDTO sickLeaveRequestDTO) {
        return given()
            .contentType(ContentType.JSON)
            .body(sickLeaveRequestDTO)
            .expect()
            .statusCode(STATUS_CODE)
            .when()
            .post(API_ENDPOINT_ACTIVE_SICK_LEAVE_FOR_UNIT)
            .then()
            .extract()
            .response()
            .as(SickLeaveResponseDTO.class, getObjectMapperForDeserialization()).getContent();
    }

    private List<Lakare> getResponsePopulateFiltersActiveDoctors(PopulateFiltersRequestDTO populateFiltersRequestDTO) {
        return given()
            .contentType(ContentType.JSON)
            .body(populateFiltersRequestDTO)
            .expect()
            .statusCode(STATUS_CODE)
            .when()
            .post(API_ENDPOINT_POPULATE_FILTERS)
            .then()
            .extract()
            .response()
            .as(PopulateFiltersResponseDTO.class, getObjectMapperForDeserialization()).getActiveDoctors();
    }

    private List<DiagnosKapitel> getResponsePopulateFiltersDiagnosis(PopulateFiltersRequestDTO populateFiltersRequestDTO) {
        return given()
            .contentType(ContentType.JSON)
            .body(populateFiltersRequestDTO)
            .expect()
            .statusCode(STATUS_CODE)
            .when()
            .post(API_ENDPOINT_POPULATE_FILTERS)
            .then()
            .extract()
            .response()
            .as(PopulateFiltersResponseDTO.class, getObjectMapperForDeserialization()).getDiagnosisChapters();
    }

    private SickLeaveRequestDTO getSickLeaveRequest(String unitId, String careUnitId, String doctorId, int maxCertificateGap,
        int maxDaysSinceSickLeaveCompleted, Integer fromSickLeaveLength, Integer toSickLeaveLength, List<DiagnosKapitel> diagnosisChapter) {
        final var sickLeaveRequestDTO = new SickLeaveRequestDTO();
        sickLeaveRequestDTO.setUnitId(unitId);
        sickLeaveRequestDTO.setCareUnitId(careUnitId);
        sickLeaveRequestDTO.setDoctorIds(doctorId != null ? List.of(doctorId) : null);
        sickLeaveRequestDTO.setMaxCertificateGap(maxCertificateGap);
        sickLeaveRequestDTO.setMaxDaysSinceSickLeaveCompleted(maxDaysSinceSickLeaveCompleted);
        sickLeaveRequestDTO.setToSickLeaveLength(toSickLeaveLength);
        sickLeaveRequestDTO.setFromSickLeaveLength(fromSickLeaveLength);
        sickLeaveRequestDTO.setDiagnosisChapters(diagnosisChapter);
        return sickLeaveRequestDTO;
    }

    private PopulateFiltersRequestDTO getPopulateFiltersRequest(String careUnitId, int maxDaysSinceSickLeaveCompleted, String unitId) {
        final var populateFiltersRequest = new PopulateFiltersRequestDTO();
        populateFiltersRequest.setCareUnitId(careUnitId);
        populateFiltersRequest.setMaxDaysSinceSickLeaveCompleted(maxDaysSinceSickLeaveCompleted);
        populateFiltersRequest.setUnitId(unitId);
        return populateFiltersRequest;
    }

    private void registerCertificateWithParameters(String certificateId, String patientId,
        int fromDays, int toDays, String doctorId, String careProviderId, String unitId, String doctorName, RelationKod relationKod,
        String relationsId, String diagnosisCode) {
        final var sickLeaveITConfigProvider = new SickLeaveITConfigProvider(careProviderId, certificateId, patientId, fromDays, toDays,
            unitId, doctorId, doctorName, relationsId, relationKod, diagnosisCode);
        IntegrationTestUtil.registerCertificateWithSickLeaveConfig(sickLeaveITConfigProvider);
    }

    private void registerCertificateWithParametersDoctorAndDiagnosis(String careUnitId, String careProviderId, String doctorId,
        String doctorName, String diagnosisCode,
        int fromDaysRelativeToNow, int toDaysRelativeToNow, String certificateId, String patientId, String relationsId,
        RelationKod relationKod) {
        IntegrationTestUtil.registerCertificateWithParametersDoctorAndDiagnosis(careUnitId, careProviderId, certificateId, patientId,
            doctorId, doctorName,
            diagnosisCode, fromDaysRelativeToNow, toDaysRelativeToNow, relationsId, relationKod);
    }

    private ObjectMapper getObjectMapperForDeserialization() {
        return new Jackson2Mapper(((type, charset) -> new CustomObjectMapper()));
    }

    public static class SickLeaveITConfigProvider {

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
        private final String diagnosisCode;

        SickLeaveITConfigProvider(String careProviderId, String certificateId, String patientId, int fromDays, int toDays, String unitId,
            String doctorId, String doctorName, String relationsId, RelationKod relationKod, String diagnosisCode) {
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
            this.diagnosisCode = diagnosisCode;
        }

        public String getDiagnosisCode() {
            return diagnosisCode;
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
