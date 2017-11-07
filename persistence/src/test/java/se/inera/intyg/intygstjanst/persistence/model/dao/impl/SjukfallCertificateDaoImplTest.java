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
package se.inera.intyg.intygstjanst.persistence.model.dao.impl;

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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * DAO test, uses @ContextConfiguration e.g. real DB.
 *
 * @author eriklupander
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/persistence-config-unittest.xml" })
@ActiveProfiles("dev")
@Transactional
public class SjukfallCertificateDaoImplTest {

    private static final String CARE_GIVER_1_ID = "caregiver-1";
    private static final String CARE_GIVER_2_ID = "caregiver-2";
    private static final String CARE_UNIT_1_ID = "careunit-1";
    private static final String CARE_UNIT_1_NAME = "careunit-1-name";

    private static final LocalDateTime CERT_SIGNING_DATETIME = LocalDateTime.parse("2016-02-01T15:00:00");
    private static final String PERSONNUMMER = "19121212-1212";
    private static final String DOCTOR_HSA_ID = "doctor-1";
    private static final String DOCTOR_NAME = "doctor-1-name";
    private static final String FK7263 = "fk7263";



    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SjukfallCertificateDao sjukfallCertificateDao;

    @Test
    public void testFindActiveSjukfallCertificates() {
        buildDefaultSjukfallCertificates();
        List<SjukfallCertificate> resultList = sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(
                CARE_GIVER_1_ID, Arrays.asList(CARE_UNIT_1_ID));

        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        assertEquals(3, resultList.get(0).getSjukfallCertificateWorkCapacity().size());
    }

    @Test
    public void testFindActiveSjukfallCertificatesForPatient() {
        buildDefaultSjukfallCertificates();
        List<SjukfallCertificate> resultList = sjukfallCertificateDao.findActiveSjukfallCertificateForPersonOnCareUnits(
                CARE_GIVER_1_ID, Arrays.asList(CARE_UNIT_1_ID), PERSONNUMMER);

        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        assertEquals(3, resultList.get(0).getSjukfallCertificateWorkCapacity().size());
    }

    @Test
    public void testDeletedSjukfallCertificateIsNotFound() {
        buildDeletedSjukfallCertificates();
        List<SjukfallCertificate> resultList = sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(
                CARE_GIVER_1_ID, Arrays.asList(CARE_UNIT_1_ID));
        assertNotNull(resultList);
        assertEquals(0, resultList.size());
    }

