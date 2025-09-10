package se.inera.intyg.intygstjanst.web.service.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    private static final String QUESTION_DIAGNOSIS_ID = "6";
    private static final String QUESTION_SYSSELSATTNING_ID = "28";
    private static final String QUESTION_BEDOMNING_ID = "32";

    private CertificateToSickLeaveConverter converter;

    @BeforeEach
    void setUp() {
        this.converter = new CertificateToSickLeaveConverter();
    }

    @Test
    void shouldConvertId() {
        final var result = converter.convert(buildCertificate());
        assertEquals(CERT_ID, result.getId());
    }

    @Test
    void shouldConvertCareGiverId() {
        final var result = converter.convert(buildCertificate());
        assertEquals(CARE_GIVER_ID, result.getCareGiverId());
    }

    @Test
    void shouldConvertCareUnitId() {
        final var result = converter.convert(buildCertificate());
        assertEquals(CARE_UNIT_ID, result.getCareUnitId());
    }

    @Test
    void shouldConvertCareUnitName() {
        final var result = converter.convert(buildCertificate());
        assertEquals(CARE_UNIT_NAME, result.getCareUnitName());
    }

    @Test
    void shouldConvertType() {
        final var result = converter.convert(buildCertificate());
        assertEquals(CERT_TYPE_EXPECTED, result.getType());
    }

    @Test
    void shouldConvertCivicRegistrationNumber() {
        final var result = converter.convert(buildCertificate());
        assertEquals(PATIENT_PERSON_ID, result.getCivicRegistrationNumber());
    }

    @Test
    void shouldConvertSigningDoctorName() {
        final var result = converter.convert(buildCertificate());
        assertEquals(DOCTOR_FULL_NAME, result.getSigningDoctorName());
    }

    @Test
    void shouldConvertPatientName() {
        final var result = converter.convert(buildCertificate());
        assertEquals(PATIENT_NAME, result.getPatientName());
    }

    @Test
    void shouldConvertDiagnoseCode() {
        final var result = converter.convert(buildCertificate());
        assertEquals(DIAG_MAIN, result.getDiagnoseCode());
    }

    @Test
    void shouldConvertBiDiagnoseCode1() {
        final var result = converter.convert(buildCertificate());
        assertEquals(DIAG_BI_1, result.getBiDiagnoseCode1());
    }

    @Test
    void shouldConvertBiDiagnoseCode2() {
        final var result = converter.convert(buildCertificate());
        assertEquals(DIAG_BI_2, result.getBiDiagnoseCode2());
    }

    @Test
    void shouldConvertSigningDoctorId() {
        final var result = converter.convert(buildCertificate());
        assertEquals(DOCTOR_PERSON_ID, result.getSigningDoctorId());
    }

    @Test
    void shouldConvertSigningDateTime() {
        final var result = converter.convert(buildCertificate());
        assertEquals(SIGNED_AT, result.getSigningDateTime());
    }

    @Test
    void shouldConvertDeleted() {
        final var result = converter.convert(buildCertificate());
        assertFalse(result.getDeleted());
    }

    @Test
    void shouldConvertEmployment() {
        final var result = converter.convert(buildCertificate());
        assertEquals(EMPLOYMENT_1 + ", " + EMPLOYMENT_2, result.getEmployment());
    }

    @Test
    void shouldConvertTestCertificate() {
        final var result = converter.convert(buildCertificate());
        assertFalse(result.isTestCertificate());
    }

    @Test
    void shouldConvertWorkCapacitySize() {
        final var result = converter.convert(buildCertificate());
        final var capacities = result.getSjukfallCertificateWorkCapacity();
        assertEquals(2, capacities.size());
    }

    @Test
    void shouldConvertFirstCapacityPercentage() {
        final var result = converter.convert(buildCertificate());
        final var capacities = result.getSjukfallCertificateWorkCapacity();
        assertEquals(100, capacities.getFirst().getCapacityPercentage());
    }

    @Test
    void shouldConvertFirstCapacityFromDate() {
        final var result = converter.convert(buildCertificate());
        final var capacities = result.getSjukfallCertificateWorkCapacity();
        assertEquals(WC1_FROM.toString(), capacities.getFirst().getFromDate());
    }

    @Test
    void shouldConvertFirstCapacityToDate() {
        final var result = converter.convert(buildCertificate());
        final var capacities = result.getSjukfallCertificateWorkCapacity();
        assertEquals(WC1_TO.toString(), capacities.getFirst().getToDate());
    }

    @Test
    void shouldConvertSecondCapacityPercentage() {
        final var result = converter.convert(buildCertificate());
        final var capacities = result.getSjukfallCertificateWorkCapacity();
        assertEquals(50, capacities.get(1).getCapacityPercentage());
    }

    @Test
    void shouldConvertSecondCapacityFromDate() {
        final var result = converter.convert(buildCertificate());
        final var capacities = result.getSjukfallCertificateWorkCapacity();
        assertEquals(WC2_FROM.toString(), capacities.get(1).getFromDate());
    }

    @Test
    void shouldConvertSecondCapacityToDate() {
        final var result = converter.convert(buildCertificate());
        final var capacities = result.getSjukfallCertificateWorkCapacity();
        assertEquals(WC2_TO.toString(), capacities.get(1).getToDate());
    }

    private static Certificate buildCertificate() {
        final Map<String, CertificateDataElement> certificateData = new HashMap<>();
        certificateData.put(QUESTION_DIAGNOSIS_ID, CertificateDataElement.builder()
            .value(CertificateDataValueDiagnosisList.builder()
                .list(
                    List.of(
                        CertificateDataValueDiagnosis.builder().code(DIAG_MAIN).build(),
                        CertificateDataValueDiagnosis.builder().code(DIAG_BI_1).build(),
                        CertificateDataValueDiagnosis.builder().code(DIAG_BI_2).build()))
                .build())
            .build()
        );
        certificateData.put(QUESTION_SYSSELSATTNING_ID, CertificateDataElement.builder()
            .value(CertificateDataValueCodeList.builder()
                .list(
                    List.of(
                        CertificateDataValueCode.builder().id(EMPLOYMENT_1).build(),
                        CertificateDataValueCode.builder().id(EMPLOYMENT_2).build()))
                .build())
            .build()
        );
        certificateData.put(QUESTION_BEDOMNING_ID, CertificateDataElement.builder()
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

        final Certificate certificate = CertificateBuilder.create().build();
        certificate.setMetadata(certificateMetaData);
        certificate.setData(certificateData);
        return certificate;
    }
}