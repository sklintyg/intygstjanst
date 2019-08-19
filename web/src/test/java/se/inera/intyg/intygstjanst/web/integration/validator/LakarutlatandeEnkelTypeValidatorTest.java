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
package se.inera.intyg.intygstjanst.web.integration.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import iso.v21090.dt.v1.II;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;


public class LakarutlatandeEnkelTypeValidatorTest {

    private LakarutlatandeEnkelType lakarutlatande;
    private PatientType patient;
    private II patientId;
    private List<String> errors;
    private LakarutlatandeEnkelTypeValidator validator;

    @Before
    public void setup() {
        lakarutlatande = new LakarutlatandeEnkelType();
        lakarutlatande.setLakarutlatandeId("id");
        lakarutlatande.setSigneringsTidpunkt(LocalDateTime.now());
        patient = new PatientType();
        patientId = new II();
        patientId.setRoot("1.2.752.129.2.1.3.1");
        patientId.setExtension("19121212-1212");
        patient.setPersonId(patientId);
        patient.setFullstandigtNamn("namn");
        lakarutlatande.setPatient(patient);
        errors = new ArrayList<>();
        validator = new LakarutlatandeEnkelTypeValidator(lakarutlatande, errors);
    }

    @Test
    public void testErrorsOnEmtpyCertificate() {
        lakarutlatande.setLakarutlatandeId(null);
        lakarutlatande.setSigneringsTidpunkt(null);
        lakarutlatande.setPatient(null);
        validator.validateAndCorrect();
        assertTrue(errors.contains("No Lakarutlatande Id found!"));
        assertTrue(errors.contains("No signeringstidpunkt found!"));
        assertTrue(errors.contains("No Patient element found!"));
    }

    @Test
    public void testInvalidPatientIdExtension() {
        patientId.setExtension("19121212-121X");
        validator.validateAndCorrect();
        assertTrue(errors.contains("Wrong format for person-id! Valid format is YYYYMMDD-XXXX or YYYYMMDD+XXXX."));
    }

    @Test
    public void testDashInPatientIdExtensionIsCorrected() {
        patientId.setExtension("191212121212");
        validator.validateAndCorrect();
        assertTrue(errors.isEmpty());
        assertEquals("19121212-1212", patientId.getExtension());
    }

    // INTYG-4086, namn skall ej längre skickas med.
//    @Test
//    public void testMissingFullstandigtNamn() {
//        patient.setFullstandigtNamn(null);
//        validator.validateAndCorrect();
//        assertTrue(errors.contains("No Patient fullstandigtNamn elements found or set!"));
//    }

}
