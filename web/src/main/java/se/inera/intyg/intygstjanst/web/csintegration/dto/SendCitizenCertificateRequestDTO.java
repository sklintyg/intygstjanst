package se.inera.intyg.intygstjanst.web.csintegration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SendCitizenCertificateRequestDTO.SendCitizenCertificateRequestDTOBuilder;
import se.inera.intyg.intygstjanst.web.service.dto.PersonIdDTO;

@JsonDeserialize(builder = SendCitizenCertificateRequestDTOBuilder.class)
@Value
@Builder
public class SendCitizenCertificateRequestDTO {

    PersonIdDTO personId;

    @JsonPOJOBuilder(withPrefix = "")
    public static class SendCitizenCertificateRequestDTOBuilder {

    }
}