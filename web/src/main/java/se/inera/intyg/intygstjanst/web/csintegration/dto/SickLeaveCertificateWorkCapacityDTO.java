package se.inera.intyg.intygstjanst.web.csintegration.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@JsonDeserialize(builder = SickLeaveCertificateWorkCapacityDTO.SickLeaveCertificateWorkCapacityDTOBuilder.class)
@Value
@Builder
public class SickLeaveCertificateWorkCapacityDTO {

  Integer capacityPercentage;
  String fromDate;
  String toDate;

  @JsonPOJOBuilder(withPrefix = "")
  public static class SickLeaveCertificateWorkCapacityDTOBuilder {

  }
}