package se.inera.intyg.intygstjanst.web.csintegration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SickLeaveCertificatesRequestDTO.SickLeaveCertificatesRequestDTOBuilder;
import se.inera.intyg.intygstjanst.web.service.dto.PersonIdDTO;

@JsonDeserialize(builder = SickLeaveCertificatesRequestDTOBuilder.class)
@Value
@Builder
public class SickLeaveCertificatesRequestDTO {

    PersonIdDTO personId;
    List<String> certificateTypes;
    LocalDate signedFrom;
    LocalDate signedTo;
    List<String> issuedByUnitIds;

    @JsonPOJOBuilder(withPrefix = "")
    public static class SickLeaveCertificatesRequestDTOBuilder {

    }
}
