package se.inera.intyg.intygstjanst.web.csintegration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SickLeaveResponseDTO.SickLeaveResponseDTOBuilder;

@JsonDeserialize(builder = SickLeaveResponseDTOBuilder.class)
@Value
@Builder
public class SickLeaveResponseDTO {

  boolean available;
  SjukfallCertificate sickLeaveCertificate;

  @JsonPOJOBuilder(withPrefix = "")
  public static class SickLeaveResponseDTOBuilder {

  }

}
