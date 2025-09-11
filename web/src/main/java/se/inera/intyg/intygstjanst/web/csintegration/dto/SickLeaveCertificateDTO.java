package se.inera.intyg.intygstjanst.web.csintegration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@JsonDeserialize(builder = SickLeaveCertificateDTO.SickLeaveCertificateDTOBuilder.class)
@Value
@Builder
public class SickLeaveCertificateDTO {

  String id;
  String type;
  String signingDoctorId;
  String signingDoctorName;
  LocalDateTime signingDateTime;
  String careUnitId;
  String careUnitName;
  String careGiverId;
  String civicRegistrationNumber;
  String patientName;
  String diagnoseCode;
  String biDiagnoseCode1;
  String biDiagnoseCode2;
  String employment;
  Boolean deleted;
  List<SickLeaveCertificateWorkCapacityDTO> sjukfallCertificateWorkCapacity;
  boolean testCertificate;

  @JsonPOJOBuilder(withPrefix = "")
  public static class SickLeaveCertificateDTOBuilder {

  }
}