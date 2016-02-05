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

package se.inera.intyg.intygstjanst.persistence.model.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateWorkCapacity;

/**
 * @author andreaskaltenbach
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/persistence-config-unittest.xml" })
@ActiveProfiles("dev")
@Transactional
public class SjukfallCertificateDaoImplTest {

    private static final String HSA_ID_1 = "careunit-1";
    private static final String CARE_GIVER_1_ID = "caregiver-1";
    private static final String CARE_UNIT_NAME = "careunit-name-1";
    private static final String PERSONNUMMER = "191212121212";
    private static final String DOCTOR_HSA_ID = "doctor-1";
    private static final String DOCTOR_NAME = "doctor-name-1";
    private static final String FK7263 = "fk7263";

    private static final String HSA_ID_2_1 = "careunit-2-1";
    private static final String HSA_ID_2_2 = "careunit-2-2";
    private static final String CARE_GIVER_2_ID = "caregiver-2";


    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SjukfallCertificateDao sjukfallCertificateDao;

    @Test
    public void testFindActiveSjukfallCertificates() {
        buildDefaultSjukfallCertificates();
        List<SjukfallCertificate> resultList = sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(Arrays.asList(HSA_ID_1), CARE_GIVER_1_ID);
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        assertEquals(2, resultList.get(0).getSjukfallCertificateWorkCapacity().size());
    }

    @Test
    public void testDeletedSjukfallCertificateIsNotFound() {
        buildDeletedSjukfallCertificates();
        List<SjukfallCertificate> resultList = sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(Arrays.asList(HSA_ID_1), CARE_GIVER_1_ID);
        assertNotNull(resultList);
        assertEquals(0, resultList.size());
    }

    @Test
    public void testSjukfallCertificateWithoutOngoingArbetskapacitetNedsattningIsNotFound() {
        buildNonOngoingSjukfallCertificates();
        List<SjukfallCertificate> resultList = sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(Arrays.asList(HSA_ID_1), CARE_GIVER_1_ID);
        assertNotNull(resultList);
        assertEquals(0, resultList.size());
    }

    @Test
    public void testSjukfallCertFromOtherUnitOnSameCareGiverReturned() {

        // Add the standard belonging to Care giver 1
        buildDefaultSjukfallCertificates();

        // Create some certs for Care Giver 2.
        SjukfallCertificate sc1 = buildSjukfallCertificate(HSA_ID_2_1, CARE_GIVER_2_ID, defaultWorkCapacities(), false);
        SjukfallCertificate sc2 = buildSjukfallCertificate(HSA_ID_2_2, CARE_GIVER_2_ID, defaultWorkCapacities(), false);
        sjukfallCertificateDao.store(sc1);
        sjukfallCertificateDao.store(sc2);

        List<SjukfallCertificate> resultList = sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(Arrays.asList(HSA_ID_2_1), CARE_GIVER_2_ID);
        assertNotNull(resultList);
        assertEquals(2, resultList.size());
    }

    @Test
    public void testRevoke() {
        String id = buildDefaultSjukfallCertificates();
        sjukfallCertificateDao.revoke(id);

        SjukfallCertificate fromDb = entityManager.find(SjukfallCertificate.class, id);
        assertEquals(true, fromDb.getDeleted());
    }

    @Test
    public void testNothingHappensOnRevokeForNonExistingSjukfallCert() {
        String id = buildDefaultSjukfallCertificates();
        sjukfallCertificateDao.revoke("some-other-id");

        SjukfallCertificate fromDb = entityManager.find(SjukfallCertificate.class, id);
        assertEquals(false, fromDb.getDeleted());
    }

    private String buildDefaultSjukfallCertificates() {
        SjukfallCertificate sc = buildSjukfallCertificate(HSA_ID_1, CARE_GIVER_1_ID, defaultWorkCapacities(), false);
        sc = entityManager.merge(sc);
        return sc.getId();
    }

    private String buildDeletedSjukfallCertificates() {
        SjukfallCertificate sc = buildSjukfallCertificate(HSA_ID_1, CARE_GIVER_1_ID, defaultWorkCapacities(), true);
        sc = entityManager.merge(sc);
        return sc.getId();
    }

    private String buildNonOngoingSjukfallCertificates() {
        SjukfallCertificate sc = buildSjukfallCertificate(HSA_ID_1, CARE_GIVER_1_ID, nonOngoingWorkCapacities(), false);
        sc = entityManager.merge(sc);
        return sc.getId();
    }


    private SjukfallCertificate buildSjukfallCertificate(String careUnitId, String careGiverId, List<SjukfallCertificateWorkCapacity> workCapacities, boolean deleted) {
        SjukfallCertificate sc = new SjukfallCertificate(UUID.randomUUID().toString());
        sc.setCareUnitId(careUnitId);

        sc.setSjukfallCertificateWorkCapacity(workCapacities);
        sc.setCareGiverId(careGiverId);
        sc.setCareUnitName(CARE_UNIT_NAME);
        sc.setCivicRegistrationNumber(PERSONNUMMER);
        sc.setDiagnoseCode("M16");
        sc.setPatientFirstName("Tolvan");
        sc.setPatientLastName("Tolvansson");
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

    private List<SjukfallCertificateWorkCapacity> nonOngoingWorkCapacities() {
        List<SjukfallCertificateWorkCapacity> workCapacities = new ArrayList<>();
        SjukfallCertificateWorkCapacity wc = new SjukfallCertificateWorkCapacity();
        wc.setCapacityPercentage(100);
        wc.setFromDate(LocalDate.now().minusWeeks(2).format(DateTimeFormatter.ISO_DATE));
        wc.setToDate(LocalDate.now().minusWeeks(1).format(DateTimeFormatter.ISO_DATE));
        workCapacities.add(wc);
        return workCapacities;
    }
}
