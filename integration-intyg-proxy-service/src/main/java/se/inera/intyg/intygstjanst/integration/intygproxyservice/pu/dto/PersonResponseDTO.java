package se.inera.intyg.intygstjanst.integration.intygproxyservice.pu.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.intygstjanst.integration.intygproxyservice.pu.dto.PersonResponseDTO.PersonResponseDTOBuilder;
import se.inera.intyg.intygstjanst.integration.pu.model.Person;
import se.inera.intyg.intygstjanst.integration.pu.model.PersonSvar.Status;

@JsonDeserialize(builder = PersonResponseDTOBuilder.class)
@Value
@Builder
public class PersonResponseDTO {

    Person person;
    Status status;

    @JsonPOJOBuilder(withPrefix = "")
    public static class PersonResponseDTOBuilder {

    }
}