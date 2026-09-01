/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.application.binarycertificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificatePersonIdType.COORDINATION_NUMBER;
import static se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificatePersonIdType.PERSONAL_IDENTITY_NUMBER;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.Constants;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateCareProvider;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateCode;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateMetadataDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificatePatient;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateRelation;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateResponseDTO;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateStaff;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateUnit;

@ExtendWith(MockitoExtension.class)
class BinaryCertificateResponseConverterTest {

  private static final String CERTIFICATE_ID = "cert-123";
  private static final String UNIT_ID = "unit-abc";
  private static final String UNIT_NAME = "Unit Name";
  private static final String UNIT_ADDRESS = "Street 1";
  private static final String UNIT_ZIP = "12345";
  private static final String UNIT_CITY = "City";
  private static final String UNIT_PHONE = "0701234567";
  private static final String UNIT_EMAIL = "unit@example.com";
  private static final String UNIT_WORKPLACE_CODE = "1234";
  private static final String CARE_PROVIDER_ID = "cp-id";
  private static final String CARE_PROVIDER_NAME = "Care Provider";
  private static final String PERSON_ID = "hsa-person-1";
  private static final String FULL_NAME = "Doktor Doktorsson";
  private static final String PATIENT_ID = "191212121212";
  private static final String TYPE_CODE = "FK7210";
  private static final String TYPE_CODE_SYSTEM = "1.2.752.116.1.1.1.1.3";
  private static final String TYPE_DISPLAY_NAME = "Läkarintyg";
  private static final String VERSION = "1.0";
  private static final String RECIPIENT_ID = "FKASSA";
  private static final LocalDateTime SIGNED_AT = LocalDateTime.of(2024, Month.JANUARY, 15, 10, 0);
  private static final LocalDateTime SENT_AT = LocalDateTime.of(2024, Month.JANUARY, 16, 9, 0);
  private static final LocalDateTime REVOKED_AT = LocalDateTime.of(2024, Month.JANUARY, 17, 8, 0);
  private static final String PARENT_CERT_ID = "parent-cert-456";
  private static final String PARENT_UNIT_ID = "parent-unit-789";
  private static final byte[] PDF_DATA = new byte[] {1, 2, 3};

  @InjectMocks private BinaryCertificateResponseConverter converter;

  private BinaryCertificateUnit unit;
  private BinaryCertificateCode titleCode;
  private BinaryCertificateCode licenceCode;
  private BinaryCertificateStaff staff;
  private BinaryCertificatePatient patient;
  private BinaryCertificateCode typeCode;

  @BeforeEach
  void setUp() {
    BinaryCertificateCareProvider careProvider =
        BinaryCertificateCareProvider.builder()
            .unitId(CARE_PROVIDER_ID)
            .unitName(CARE_PROVIDER_NAME)
            .build();

    unit =
        BinaryCertificateUnit.builder()
            .unitId(UNIT_ID)
            .unitName(UNIT_NAME)
            .address(UNIT_ADDRESS)
            .zipCode(UNIT_ZIP)
            .city(UNIT_CITY)
            .phoneNumber(UNIT_PHONE)
            .email(UNIT_EMAIL)
            .workplaceCode(UNIT_WORKPLACE_CODE)
            .careProvider(careProvider)
            .build();

    titleCode =
        BinaryCertificateCode.builder()
            .code("TC")
            .codeSystem("title-system")
            .displayName("Title Display")
            .build();

    licenceCode =
        BinaryCertificateCode.builder()
            .code("LC")
            .codeSystem("licence-system")
            .displayName("Licence Display")
            .build();

    staff =
        BinaryCertificateStaff.builder()
            .personId(PERSON_ID)
            .fullName(FULL_NAME)
            .titles(List.of(titleCode))
            .licences(List.of(licenceCode))
            .specialities(List.of("Cardiology"))
            .unit(unit)
            .build();

    patient =
        BinaryCertificatePatient.builder()
            .patientId(PATIENT_ID)
            .type(PERSONAL_IDENTITY_NUMBER)
            .build();

    typeCode =
        BinaryCertificateCode.builder()
            .code(TYPE_CODE)
            .codeSystem(TYPE_CODE_SYSTEM)
            .displayName(TYPE_DISPLAY_NAME)
            .build();
  }

