/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.persistence.model.dao.util;

import se.inera.intyg.common.support.modules.support.api.dto.InvalidPersonNummerException;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

public final class DaoUtil {
    private DaoUtil() { }
    /**
     * Get personnummer in the persisted format (yyyyMMdd-xxxx) regardless of the entered format.
     * Please observe that this method is only to be used in the DAO and other interactions with the persistence layer!
     *
     * @param pnr Personnummer
     *
     * @return pnr as a String with format yyyyMMdd-xxxx, or the original pnr if formatting was unsuccessful.
     */
    // CHECKSTYLE:OFF MagicNumber
    public static String formatPnrForPersistence(Personnummer pnr) {
        try {
            final String normalizedPnr = pnr.getNormalizedPnr();
            return normalizedPnr.substring(0, 8) + "-" + normalizedPnr.substring(8);
        } catch (InvalidPersonNummerException e) {
            return pnr.getPersonnummer();
        }
    }
    // CHECKSTYLE:ON MagicNumber
}
