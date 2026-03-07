package se.inera.intyg.intygstjanst.integration.intygproxyservice.pu.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PersonsRequestDTO {

    List<String> personIds;
    boolean queryCache;

}