  private BinaryCertificateMetadataDTO.BinaryCertificateMetadataDTOBuilder baseMetadataBuilder() {
    return BinaryCertificateMetadataDTO.builder()
        .certificateId(CERTIFICATE_ID)
        .type(typeCode)
        .version(VERSION)
        .recipientId(RECIPIENT_ID)
        .signedAt(SIGNED_AT)
        .patient(patient)
        .issuedBy(staff);
  }

  private BinaryCertificateResponseDTO baseDto() {
    return BinaryCertificateResponseDTO.builder()
        .metadata(baseMetadataBuilder().build())
        .pdfData(PDF_DATA)
        .build();
  }

  @Test
  void shallReturnNullWhenDtoIsNull() {
    assertNull(converter.toResponse(null));
  }

  @Test
  void shallReturnNonNullResponseWhenDtoIsNotNull() {
    assertNotNull(converter.toResponse(baseDto()));
  }

  @Nested
  class IntygId {

    @Test
    void shallIncludeCertificateIdExtension() {
      final var result = converter.toResponse(baseDto());
      assertEquals(CERTIFICATE_ID, result.getBinartIntyg().getIntygsId().getExtension());
    }

    @Test
    void shallIncludeUnitIdAsRoot() {
      final var result = converter.toResponse(baseDto());
      assertEquals(UNIT_ID, result.getBinartIntyg().getIntygsId().getRoot());
    }
  }

  @Nested
  class TypAvIntyg {

    @Test
    void shallIncludeTypeCode() {
      final var result = converter.toResponse(baseDto());
      assertEquals(TYPE_CODE, result.getBinartIntyg().getTyp().getCode());
    }

    @Test
    void shallIncludeTypeCodeSystem() {
      final var result = converter.toResponse(baseDto());
      assertEquals(TYPE_CODE_SYSTEM, result.getBinartIntyg().getTyp().getCodeSystem());
    }

    @Test
    void shallIncludeTypeDisplayName() {
      final var result = converter.toResponse(baseDto());
      assertEquals(TYPE_DISPLAY_NAME, result.getBinartIntyg().getTyp().getDisplayName());
    }
  }

  @Nested
  class Version {

    @Test
    void shallIncludeVersion() {
      final var result = converter.toResponse(baseDto());
      assertEquals(VERSION, result.getBinartIntyg().getVersion());
    }
  }

  @Nested
  class SignedAt {

    @Test
    void shallIncludeSigneringstidpunkt() {
      final var result = converter.toResponse(baseDto());
      assertEquals(SIGNED_AT, result.getBinartIntyg().getSigneringstidpunkt());
    }
  }

  @Nested
  class PatientData {

    @Test
    void shallIncludePatientId() {
      final var result = converter.toResponse(baseDto());
      assertEquals(PATIENT_ID, result.getBinartIntyg().getPatient().getPersonId().getExtension());
    }

    @Test
    void shallSetPersonIdRootToPersonIdOidForPersonalIdentityNumber() {
      final var result = converter.toResponse(baseDto());
      assertEquals(
          Constants.PERSON_ID_OID, result.getBinartIntyg().getPatient().getPersonId().getRoot());
    }

    @Test
    void shallSetPersonIdRootToSamordningIdOidForCoordinationNumber() {
      final var dto =
          BinaryCertificateResponseDTO.builder()
              .metadata(
                  baseMetadataBuilder()
                      .patient(
                          BinaryCertificatePatient.builder()
                              .patientId(PATIENT_ID)
                              .type(COORDINATION_NUMBER)
                              .build())
                      .build())
              .pdfData(PDF_DATA)
              .build();

      final var result = converter.toResponse(dto);
      assertEquals(
          Constants.SAMORDNING_ID_OID,
          result.getBinartIntyg().getPatient().getPersonId().getRoot());
    }
  }

  @Nested
  class HosPersonalData {

    @Test
    void shallIncludePersonId() {
      final var result = converter.toResponse(baseDto());
      assertEquals(PERSON_ID, result.getBinartIntyg().getSkapadAv().getPersonalId().getExtension());
    }

    @Test
    void shallIncludePersonIdRoot() {
      final var result = converter.toResponse(baseDto());
      assertEquals(
          Constants.HSA_ID_OID, result.getBinartIntyg().getSkapadAv().getPersonalId().getRoot());
    }

    @Test
    void shallIncludeFullName() {
      final var result = converter.toResponse(baseDto());
      assertEquals(FULL_NAME, result.getBinartIntyg().getSkapadAv().getFullstandigtNamn());
    }

