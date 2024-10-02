/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
import java.time.Period;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.CalculatePatientAgeService;
import se.inera.intyg.schemas.contract.Personnummer;

@Service
public class CalculatePatientAgeServiceImpl implements CalculatePatientAgeService {

    private static final int SAMORDNINGSNUMMER_DAY_CONSTANT = 60;

    @Override
    public Integer get(String patientId) {
        final var normalizedPnr = Personnummer.createPersonnummer(patientId).orElseThrow().getPersonnummer();
        return getPatientAge(normalizedPnr);
    }

    private int getPatientAge(String normalizedPnr) {
        final var date = normalizedPnr.substring(0, 8);
        final var birthDate = LocalDate.from(DateTimeFormatter.BASIC_ISO_DATE.parse(subtractDaysIfSamordningsNummer(date)));
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private String subtractDaysIfSamordningsNummer(String date) {
        var day = Integer.parseInt(date.substring(6));
        if (day > SAMORDNINGSNUMMER_DAY_CONSTANT) {
            return date.replaceFirst("(?<=\\d{6})\\d{2}", String.format("%02d", day - SAMORDNINGSNUMMER_DAY_CONSTANT));
        }
        return date;
    }
}
