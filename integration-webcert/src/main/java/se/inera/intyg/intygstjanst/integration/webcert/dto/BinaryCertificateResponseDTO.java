package se.inera.intyg.intygstjanst.integration.webcert.dto;

import lombok.Builder;
import lombok.Value;

import se.inera.intyg.intygstjanst.integration.webcert.dto.BinaryCertificateResponseDTO.BinaryCertificateResponseDTOBuilder;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = BinaryCertificateResponseDTOBuilder.class)
@Value
@Builder
public class BinaryCertificateResponseDTO {



  @JsonPOJOBuilder(withPrefix = "")
  public static class BinaryCertificateResponseDTOBuilder {

  }
}
