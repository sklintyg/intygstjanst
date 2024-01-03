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

import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.ResolvePatientGenderService;
import se.inera.intyg.schemas.contract.Personnummer;

@Service
public class ResolvePatientGenderServiceImpl implements ResolvePatientGenderService {

    private static final int GENDER_START = 10;
    private static final int GENDER_END = 11;
    private static final String UKNOWN = "Okänd";
    private static final String MALE_PATIENT_ID_REGEX = "^\\d*[13579]$";

    @Override
    public String get(String patientId) {
        return getGenderFromString(patientId);
    }

    private static String getGenderFromString(String patientId) {
        try {
            return Personnummer
                .createPersonnummer(patientId)
                .orElseThrow()
                .getPersonnummer()
                .substring(GENDER_START, GENDER_END)
                .matches(MALE_PATIENT_ID_REGEX) ? "Man" : "Kvinna";
        } catch (Exception exception) {
            return UKNOWN;
        }
    }
}
