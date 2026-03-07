package se.inera.intyg.intygstjanst.web.csintegration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.intygstjanst.web.csintegration.dto.ExportInternalResponseDTO.ExportInternalResponseDTOBuilder;

@JsonDeserialize(builder = ExportInternalResponseDTOBuilder.class)
@Value
@Builder
public class ExportInternalResponseDTO {

    List<ExportCertificateInternalResponseDTO> exports;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ExportInternalResponseDTOBuilder {

    }
}