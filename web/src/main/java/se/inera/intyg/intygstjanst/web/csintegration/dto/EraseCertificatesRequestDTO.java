package se.inera.intyg.intygstjanst.web.csintegration.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.intygstjanst.web.csintegration.dto.EraseCertificatesRequestDTO.EraseCertificatesRequestDTOBuilder;

@JsonDeserialize(builder = EraseCertificatesRequestDTOBuilder.class)
@Value
@Builder
public class EraseCertificatesRequestDTO {

    String careProviderId;
    int erasePageSize;

    @JsonPOJOBuilder(withPrefix = "")
    public static class EraseCertificatesRequestDTOBuilder {

    }
}