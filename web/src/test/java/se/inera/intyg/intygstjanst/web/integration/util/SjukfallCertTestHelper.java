/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integration.util;

import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateWorkCapacity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by eriklupander on 2017-02-17.
 */
public class SjukfallCertTestHelper {

    public static final String CARE_UNIT_ID_1 = "careunit-1";
    public static final String DOCTOR_HSA_ID = "doctor-1";

    public static final String CARE_GIVER_1_ID = "caregiver-1";
    public static final String CARE_UNIT_NAME = "careunit-name-1";
    public static final String PERSONNUMMER = "191212121212";

    public static final String DOCTOR_NAME = "doctor-name-1";
    private static final String FK7263 = "fk7263";
    public static final String PATIENT_NAME = "Tolvan Tolvansson";
    public static final String DIAGNOSE_CODE = "M16";

    public List<SjukfallCertificate> intygsList() {
        List<SjukfallCertificate> certList = new ArrayList<>();
        certList.add(buildSjukfallCertificate(CARE_UNIT_ID_1, CARE_GIVER_1_ID, defaultWorkCapacities(), false));
        return certList;
    }

    private SjukfallCertificate buildSjukfallCertificate(String careUnitId, String careGiverId, List<SjukfallCertificateWorkCapacity> workCapacities, boolean deleted) {
        SjukfallCertificate sc = new SjukfallCertificate(UUID.randomUUID().toString());
        sc.setCareUnitId(careUnitId);
        sc.setSjukfallCertificateWorkCapacity(workCapacities);
        sc.setCareGiverId(careGiverId);
        sc.setCareUnitName(CARE_UNIT_NAME);
        sc.setCivicRegistrationNumber(PERSONNUMMER);
        sc.setDiagnoseCode(DIAGNOSE_CODE);
        sc.setPatientName(PATIENT_NAME);
        sc.setSigningDoctorId(DOCTOR_HSA_ID);
        sc.setSigningDoctorName(DOCTOR_NAME);
        sc.setType(FK7263);
        sc.setDeleted(deleted);
        return sc;
    }

    private List<SjukfallCertificateWorkCapacity> defaultWorkCapacities() {
        List<SjukfallCertificateWorkCapacity> workCapacities = new ArrayList<>();
        SjukfallCertificateWorkCapacity wc = new SjukfallCertificateWorkCapacity();

        wc.setCapacityPercentage(75);
        wc.setFromDate(LocalDate.now().minusWeeks(1).format(DateTimeFormatter.ISO_DATE));
        wc.setToDate(LocalDate.now().plusWeeks(1).format(DateTimeFormatter.ISO_DATE));
        workCapacities.add(wc);

        SjukfallCertificateWorkCapacity wc2 = new SjukfallCertificateWorkCapacity();
        wc2.setCapacityPercentage(100);
        wc2.setFromDate(LocalDate.now().minusWeeks(3).format(DateTimeFormatter.ISO_DATE));
        wc2.setToDate(LocalDate.now().minusWeeks(1).format(DateTimeFormatter.ISO_DATE));
        workCapacities.add(wc2);
        return workCapacities;
    }
}