    @Test
    void shallIncludeForskrivarkod() {
      final var result = converter.toResponse(baseDto());
      assertEquals("0000000", result.getBinartIntyg().getSkapadAv().getForskrivarkod());
    }

    @Test
    void shallIncludeTitleCode() {
      final var result = converter.toResponse(baseDto());
      assertEquals(
          titleCode.getCode(),
          result.getBinartIntyg().getSkapadAv().getBefattning().getFirst().getCode());
    }

    @Test
    void shallIncludeTitleCodeSystem() {
      final var result = converter.toResponse(baseDto());
      assertEquals(
          titleCode.getCodeSystem(),
          result.getBinartIntyg().getSkapadAv().getBefattning().getFirst().getCodeSystem());
    }

    @Test
    void shallIncludeTitleDisplayName() {
      final var result = converter.toResponse(baseDto());
      assertEquals(
          titleCode.getDisplayName(),
          result.getBinartIntyg().getSkapadAv().getBefattning().getFirst().getDisplayName());
    }

    @Test
    void shallReturnEmptyTitlesWhenNull() {
      final var dto =
          BinaryCertificateResponseDTO.builder()
              .metadata(
                  baseMetadataBuilder()
                      .issuedBy(
                          BinaryCertificateStaff.builder()
                              .personId(PERSON_ID)
                              .fullName(FULL_NAME)
                              .titles(null)
                              .licences(List.of())
                              .specialities(List.of())
                              .unit(unit)
                              .build())
                      .build())
              .pdfData(PDF_DATA)
              .build();

      final var result = converter.toResponse(dto);
      assertEquals(0, result.getBinartIntyg().getSkapadAv().getBefattning().size());
    }

    @Test
    void shallIncludeLicenceCode() {
      final var result = converter.toResponse(baseDto());
      assertEquals(
          licenceCode.getCode(),
          result.getBinartIntyg().getSkapadAv().getLegitimeratYrke().getFirst().getCode());
    }

    @Test
    void shallIncludeLicenceCodeSystem() {
      final var result = converter.toResponse(baseDto());
      assertEquals(
          licenceCode.getCodeSystem(),
          result.getBinartIntyg().getSkapadAv().getLegitimeratYrke().getFirst().getCodeSystem());
    }

    @Test
    void shallIncludeLicenceDisplayName() {
      final var result = converter.toResponse(baseDto());
      assertEquals(
          licenceCode.getDisplayName(),
          result.getBinartIntyg().getSkapadAv().getLegitimeratYrke().getFirst().getDisplayName());
    }

    @Test
    void shallReturnEmptyLicencesWhenNull() {
      final var dto =
          BinaryCertificateResponseDTO.builder()
              .metadata(
                  baseMetadataBuilder()
                      .issuedBy(
                          BinaryCertificateStaff.builder()
                              .personId(PERSON_ID)
                              .fullName(FULL_NAME)
                              .titles(List.of())
                              .licences(null)
                              .specialities(List.of())
                              .unit(unit)
                              .build())
                      .build())
              .pdfData(PDF_DATA)
              .build();

      final var result = converter.toResponse(dto);
      assertEquals(0, result.getBinartIntyg().getSkapadAv().getLegitimeratYrke().size());
    }

    @Test
    void shallIncludeSpecialityDisplayName() {
      final var result = converter.toResponse(baseDto());
      assertEquals(
          "Cardiology",
          result
              .getBinartIntyg()
              .getSkapadAv()
              .getSpecialistkompetens()
              .getFirst()
              .getDisplayName());
    }

    @Test
    void shallReturnEmptySpecialitiesWhenNull() {
      final var dto =
          BinaryCertificateResponseDTO.builder()
              .metadata(
                  baseMetadataBuilder()
                      .issuedBy(
                          BinaryCertificateStaff.builder()
                              .personId(PERSON_ID)
                              .fullName(FULL_NAME)
                              .titles(List.of())
                              .licences(List.of())
                              .specialities(null)
                              .unit(unit)
                              .build())
                      .build())
              .pdfData(PDF_DATA)
              .build();

      final var result = converter.toResponse(dto);
      assertEquals(0, result.getBinartIntyg().getSkapadAv().getSpecialistkompetens().size());
    }
  }

  @Nested
  class EnhetData {

    @Test
    void shallIncludeUnitId() {
      final var result = converter.toResponse(baseDto());
      assertEquals(
          UNIT_ID, result.getBinartIntyg().getSkapadAv().getEnhet().getEnhetsId().getExtension());
    }

