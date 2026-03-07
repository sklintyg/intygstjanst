package se.inera.intyg.intygstjanst.integration.intygproxyservice.pu.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PersonRequestDTO {

    String personId;
    boolean queryCache;

}