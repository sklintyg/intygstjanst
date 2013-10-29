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
package se.inera.certificate.schema.adapter;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author andreaskaltenbach
 */
public final class LocalDateAdapterTest {

    private static final String DATE_TIME_STRING_WITH_TIME_ZONE = "2012-11-13T11:55:50-01:00";
    private static final String DATE_TIME_STRING_WITH_MILLIS_AND_TIME_ZONE = "2012-11-13T11:55:50.12-01:00";
    private static final String DATE_TIME_STRING_WITH_Z = "2012-11-13T12:55:50Z";
    private static final String DATE_TIME_STRING_WITH_MILLIS_AND_Z = "2012-11-13T12:55:50.12Z";
    private static final String DATE_TIME_STRING_WITH_MILLIS = "2012-11-13T13:55:50.120";
    private static final String DATE_TIME_STRING = "2012-11-13T13:55:50";
    private static final String DATE_STRING = "2012-11-13";
    private static final String DATE_STRING_WITH_TIME_ZONE = "2012-11-13-01:00";
    private static final String DATE_STRING_WITH_Z = "2012-11-13Z";

    private static final DateTimeZone TIME_ZONE = DateTimeZone.forID("Europe/Stockholm");
    private static final LocalDate LOCAL_DATE = new LocalDate(TIME_ZONE).withYear(2012).withMonthOfYear(11).withDayOfMonth(13);
    
    private static final LocalDateTime LOCAL_DATE_TIME = new LocalDateTime(TIME_ZONE).withYear(2012).withMonthOfYear(11).withDayOfMonth(13).withHourOfDay(13).withMinuteOfHour(55).withSecondOfMinute(50).withMillisOfSecond(0);
    private static final LocalDateTime LOCAL_DATE_TIME_WITH_MILLIS = LOCAL_DATE_TIME.withMillisOfSecond(120);
    private static final LocalDateTime LOCAL_DATE_TIME_START_OF_DAY_STRING = LOCAL_DATE_TIME.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

    private static final LocalDate ISO_DATE = new LocalDate(2012, 11, 13);
    private static final LocalDateTime ISO_DATE_TIME = new LocalDateTime(2012, 11, 13, 13, 55, 50, 0);

    private static final String ISO_DATE_TIME_STRING = "2012-11-13T13:55:50";
    private static final String ISO_DATE_STRING = "2012-11-13";

    private static TimeZone systemTimeZone;
    
    @BeforeClass
    public static void setGMTTimeZone() {
        systemTimeZone = TimeZone.getDefault();
        TimeZone GMT = DateTimeZone.forID("GMT").toTimeZone();
        TimeZone.setDefault(GMT);
    }

    @AfterClass
    public static void restoreTimeZone() {
        TimeZone.setDefault(systemTimeZone);
    }
    
    @Test
    public void testParseDateTime() {
        LocalDateTime date = LocalDateAdapter.parseDateTime(DATE_TIME_STRING);
        assertEquals(LOCAL_DATE_TIME, date);
    }

    @Test
    public void testParseDateTimeWithMillis() {
        LocalDateTime date = LocalDateAdapter.parseDateTime(DATE_TIME_STRING_WITH_MILLIS);
        assertEquals(LOCAL_DATE_TIME_WITH_MILLIS, date);
    }

    @Test
    public void testParseDateTimeWithTimeZone() {
        LocalDateTime date = LocalDateAdapter.parseDateTime(DATE_TIME_STRING_WITH_TIME_ZONE);
        assertEquals(LOCAL_DATE_TIME, date);
    }

    @Test
    public void testParseDateTimeWithMillisAndTimeZone() {
        LocalDateTime date = LocalDateAdapter.parseDateTime(DATE_TIME_STRING_WITH_MILLIS_AND_TIME_ZONE);
        assertEquals(LOCAL_DATE_TIME_WITH_MILLIS, date);
    }

    @Test
    public void testParseDateTimeWithZ() {
        LocalDateTime date = LocalDateAdapter.parseDateTime(DATE_TIME_STRING_WITH_Z);
        assertEquals(LOCAL_DATE_TIME, date);
    }
    
    @Test
    public void testParseDateTimeWithMillisAndZ() {
        LocalDateTime date = LocalDateAdapter.parseDateTime(DATE_TIME_STRING_WITH_MILLIS_AND_Z);
        assertEquals(LOCAL_DATE_TIME_WITH_MILLIS, date);
    }
    
