/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