    @Test
    public void testSjukfallCertificateWithoutOngoingArbetskapacitetNedsattningIsNotFound() {
        buildNonOngoingSjukfallCertificates();
        List<SjukfallCertificate> resultList = sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(
                CARE_GIVER_1_ID, Arrays.asList(CARE_UNIT_1_ID));

        assertNotNull(resultList);
        assertEquals(0, resultList.size());
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

    @Test
    public void testGetSjukfallFromOneVardgivareOnly() {
        // This test is for JIRA INTYG-4849 where we need to ensure that
        // we only get sjukfall from a healthcare unit's current healthcare
        // provider even though a healthcare unit has belonged to several
        // healthcare providers.

        SjukfallCertificate sc1 = buildSjukfallCertificate(CARE_GIVER_1_ID, CARE_UNIT_1_ID, CARE_UNIT_1_NAME,
                defaultWorkCapacities(), false);
        sc1 = entityManager.merge(sc1);

        SjukfallCertificate sc2 = buildSjukfallCertificate(CARE_GIVER_2_ID, CARE_UNIT_1_ID, CARE_UNIT_1_NAME,
                defaultWorkCapacities(), false);
        sc2 = entityManager.merge(sc2);

        assertEquals(CARE_GIVER_1_ID, entityManager.find(SjukfallCertificate.class, sc1.getId()).getCareGiverId());
        assertEquals(CARE_GIVER_2_ID, entityManager.find(SjukfallCertificate.class, sc2.getId()).getCareGiverId());

        List<SjukfallCertificate> resultList = sjukfallCertificateDao.findActiveSjukfallCertificateForCareUnits(
                CARE_GIVER_2_ID, Arrays.asList(CARE_UNIT_1_ID));

        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        assertEquals(3, resultList.get(0).getSjukfallCertificateWorkCapacity().size());
    }

    private String buildDefaultSjukfallCertificates() {
        SjukfallCertificate sc = buildSjukfallCertificate(CARE_GIVER_1_ID, CARE_UNIT_1_ID, CARE_UNIT_1_NAME,
                defaultWorkCapacities(), false);
        sc = entityManager.merge(sc);
        return sc.getId();
    }

    private String buildDeletedSjukfallCertificates() {
        SjukfallCertificate sc = buildSjukfallCertificate(CARE_GIVER_1_ID, CARE_UNIT_1_ID, CARE_UNIT_1_NAME,
                defaultWorkCapacities(), true);
        sc = entityManager.merge(sc);
        return sc.getId();
    }

    private String buildNonOngoingSjukfallCertificates() {
        SjukfallCertificate sc = buildSjukfallCertificate(CARE_GIVER_1_ID, CARE_UNIT_1_ID, CARE_UNIT_1_NAME,
                nonOngoingWorkCapacities(), false);
        sc = entityManager.merge(sc);
        return sc.getId();
    }


    private SjukfallCertificate buildSjukfallCertificate(String careGiverId, String careUnitId, String careUnitName,
                                                         List<SjukfallCertificateWorkCapacity> workCapacities, boolean deleted) {

        SjukfallCertificate sc = new SjukfallCertificate(UUID.randomUUID().toString());
        sc.setCareGiverId(careGiverId);
        sc.setCareUnitId(careUnitId);
        sc.setCareUnitName(careUnitName);
        sc.setSigningDateTime(CERT_SIGNING_DATETIME);
        sc.setSjukfallCertificateWorkCapacity(workCapacities);
        sc.setCivicRegistrationNumber(PERSONNUMMER);
        sc.setDiagnoseCode("M16");
        sc.setPatientName("Tolvan Tolvansson");
        sc.setSigningDoctorId(DOCTOR_HSA_ID);
        sc.setSigningDoctorName(DOCTOR_NAME);
        sc.setType(FK7263);
        sc.setDeleted(deleted);
        sc.setEmployment("STUDERANDE,ARBETSSOKANDE");
        sc.setBiDiagnoseCode1("J21");
        sc.setBiDiagnoseCode2("J22");
        return sc;
    }

    private List<SjukfallCertificateWorkCapacity> defaultWorkCapacities() {
        List<SjukfallCertificateWorkCapacity> workCapacities = new ArrayList<>();
        SjukfallCertificateWorkCapacity wc = new SjukfallCertificateWorkCapacity();

        wc.setCapacityPercentage(100);
        wc.setFromDate(LocalDate.now().minusWeeks(1).format(DateTimeFormatter.ISO_DATE));
        wc.setToDate(LocalDate.now().plusWeeks(1).format(DateTimeFormatter.ISO_DATE));
        workCapacities.add(wc);

        SjukfallCertificateWorkCapacity wc2 = new SjukfallCertificateWorkCapacity();
        wc2.setCapacityPercentage(75);
        wc2.setFromDate(LocalDate.now().minusWeeks(3).format(DateTimeFormatter.ISO_DATE));
        wc2.setToDate(LocalDate.now().minusWeeks(1).format(DateTimeFormatter.ISO_DATE));
        workCapacities.add(wc2);

        SjukfallCertificateWorkCapacity wc3 = new SjukfallCertificateWorkCapacity();
        wc3.setCapacityPercentage(50);
        wc3.setFromDate(LocalDate.now().minusWeeks(4).format(DateTimeFormatter.ISO_DATE));
        wc3.setToDate(LocalDate.now().minusWeeks(3).format(DateTimeFormatter.ISO_DATE));
        workCapacities.add(wc3);
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