    @Test
    void shallIncludeUnitIdRoot() {
      final var result = converter.toResponse(baseDto());
      assertEquals(
          Constants.HSA_ID_OID,
          result.getBinartIntyg().getSkapadAv().getEnhet().getEnhetsId().getRoot());
    }

    @Test
    void shallIncludeUnitName() {
      final var result = converter.toResponse(baseDto());
      assertEquals(UNIT_NAME, result.getBinartIntyg().getSkapadAv().getEnhet().getEnhetsnamn());
    }

    @Test
    void shallIncludeAddress() {
      final var result = converter.toResponse(baseDto());
      assertEquals(UNIT_ADDRESS, result.getBinartIntyg().getSkapadAv().getEnhet().getPostadress());
    }

    @Test
    void shallIncludeZipCode() {
      final var result = converter.toResponse(baseDto());
      assertEquals(UNIT_ZIP, result.getBinartIntyg().getSkapadAv().getEnhet().getPostnummer());
    }

    @Test
    void shallIncludeCity() {
      final var result = converter.toResponse(baseDto());
      assertEquals(UNIT_CITY, result.getBinartIntyg().getSkapadAv().getEnhet().getPostort());
    }

    @Test
    void shallIncludePhoneNumber() {
      final var result = converter.toResponse(baseDto());
      assertEquals(UNIT_PHONE, result.getBinartIntyg().getSkapadAv().getEnhet().getTelefonnummer());
    }

    @Test
    void shallIncludeEmail() {
      final var result = converter.toResponse(baseDto());
      assertEquals(UNIT_EMAIL, result.getBinartIntyg().getSkapadAv().getEnhet().getEpost());
    }

    @Test
    void shallIncludeWorkplaceCode() {
      final var result = converter.toResponse(baseDto());
      assertEquals(
          UNIT_WORKPLACE_CODE,
          result.getBinartIntyg().getSkapadAv().getEnhet().getArbetsplatskod().getExtension());
    }

    @Test
    void shallIncludeWorkplaceCodeRoot() {
      final var result = converter.toResponse(baseDto());
      assertEquals(
          Constants.ARBETSPLATS_KOD_OID,
          result.getBinartIntyg().getSkapadAv().getEnhet().getArbetsplatskod().getRoot());
    }

    @Test
    void shallIncludeCareProviderId() {
      final var result = converter.toResponse(baseDto());
      assertEquals(
          CARE_PROVIDER_ID,
          result
              .getBinartIntyg()
              .getSkapadAv()
              .getEnhet()
              .getVardgivare()
              .getVardgivareId()
              .getExtension());
    }

    @Test
    void shallIncludeCareProviderName() {
      final var result = converter.toResponse(baseDto());
      assertEquals(
          CARE_PROVIDER_NAME,
          result.getBinartIntyg().getSkapadAv().getEnhet().getVardgivare().getVardgivarnamn());
    }
  }

  @Nested
  class Statuses {

    @Test
    void shallAlwaysIncludeReceivedStatus() {
      final var result = converter.toResponse(baseDto());
      final var statuses = result.getBinartIntyg().getStatus();
      final var received =
          statuses.stream()
              .filter(s -> CertificateState.RECEIVED.name().equals(s.getStatus().getCode()))
              .findFirst();
      assertNotNull(received.orElse(null));
    }

    @Test
    void shallSetReceivedTimestampToSignedAt() {
      final var result = converter.toResponse(baseDto());
      final var received =
          result.getBinartIntyg().getStatus().stream()
              .filter(s -> CertificateState.RECEIVED.name().equals(s.getStatus().getCode()))
              .findFirst()
              .orElseThrow();
      assertEquals(SIGNED_AT, received.getTidpunkt());
    }

    @Test
    void shallNotIncludeSentStatusWhenSentAtIsNull() {
      final var result = converter.toResponse(baseDto());
      final var hasSent =
          result.getBinartIntyg().getStatus().stream()
              .anyMatch(s -> CertificateState.SENT.name().equals(s.getStatus().getCode()));
      assertFalse(hasSent);
    }

    @Test
    void shallIncludeSentStatusWhenSentAtIsSet() {
      final var dto =
          BinaryCertificateResponseDTO.builder()
              .metadata(baseMetadataBuilder().sentAt(SENT_AT).build())
              .pdfData(PDF_DATA)
              .build();

      final var result = converter.toResponse(dto);
      final var sentStatus =
          result.getBinartIntyg().getStatus().stream()
              .filter(s -> CertificateState.SENT.name().equals(s.getStatus().getCode()))
              .findFirst()
              .orElse(null);
      assertNotNull(sentStatus);
      assertEquals(SENT_AT, sentStatus.getTidpunkt());
    }

