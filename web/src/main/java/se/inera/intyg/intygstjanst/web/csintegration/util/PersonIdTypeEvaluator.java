package se.inera.intyg.intygstjanst.web.csintegration.util;

import java.util.Optional;
import se.inera.intyg.common.support.validate.SamordningsnummerValidator;
import se.inera.intyg.intygstjanst.web.service.dto.PersonIdTypeDTO;
import se.inera.intyg.schemas.contract.Personnummer;

public class PersonIdTypeEvaluator {

  private PersonIdTypeEvaluator() {
    throw new IllegalStateException("Utility class");
  }

  public static PersonIdTypeDTO getType(Personnummer personId) {
    return SamordningsnummerValidator.isSamordningsNummer(
        Optional.of(personId)) ? PersonIdTypeDTO.COORDINATION_NUMBER
        : PersonIdTypeDTO.PERSONAL_IDENTITY_NUMBER;
  }
}