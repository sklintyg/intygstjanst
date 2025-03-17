package se.inera.intyg.intygstjanst.web.csintegration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.intygstjanst.web.csintegration.dto.TotalExportsInternalResponseDTO.TotalExportsInternalResponseDTOBuilder;

@JsonDeserialize(builder = TotalExportsInternalResponseDTOBuilder.class)
@Value
@Builder
public class TotalExportsInternalResponseDTO {

    long totalCertificates;
    long totalRevokedCertificates;

    @JsonPOJOBuilder(withPrefix = "")
    public static class TotalExportsInternalResponseDTOBuilder {

    }
}