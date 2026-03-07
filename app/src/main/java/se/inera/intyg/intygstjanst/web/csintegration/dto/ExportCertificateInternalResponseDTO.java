package se.inera.intyg.intygstjanst.web.csintegration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.intygstjanst.web.csintegration.dto.ExportCertificateInternalResponseDTO.ExportCertificateInternalResponseDTOBuilder;

@JsonDeserialize(builder = ExportCertificateInternalResponseDTOBuilder.class)
@Value
@Builder
public class ExportCertificateInternalResponseDTO {

    String certificateId;
    String xml;
    boolean revoked;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ExportCertificateInternalResponseDTOBuilder {

    }
}