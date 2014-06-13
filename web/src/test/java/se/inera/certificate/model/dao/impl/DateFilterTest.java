/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Certificate (http://code.google.com/p/inera-certificate).
 *
 * Inera Certificate is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Certificate is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.certificate.model.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;

import se.inera.certificate.model.dao.Certificate;

public class DateFilterTest {

    @Test
    public void testFilterWithDates() throws Exception {
        List<Certificate> data = Arrays.asList(new Certificate []{
           createCertificate("1", "2013-04-13", "2013-05-13"),
           createCertificate("2", "2013-03-28", "2013-04-28"),
           createCertificate("3", "2013-03-01", "2013-03-27"),
           createCertificate("4", "2013-03-01", "2013-03-28"),
           createCertificate("5", "2013-03-01", "2013-03-27"),
           createCertificate("6", "2013-04-29", "2013-05-03"),
           createCertificate("7", "2013-04-01", "2013-05-03")
        });

        List<Certificate> result = new DateFilter(data).filter(new LocalDate("2013-03-28"), new LocalDate("2013-04-28"));
        List<String> ids = new ArrayList<String>();
        for (Certificate c : result) {
            ids.add(c.getId());
        }
        assertTrue(ids.contains("1"));
        assertTrue(ids.contains("2"));
        assertFalse(ids.contains("3"));
        assertTrue(ids.contains("4"));
        assertFalse(ids.contains("5"));
        assertFalse(ids.contains("6"));
        assertTrue(ids.contains("7"));
    }

    private Certificate createCertificate(String id, String from, String to) {
        Certificate c = new Certificate(id, "");
        c.setValidFromDate(from);
        c.setValidToDate(to);
        return c;
    }

    @Test
    public void testFilterNullDates() throws Exception {
        List<Certificate> data = Arrays.asList(new Certificate []{
                createCertificate("1", "2013-04-13", "2013-05-13"),
                createCertificate("2", "2013-03-28", "2013-04-28"),
                createCertificate("3", "2013-03-01", "2013-03-27"),
                createCertificate("7", "2013-04-01", "2013-05-03")
             });
        DateFilter dateFilter = new DateFilter(data);
        List<Certificate> result1 = dateFilter.filter(null, null);
        assertEquals(data.size(), result1.size());
        List<Certificate> result2 = dateFilter.filter(new LocalDate("2013-03-28"), null);
        assertEquals(data.size(), result2.size());
        List<Certificate> result3 = dateFilter.filter(null, new LocalDate("2013-04-28"));
        assertEquals(data.size(), result3.size());
    }

    @Test
    public void testFilterNullDatesInCertificate() throws Exception {
        // Timeline
        // <---+------+------+------+------+--->
        //     A      B      C      D      E
        //
        //            [-------------]            certificate 1
        //            [------------------------> certificate 2
        // <------------------------]            certificate 3
        // <-----------------------------------> certificate 4
        //
        // <---]                                 filter 1
        // <-----------------]                   filter 2
        //            [------]                   filter 3
        //                   [-----------------> filter 4
        //                                 [---> filter 5
        // <-----------------------------------> filter 6

        String dateA = "2013-01-01";
        String dateB = "2013-02-01";
        String dateC = "2013-03-01";
        String dateD = "2013-04-01";
        String dateE = "2013-05-01";

        List<Certificate> data = Arrays.asList(new Certificate[] {
                createCertificate("1", dateB, dateD),
                createCertificate("2", null, dateD),
                createCertificate("3", dateB, null),
                createCertificate("4", null, null)
        });
        DateFilter dateFilter = new DateFilter(data);

        List<Certificate> result1 = dateFilter.filter(null, new LocalDate(dateA));
        assertCertificates(result1, "3", "4");

        List<Certificate> result2 = dateFilter.filter(null, new LocalDate(dateC));
        assertCertificates(result2, "1", "2", "3", "4");

        List<Certificate> result3 = dateFilter.filter(new LocalDate(dateB), new LocalDate(dateC));
        assertCertificates(result3, "1", "2", "3", "4");

        List<Certificate> result4 = dateFilter.filter(new LocalDate(dateC), null);
        assertCertificates(result4, "1", "2", "3", "4");

        List<Certificate> result5 = dateFilter.filter(new LocalDate(dateE), null);
        assertCertificates(result5, "2", "4");

        List<Certificate> result6 = dateFilter.filter(null, null);
        assertCertificates(result6, "1", "2", "3", "4");
    }

    private void assertCertificates(List<Certificate> certificateList, String... certificateIds) {
        HashSet<String> certIdSet = new HashSet<>();
        for (Certificate certificate : certificateList) {
            certIdSet.add(certificate.getId());
        }

        assertTrue("certificateList did not contain all expected Ids", certIdSet.containsAll(Arrays.asList(certificateIds)));
    }
}
