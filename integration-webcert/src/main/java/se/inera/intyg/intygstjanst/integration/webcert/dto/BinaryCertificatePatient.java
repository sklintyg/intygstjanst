package se.inera.intyg.intygstjanst.integration.webcert.dto;

import lombok.Builder;
import lombok.Value;
import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificatePatient.BinaryCertificatePatientBuilder;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = BinaryCertificatePatientBuilder.class)
@Value
@Builder
public class BinaryCertificatePatient {

  String patientId;
  BinaryCertificatePersonIdType type;

  @JsonPOJOBuilder(withPrefix = "")
  public static class BinaryCertificatePatientBuilder {}
}