    @Test
    void shallNotIncludeCancelledStatusWhenRevokedAtIsNull() {
      final var result = converter.toResponse(baseDto());
      final var hasCancelled =
          result.getBinartIntyg().getStatus().stream()
              .anyMatch(s -> CertificateState.CANCELLED.name().equals(s.getStatus().getCode()));
      assertFalse(hasCancelled);
    }

    @Test
    void shallIncludeCancelledStatusWhenRevokedAtIsSet() {
      final var dto =
          BinaryCertificateResponseDTO.builder()
              .metadata(baseMetadataBuilder().revokedAt(REVOKED_AT).build())
              .pdfData(PDF_DATA)
              .build();

      final var result = converter.toResponse(dto);
      final var cancelledStatus =
          result.getBinartIntyg().getStatus().stream()
              .filter(s -> CertificateState.CANCELLED.name().equals(s.getStatus().getCode()))
              .findFirst()
              .orElse(null);
      assertNotNull(cancelledStatus);
      assertEquals(REVOKED_AT, cancelledStatus.getTidpunkt());
    }
  }

  @Nested
  class RelationData {

    @Test
    void shallIncludeRelationCertificateId() {
      final var dto =
          BinaryCertificateResponseDTO.builder()
              .metadata(
                  baseMetadataBuilder()
                      .parentRelation(
                          BinaryCertificateRelation.builder()
                              .certificateId(PARENT_CERT_ID)
                              .issuingUnitId(PARENT_UNIT_ID)
                              .type(CertificateRelationType.REPLACED)
                              .build())
                      .build())
              .pdfData(PDF_DATA)
              .build();

      final var result = converter.toResponse(dto);
      assertEquals(
          PARENT_CERT_ID,
          result.getBinartIntyg().getRelation().getFirst().getIntygsId().getExtension());
    }

    @Test
    void shallIncludeRelationIssuingUnitId() {
      final var dto =
          BinaryCertificateResponseDTO.builder()
              .metadata(
                  baseMetadataBuilder()
                      .parentRelation(
                          BinaryCertificateRelation.builder()
                              .certificateId(PARENT_CERT_ID)
                              .issuingUnitId(PARENT_UNIT_ID)
                              .type(CertificateRelationType.REPLACED)
                              .build())
                      .build())
              .pdfData(PDF_DATA)
              .build();

      final var result = converter.toResponse(dto);
      assertEquals(
          PARENT_UNIT_ID, result.getBinartIntyg().getRelation().getFirst().getIntygsId().getRoot());
    }

    @Test
    void shallMapReplacedRelationTypeToErsatt() {
      final var dto =
          BinaryCertificateResponseDTO.builder()
              .metadata(
                  baseMetadataBuilder()
                      .parentRelation(
                          BinaryCertificateRelation.builder()
                              .certificateId(PARENT_CERT_ID)
                              .issuingUnitId(PARENT_UNIT_ID)
                              .type(CertificateRelationType.REPLACED)
                              .build())
                      .build())
              .pdfData(PDF_DATA)
              .build();

      final var result = converter.toResponse(dto);
      assertEquals("ERSATT", result.getBinartIntyg().getRelation().getFirst().getTyp().getCode());
    }

    @Test
    void shallNotAddRelationWhenParentRelationIsNull() {
      final var result = converter.toResponse(baseDto());
      assertEquals(1, result.getBinartIntyg().getRelation().size());
      assertNull(result.getBinartIntyg().getRelation().getFirst());
    }
  }

  @Nested
  class BinaryData {

    @Test
    void shallIncludePdfData() {
      final var result = converter.toResponse(baseDto());
      assertEquals(PDF_DATA, result.getBinartIntyg().getBinartSvar().getData());
    }

    @Test
    void shallIncludeMimeType() {
      final var result = converter.toResponse(baseDto());
      assertEquals("application/pdf", result.getBinartIntyg().getBinartSvar().getContentType());
    }

    @Test
    void shallReturnNullBinaryDataWhenPdfDataIsNull() {
      final var dto =
          BinaryCertificateResponseDTO.builder()
              .metadata(baseMetadataBuilder().build())
              .pdfData(null)
              .build();

      final var result = converter.toResponse(dto);
      assertNull(result.getBinartIntyg().getBinartSvar());
    }
  }
}
