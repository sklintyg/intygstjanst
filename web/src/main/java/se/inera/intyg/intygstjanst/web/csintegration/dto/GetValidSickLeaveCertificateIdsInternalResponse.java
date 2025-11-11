package se.inera.intyg.intygstjanst.web.csintegration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@JsonDeserialize(builder = GetValidSickLeaveCertificateIdsInternalResponse.GetValidSickLeaveCertificateIdsInternalResponseBuilder.class)
@Value
@Builder
public class GetValidSickLeaveCertificateIdsInternalResponse {

    List<String> certificateIds;

    @JsonPOJOBuilder(withPrefix = "")
    public static class GetValidSickLeaveCertificateIdsInternalResponseBuilder {

    }

}