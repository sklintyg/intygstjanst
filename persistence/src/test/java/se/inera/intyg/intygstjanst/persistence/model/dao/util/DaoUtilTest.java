/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.inera.intyg.common.support.peristence.dao.util.DaoUtil;
import se.inera.intyg.schemas.contract.Personnummer;

public class DaoUtilTest {

    @Test
    public void testWithDash() {
        Personnummer pnr = createPnr("19121212-1212");
        String expected = "19121212-1212";
        assertEquals(expected, DaoUtil.formatPnrForPersistence(pnr));
    }

    @Test
    public void testWithoutDash() {
        Personnummer pnr = createPnr("191212121212");
        String expected = "19121212-1212";
        assertEquals(expected, DaoUtil.formatPnrForPersistence(pnr));
    }

    @Test
    public void testWith6Digits() {
        Personnummer pnr = createPnr("1212121212");
        String expected = "20121212-1212";
        assertEquals(expected, DaoUtil.formatPnrForPersistence(pnr));
    }

    private Personnummer createPnr(String pnr) {
        return Personnummer.createPersonnummer(pnr).get();
    }
}
