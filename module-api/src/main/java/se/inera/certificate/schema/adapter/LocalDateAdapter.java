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

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 * Adapter for converting XML Schema types to Java dates and vice versa.
 *
 * @author andreaskaltenbach
 */
public final class LocalDateAdapter {

    private static final DateTimeZone TIME_ZONE = DateTimeZone.forID("Europe/Stockholm");
    private static final String ISO_DATE_PATTERN = "YYYY-MM-dd";
    private static final String ISO_DATE_TIME_PATTERN = "YYYY-MM-dd'T'HH:mm:ss";

    private LocalDateAdapter() {
    }

    /**
     * Converts an xs:date to a Joda Time LocalDate.
     */
    public static LocalDate parseDate(String dateString) {
        return new LocalDate(javax.xml.bind.DatatypeConverter.parseDate(dateString), TIME_ZONE);
    }

    /**
     * Converts an xs:datetime to a Joda Time LocalDateTime.
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        return new LocalDateTime(javax.xml.bind.DatatypeConverter.parseDateTime(dateTimeString), TIME_ZONE);
    }

    /**
     * Converts an intyg:common-model:1:date to a Joda Time LocalDate.
     */
    public static LocalDate parseIsoDate(String dateString) {
        return LocalDate.parse(dateString);
    }

    /**
     * Converts an intyg:common-model:1:dateTime to a Joda Time LocalDateTime.
     */
    public static LocalDateTime parseIsoDateTime(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString);
    }

    /**
     * Converts a Joda Time LocalDateTime to an xs:datetime.
     */
    public static String printDateTime(LocalDateTime dateTime) {
        return printIsoDateTime(dateTime);
    }

    /**
     * Converts a Joda Time LocalDate to an xs:date.
     */
    public static String printDate(LocalDate date) {
        return printIsoDate(date);
    }

    /**
     * Converts a Joda Time LocalDateTime to an intyg:common-model:1:date.
     */
    public static String printIsoDateTime(LocalDateTime dateTime) {
        return dateTime.toString(ISO_DATE_TIME_PATTERN);
    }

    /**
     * Converts a Joda Time LocalDate to an intyg:common-model:1:dateTime.
     */
    public static String printIsoDate(LocalDate date) {
        return date.toString(ISO_DATE_PATTERN);
    }
}
