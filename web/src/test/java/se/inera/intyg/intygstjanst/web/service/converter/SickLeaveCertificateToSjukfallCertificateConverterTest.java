package se.inera.intyg.intygstjanst.web.service.converter;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SickLeaveCertificateDTO;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SickLeaveCertificateWorkCapacityDTO;

class SickLeaveCertificateToSjukfallCertificateConverterTest {

  private static final String ID = "12345";
  private static final String TYPE = "TYPE_A";
  private static final String SIGNING_DOCTOR_ID = "ID";
  private static final String SIGNING_DOCTOR_NAME = "NAME";
  private static final LocalDateTime SIGNING_DATE_TIME = LocalDateTime.of(2025, 1, 1, 12, 0);
  private static final String CARE_UNIT_ID = "CAR_ID";
  private static final String CARE_UNIT_NAME = "CARE_UNIT";
  private static final String CARE_GIVER_ID = "CG_ID";
  private static final String CIVIC_REGISTRATION_NUMBER = "NUMBER";
  private static final String PATIENT_NAME = "PATIENT";
  private static final String DIAGNOSE_CODE = "DIA_CODE";
  private static final String BI_DIAGNOSE_CODE_1 = "BI_CODE1";
  private static final String BI_DIAGNOSE_CODE_2 = "BI_CODE2";
  private static final String EMPLOYMENT = "EMPLOYMENT";
  private static final boolean DELETED = false;
  private static final boolean TEST_CERTIFICATE = false;
  private SickLeaveCertificateDTO sickLeaveCertificate;
  private SickLeaveCertificateToSjukfallCertificateConverter converter;

  @BeforeEach
  void setUp() {

    sickLeaveCertificate = SickLeaveCertificateDTO.builder()
        .id(ID)
        .type(TYPE)
        .signingDoctorId(SIGNING_DOCTOR_ID)
        .signingDoctorName(SIGNING_DOCTOR_NAME)
        .signingDateTime(SIGNING_DATE_TIME)
        .careUnitId(CARE_UNIT_ID)
        .careUnitName(CARE_UNIT_NAME)
        .careGiverId(CARE_GIVER_ID)
        .civicRegistrationNumber(CIVIC_REGISTRATION_NUMBER)
        .patientName(PATIENT_NAME)
        .diagnoseCode(DIAGNOSE_CODE)
        .biDiagnoseCode1(BI_DIAGNOSE_CODE_1)
        .biDiagnoseCode2(BI_DIAGNOSE_CODE_2)
        .employment(EMPLOYMENT)
        .deleted(DELETED)
        .testCertificate(TEST_CERTIFICATE)
        .sjukfallCertificateWorkCapacity(List.of(
            SickLeaveCertificateWorkCapacityDTO.builder()
                .fromDate("2024-01-01")
                .toDate("2024-01-15")
                .capacityPercentage(50)
            .build()))
        .build();

    converter = new SickLeaveCertificateToSjukfallCertificateConverter();
  }

  @Test
  void shallThrowIfInputIsNull() {
    sickLeaveCertificate = null;
    assertThrows(IllegalStateException.class, () -> converter.convert(sickLeaveCertificate));
  }

  @Test
  void shallConvertWorkCapacitiesCorrectly() {
    final var result = converter.convert(sickLeaveCertificate);

    final var workCapacity = result.getSjukfallCertificateWorkCapacity().get(0);

    assertAll(
        () -> assertNotNull(result.getSjukfallCertificateWorkCapacity()),
        () -> assertEquals(1, result.getSjukfallCertificateWorkCapacity().size()),
        () -> assertEquals("2024-01-01", workCapacity.getFromDate()),
        () -> assertEquals("2024-01-15", workCapacity.getToDate()),
        () -> assertEquals(50, workCapacity.getCapacityPercentage()));
  }

  @Test
  void shallConvertIdCorrectly() {
    final var result = converter.convert(sickLeaveCertificate);
    assertEquals(ID, result.getId());
  }

  @Test
  void shallConvertTypeCorrectly() {
    final var result = converter.convert(sickLeaveCertificate);
    assertEquals(TYPE, result.getType());
  }

  @Test
  void shallConvertSigningDoctorIdCorrectly() {
    final var result = converter.convert(sickLeaveCertificate);
    assertEquals(SIGNING_DOCTOR_ID, result.getSigningDoctorId());
  }

  @Test
  void shallConvertSigningDoctorNameCorrectly() {
    final var result = converter.convert(sickLeaveCertificate);
    assertEquals(SIGNING_DOCTOR_NAME, result.getSigningDoctorName());
  }

  @Test
  void shallConvertSigningDateTimeCorrectly() {
    final var result = converter.convert(sickLeaveCertificate);
    assertEquals(SIGNING_DATE_TIME, result.getSigningDateTime());
  }

  @Test
  void shallConvertCareUnitIdCorrectly() {
    final var result = converter.convert(sickLeaveCertificate);
    assertEquals(CARE_UNIT_ID, result.getCareUnitId());
  }

  @Test
  void shallConvertCareUnitNameCorrectly() {
    final var result = converter.convert(sickLeaveCertificate);
    assertEquals(CARE_UNIT_NAME, result.getCareUnitName());
  }

  @Test
  void shallConvertCareGiverIdCorrectly() {
    final var result = converter.convert(sickLeaveCertificate);
    assertEquals(CARE_GIVER_ID, result.getCareGiverId());
  }

  @Test
  void shallConvertCivicRegistrationNumberCorrectly() {
    final var result = converter.convert(sickLeaveCertificate);
    assertEquals(CIVIC_REGISTRATION_NUMBER, result.getCivicRegistrationNumber());
  }

  @Test
  void shallConvertPatientNameCorrectly() {
    final var result = converter.convert(sickLeaveCertificate);
    assertEquals(PATIENT_NAME, result.getPatientName());
  }

  @Test
  void shallConvertDiagnoseCodeCorrectly() {
    final var result = converter.convert(sickLeaveCertificate);
    assertEquals(DIAGNOSE_CODE, result.getDiagnoseCode());
  }

  @Test
  void shallConvertBiDiagnoseCode1Correctly() {
    final var result = converter.convert(sickLeaveCertificate);
    assertEquals(BI_DIAGNOSE_CODE_1, result.getBiDiagnoseCode1());
  }

  @Test
  void shallConvertBiDiagnoseCode2Correctly() {
    final var result = converter.convert(sickLeaveCertificate);
    assertEquals(BI_DIAGNOSE_CODE_2, result.getBiDiagnoseCode2());
  }

  @Test
  void shallConvertEmploymentCorrectly() {
    final var result = converter.convert(sickLeaveCertificate);
    assertEquals(EMPLOYMENT, result.getEmployment());
  }

  @Test
  void shallConvertDeletedCorrectly() {
    final var result = converter.convert(sickLeaveCertificate);
    assertEquals(DELETED, result.getDeleted());
  }

  @Test
  void shallConvertTestCertificateCorrectly() {
    final var result = converter.convert(sickLeaveCertificate);
    assertEquals(TEST_CERTIFICATE, result.isTestCertificate());
  }

}
