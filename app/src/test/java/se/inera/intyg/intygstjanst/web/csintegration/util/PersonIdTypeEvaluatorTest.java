package se.inera.intyg.intygstjanst.web.csintegration.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import se.inera.intyg.intygstjanst.web.service.dto.PersonIdTypeDTO;
import se.inera.intyg.schemas.contract.Personnummer;

class PersonIdTypeEvaluatorTest {

    private static final Personnummer PERSONAL_IDENTITY_NUMBER = Personnummer.createPersonnummer("191212121212").orElseThrow();
    private static final Personnummer COORDINATION_NUMBER = Personnummer.createPersonnummer("191212721212").orElseThrow();

    @Test
    void shallReturnPersonalIdentityNumber() {
        assertEquals(PersonIdTypeDTO.PERSONAL_IDENTITY_NUMBER, PersonIdTypeEvaluator.getType(PERSONAL_IDENTITY_NUMBER));
    }

    @Test
    void shallReturnCoordinationNumber() {
        assertEquals(PersonIdTypeDTO.COORDINATION_NUMBER, PersonIdTypeEvaluator.getType(COORDINATION_NUMBER));
    }
}