    @Test
    public void testParseDateTimeWithOnlyDate() {
        LocalDateTime date = LocalDateAdapter.parseDateTime(DATE_STRING);
        assertEquals(LOCAL_DATE_TIME_START_OF_DAY_STRING, date);
    }

    @Test
    public void testParseDateTimeWithOnlyDateAndTimeZone() {
        LocalDateTime date = LocalDateAdapter.parseDateTime(DATE_STRING_WITH_TIME_ZONE);
        assertEquals(LOCAL_DATE_TIME_START_OF_DAY_STRING.plusHours(2), date);
    }

    @Test
    public void testParseDateTimeWithOnlyDateAndZ() {
        LocalDateTime date = LocalDateAdapter.parseDateTime(DATE_STRING_WITH_Z);
        assertEquals(LOCAL_DATE_TIME_START_OF_DAY_STRING.plusHours(1), date);
    }

    @Test
    public void testParseDate() throws DatatypeConfigurationException {
        LocalDate date = LocalDateAdapter.parseDate(DATE_STRING);
        assertEquals(LOCAL_DATE, date);
    }

    @Test
    public void testParseDateWithTimeZone() throws DatatypeConfigurationException {
        LocalDate date = LocalDateAdapter.parseDate(DATE_STRING_WITH_TIME_ZONE);
        assertEquals(LOCAL_DATE, date);
    }

    @Test
    public void testParseDateWithZ() throws DatatypeConfigurationException {
        LocalDate date = LocalDateAdapter.parseDate(DATE_STRING_WITH_Z);
        assertEquals(LOCAL_DATE, date);
    }

    @Test
    public void testParseDateWithDateTime() throws DatatypeConfigurationException {
        LocalDate date = LocalDateAdapter.parseDate(DATE_TIME_STRING);
        assertEquals(LOCAL_DATE, date);
    }

    @Test
    public void testParseDateWithDateTimeWithMillis() throws DatatypeConfigurationException {
        LocalDate date = LocalDateAdapter.parseDate(DATE_TIME_STRING_WITH_MILLIS);
        assertEquals(LOCAL_DATE, date);
    }

    @Test
    public void testParseDateWithDateTimeWithZ() throws DatatypeConfigurationException {
        LocalDate date = LocalDateAdapter.parseDate(DATE_TIME_STRING_WITH_Z);
        assertEquals(LOCAL_DATE, date);
    }

    @Test
    public void testParseDateWithDateTimeWithMillisAndZ() throws DatatypeConfigurationException {
        LocalDate date = LocalDateAdapter.parseDate(DATE_TIME_STRING_WITH_MILLIS_AND_Z);
        assertEquals(LOCAL_DATE, date);
    }

    @Test
    public void testParseDateWithDateTimeWithTimeZone() throws DatatypeConfigurationException {
        LocalDate date = LocalDateAdapter.parseDate(DATE_TIME_STRING_WITH_TIME_ZONE);
        assertEquals(LOCAL_DATE, date);
    }

    @Test
    public void testParseDateWithDateTimeWithMillisAndTimeZone() throws DatatypeConfigurationException {
        LocalDate date = LocalDateAdapter.parseDate(DATE_TIME_STRING_WITH_MILLIS_AND_TIME_ZONE);
        assertEquals(LOCAL_DATE, date);
    }

    @Test
    public void testPrintDate() {
        String dateString = LocalDateAdapter.printDate(LOCAL_DATE);
        assertEquals(DATE_STRING, dateString);
    }

    @Test
    public void testPrintDateTime() {
        String dateString = LocalDateAdapter.printDateTime(LOCAL_DATE_TIME);
        assertEquals(DATE_TIME_STRING, dateString);
    }


    @Test
    public void testParseIsoDate() {
        LocalDate date = LocalDateAdapter.parseIsoDate(ISO_DATE_STRING);
        assertEquals(ISO_DATE, date);
    }

    @Test
    public void testParseIsoDateTime() {
        LocalDateTime date = LocalDateAdapter.parseIsoDateTime(ISO_DATE_TIME_STRING);
        assertEquals(ISO_DATE_TIME, date);
    }

    @Test
    public void testPrintIsoDate() {
        String dateString = LocalDateAdapter.printIsoDate(LOCAL_DATE);
        assertEquals(ISO_DATE_STRING, dateString);
    }

    @Test
    public void testPrintIsoDateTime() {
        String dateString = LocalDateAdapter.printIsoDateTime(LOCAL_DATE_TIME);
        assertEquals(ISO_DATE_TIME_STRING, dateString);
    }
    
}
