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

package se.inera.intyg.intygstjanst.persistence.model.dao;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 */
public class CertificateStateHistoryEntryTest {
    @Test
    public void testOrdering() {
        CertificateStateHistoryEntry e1 = new CertificateStateHistoryEntry();
        e1.setTimestamp(LocalDateTime.parse("2013-08-20T12:00:01"));
        CertificateStateHistoryEntry e2 = new CertificateStateHistoryEntry();
        e2.setTimestamp(LocalDateTime.parse("2013-08-21T12:00:01"));
        CertificateStateHistoryEntry e3 = new CertificateStateHistoryEntry();
        e3.setTimestamp(LocalDateTime.parse("2013-08-20T14:00:01"));
        CertificateStateHistoryEntry e4 = new CertificateStateHistoryEntry();
        e4.setTimestamp(null);

        Collection<CertificateStateHistoryEntry> c = Arrays.asList( e1, e2, e3, e4 );

        List<CertificateStateHistoryEntry> r = CertificateStateHistoryEntry.BY_TIMESTAMP_DESC.sortedCopy(c);

        assertNull(r.get(0).getTimestamp());
        assertEquals(LocalDateTime.parse("2013-08-21T12:00:01"), r.get(1).getTimestamp());
        assertEquals(LocalDateTime.parse("2013-08-20T14:00:01"), r.get(2).getTimestamp());
        assertEquals(LocalDateTime.parse("2013-08-20T12:00:01"), r.get(3).getTimestamp());
    }
    
}