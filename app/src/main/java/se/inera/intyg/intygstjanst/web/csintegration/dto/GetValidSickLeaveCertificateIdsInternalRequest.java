package se.inera.intyg.intygstjanst.web.csintegration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@JsonDeserialize(builder = GetValidSickLeaveCertificateIdsInternalRequest.GetValidSickLeaveCertificateIdsInternalRequestBuilder.class)
@Value
@Builder
public class GetValidSickLeaveCertificateIdsInternalRequest {

    List<String> certificateIds;

    @JsonPOJOBuilder(withPrefix = "")
    public static class GetValidSickLeaveCertificateIdsInternalRequestBuilder {

    }

}