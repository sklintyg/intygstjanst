package se.inera.intyg.intygstjanst.web.service.converter;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateDataElement;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueCode;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueCodeList;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRange;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDateRangeList;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDiagnosis;
import se.inera.intyg.common.support.facade.model.value.CertificateDataValueDiagnosisList;


class CertificateToSickLeaveConverterTest {

    private static final String CERT_ID = "CERT_ID";
    private static final String CARE_GIVER_ID = "CARE_GIVER_ID";
    private static final String CARE_UNIT_ID = "CARE_UNIT_ID";
    private static final String CARE_UNIT_NAME = "CARE_UNIT_NAME";
    private static final String CERT_TYPE_INPUT = "fk7804"; // maps to lisjp
    private static final String CERT_TYPE_EXPECTED = "lisjp";
    private static final String PATIENT_PERSON_ID = "191212121212";
    private static final String PATIENT_NAME = "PATIENT NAME";
    private static final String DOCTOR_FULL_NAME = "DOCTOR FULL NAME";
    private static final String DOCTOR_PERSON_ID = "DOCTOR_PERSON_ID";
    private static final String DIAG_MAIN = "A01";
    private static final String DIAG_BI_1 = "B02";
    private static final String DIAG_BI_2 = "C03";
    private static final String EMPLOYMENT_1 = "EMP1";
    private static final String EMPLOYMENT_2 = "EMP2";
    private static final LocalDateTime SIGNED_AT = LocalDateTime.of(2025, 1, 15, 10, 30, 0);

    private static final String WC1_ID = "HELT_NEDSATT"; // 100
    private static final LocalDate WC1_FROM = LocalDate.of(2025, 1, 1);
    private static final LocalDate WC1_TO = LocalDate.of(2025, 1, 10);

    private static final String WC2_ID = "HALFTEN"; // 50
    private static final LocalDate WC2_FROM = LocalDate.of(2025, 1, 11);
    private static final LocalDate WC2_TO = LocalDate.of(2025, 1, 20);
  public static final String CARE_GIVER_NAME = "CARE_GIVER_NAME";

  @Test
    void convert_shouldMapAllFields() {
        final var converter = new CertificateToSickLeaveConverter();

        final Map<String, CertificateDataElement> certificateData = new HashMap<>();
        certificateData.put("6", CertificateDataElement.builder()
            .value(CertificateDataValueDiagnosisList.builder()
                .list(
                    List.of(
                        CertificateDataValueDiagnosis.builder().code(DIAG_MAIN).build(),
                        CertificateDataValueDiagnosis.builder().code(DIAG_BI_1).build(),
                        CertificateDataValueDiagnosis.builder().code(DIAG_BI_2).build()))
                .build())
            .build()
        );
        certificateData.put("28", CertificateDataElement.builder()
            .value(CertificateDataValueCodeList.builder()
                .list(
                    List.of(
                        CertificateDataValueCode.builder().id(EMPLOYMENT_1).build(),
                        CertificateDataValueCode.builder().id(EMPLOYMENT_2).build()))
                .build())
            .build()
        );
        certificateData.put("32", CertificateDataElement.builder()
            .value(CertificateDataValueDateRangeList.builder()
                .list(
                    List.of(
                        CertificateDataValueDateRange.builder().id(WC1_ID).from(WC1_FROM).to(WC1_TO).build(),
                        CertificateDataValueDateRange.builder().id(WC2_ID).from(WC2_FROM).to(WC2_TO).build()))
                .build())
            .build()
        );

        final var careProvider = Unit.builder()
          .unitId(CARE_GIVER_ID)
          .unitName(CARE_GIVER_NAME)
          .build();

        final var careUnit = Unit.builder()
          .unitId(CARE_UNIT_ID)
          .unitName(CARE_UNIT_NAME)
          .build();

        final var personId = PersonId.builder()
            .id(PATIENT_PERSON_ID)
            .build();

        final var patient = Patient.builder()
          .personId(personId)
          .fullName(PATIENT_NAME)
          .build();

        final var staff = Staff.builder()
            .personId(DOCTOR_PERSON_ID)
            .fullName(DOCTOR_FULL_NAME)
            .build();

      final var certificateMetaData = CertificateMetadata.builder()
          .id(CERT_ID)
          .type(CERT_TYPE_INPUT)
          .careProvider(careProvider)
          .careUnit(careUnit)
          .patient(patient)
          .issuedBy(staff)
          .signed(SIGNED_AT)
          .build();

        // Build certificate
        final Certificate certificate = CertificateBuilder.create().build();
        certificate.setMetadata(certificateMetaData);
        certificate.setData(certificateData);

        final var result = converter.convert(certificate);

        assertEquals(CERT_ID, result.getId());
        assertEquals(CARE_GIVER_ID, result.getCareGiverId());
        assertEquals(CARE_UNIT_ID, result.getCareUnitId());
        assertEquals(CARE_UNIT_NAME, result.getCareUnitName());
        assertEquals(CERT_TYPE_EXPECTED, result.getType());
        assertEquals(PATIENT_PERSON_ID, result.getCivicRegistrationNumber());
        assertEquals(DOCTOR_FULL_NAME, result.getSigningDoctorName());
        assertEquals(PATIENT_NAME, result.getPatientName());
        assertEquals(DIAG_MAIN, result.getDiagnoseCode());
        assertEquals(DIAG_BI_1, result.getBiDiagnoseCode1());
        assertEquals(DIAG_BI_2, result.getBiDiagnoseCode2());
        assertEquals(DOCTOR_PERSON_ID, result.getSigningDoctorId());
        assertEquals(SIGNED_AT, result.getSigningDateTime());
        assertFalse(result.getDeleted());
        assertEquals(EMPLOYMENT_1 + ", " + EMPLOYMENT_2, result.getEmployment());
        assertFalse(result.isTestCertificate());

        final var capacities = result.getSjukfallCertificateWorkCapacity();
        assertEquals(2, capacities.size());

        assertEquals(100, capacities.get(0).getCapacityPercentage());
        assertEquals(WC1_FROM.toString(), capacities.get(0).getFromDate());
        assertEquals(WC1_TO.toString(), capacities.get(0).getToDate());

        assertEquals(50, capacities.get(1).getCapacityPercentage());
        assertEquals(WC2_FROM.toString(), capacities.get(1).getFromDate());
        assertEquals(WC2_TO.toString(), capacities.get(1).getToDate());
    }
}