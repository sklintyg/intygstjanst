/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.service.impl;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.CalculatePatientAgeService;

@Service
public class CalculatePatientAgeServiceImpl implements CalculatePatientAgeService {

    private static final int DATE_PART_OF_PERSON_ID = 8;
    private static final int DAY_PART_OF_DATE_PART = 6;
    private static final int MONTH_PART_OF_DATE_PART = 4;
    private static final int SAMORDNINGSNUMMER_DAY_CONSTANT = 60;
    private static final String STANDARD_FORMAT = "^(19|20)[0-9]{6}[-+]?[0-9]{4}$";

    private static final DateTimeFormatter MONTHDAY_FORMATTER = DateTimeFormatter.ofPattern("MMdd");

    @Override
    public Integer get(String patientId) {
        if (patientIdIsTwelveDigits(patientId)) {
            return getPatientAge(getNormalizedPnr(patientId));
        }
        final var normalizedPatientId = getCenturyFromYearAndSeparator(patientId) + getNormalizedPnr(patientId);
        return getPatientAge(normalizedPatientId);
    }

    private static boolean patientIdIsTwelveDigits(String patientId) {
        return patientId.matches(STANDARD_FORMAT);
    }

    private static String getNormalizedPnr(String patientId) {
        return patientId.replace("-", "").replace("+", "");
    }

    private int getPatientAge(String patientId) {
        var dateString = patientId.substring(0, DATE_PART_OF_PERSON_ID);
        final var day = Integer.parseInt(dateString.substring(DAY_PART_OF_DATE_PART));
        final var month = Integer.parseInt(dateString.substring(MONTH_PART_OF_DATE_PART, DAY_PART_OF_DATE_PART));

        if (patientIdIsSamordningsnummer(day)) {
            dateString = dateString.substring(0, MONTH_PART_OF_DATE_PART)
                + MONTHDAY_FORMATTER.format(MonthDay.of(month, day - SAMORDNINGSNUMMER_DAY_CONSTANT));
        }
        final var birthDate = LocalDate.from(DateTimeFormatter.BASIC_ISO_DATE.parse(dateString));
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private static boolean patientIdIsSamordningsnummer(int day) {
        return day > SAMORDNINGSNUMMER_DAY_CONSTANT;
    }

    private String getCenturyFromYearAndSeparator(String personnummer) {
        final var now = Calendar.getInstance();
        final var currentYear = now.getWeekYear();
        final var personnummerContainsCentury = personnummer.matches("[0-9]{8}[-+]?[0-9]{4}");
        final var yearStartIndex = personnummerContainsCentury ? 2 : 0;
        final var yearFromPersonnummer = Integer.parseInt(personnummer.substring(yearStartIndex, yearStartIndex + 2));
        final var dividerToRemoveNonCenturyYear = 100;
        final var century = (currentYear - yearFromPersonnummer) / dividerToRemoveNonCenturyYear;
        if (personnummer.contains("+")) {
            return String.valueOf(century - 1);
        }
        return String.valueOf(century);
    }
}
