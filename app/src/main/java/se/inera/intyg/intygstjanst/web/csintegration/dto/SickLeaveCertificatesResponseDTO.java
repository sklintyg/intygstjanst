package se.inera.intyg.intygstjanst.web.csintegration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.infra.certificate.dto.SickLeaveCertificate;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SickLeaveCertificatesResponseDTO.SickLeaveCertificatesResponseDTOBuilder;

@JsonDeserialize(builder = SickLeaveCertificatesResponseDTOBuilder.class)
@Value
@Builder
public class SickLeaveCertificatesResponseDTO {

    List<SickLeaveCertificate> certificates;

    @JsonPOJOBuilder(withPrefix = "")
    public static class SickLeaveCertificatesResponseDTOBuilder {

    }
}
