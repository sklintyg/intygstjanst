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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static se.inera.intyg.common.support.model.CertificateState.DELETED;
import static se.inera.intyg.common.support.model.CertificateState.RECEIVED;
import static se.inera.intyg.common.support.model.CertificateState.RESTORED;
import static se.inera.intyg.common.support.model.CertificateState.SENT;
import static se.inera.intyg.intygstjanst.persistence.support.CertificateFactory.CERTIFICATE_ID;
import static se.inera.intyg.intygstjanst.persistence.support.CertificateFactory.CIVIC_REGISTRATION_NUMBER;
import static se.inera.intyg.intygstjanst.persistence.support.CertificateFactory.FK7263;
import static se.inera.intyg.intygstjanst.persistence.support.CertificateFactory.buildCertificate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.exception.PersistenceException;
import se.inera.intyg.intygstjanst.persistence.model.dao.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/persistence-config-unittest.xml" })
@ActiveProfiles("dev")
@Transactional
public class CertificateDaoImplTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CertificateDao certificateDao;

    @Test
    public void testFindCertificateWithoutUserId() {
        List<Certificate> certificate = certificateDao.findCertificate(null, null, null, null, null);
        assertTrue(certificate.isEmpty());
    }

    @Test
    public void testFindCertificateForUserWithoutCertificates() {
        List<Certificate> certificate = certificateDao.findCertificate(CIVIC_REGISTRATION_NUMBER, null, null, null,
                null);
        assertTrue(certificate.isEmpty());
    }

    @Test
    public void testFindCertificateWithoutTypeForUserWithOneCertificate() {

        entityManager.persist(buildCertificate());

        List<Certificate> certificate = certificateDao.findCertificate(CIVIC_REGISTRATION_NUMBER, null, null, null,
                null);
        assertEquals(1, certificate.size());
    }

    @Test
    public void testFindCertificateWithEmptyTypeForUserWithOneCertificate() {
        entityManager.persist(buildCertificate());

        List<Certificate> certificate = certificateDao.findCertificate(CIVIC_REGISTRATION_NUMBER,
                Collections.<String> emptyList(), null, null, null);
        assertEquals(1, certificate.size());
    }

    @Test
    public void testFindCertificateWithCertificateTypeFilter() {

        String otherCertificateType = "other";

        // create an FK7263 and another certificate
        entityManager.persist(buildCertificate());
        entityManager.persist(buildCertificate("otherCertificateId", otherCertificateType));

        // no certificate type -> no filtering by certificate type
        List<Certificate> certificate = certificateDao.findCertificate(CIVIC_REGISTRATION_NUMBER, null, null, null,
                null);
        assertEquals(2, certificate.size());

        // filter by FK7263 -> only return FK7263
        certificate = certificateDao
                .findCertificate(CIVIC_REGISTRATION_NUMBER, singletonList(FK7263), null, null, null);
        assertEquals(1, certificate.size());
        assertEquals(FK7263, certificate.get(0).getType());

        // filter by other type -> only return other certificate
        certificate = certificateDao.findCertificate(CIVIC_REGISTRATION_NUMBER, singletonList(otherCertificateType),
                null, null, null);
        assertEquals(1, certificate.size());
        assertEquals(otherCertificateType, certificate.get(0).getType());

        // filter by both types -> both certificates are returned
        certificate = certificateDao.findCertificate(CIVIC_REGISTRATION_NUMBER, asList(FK7263, otherCertificateType),
                null, null, null);
        assertEquals(2, certificate.size());
    }

    @Test
    public void testFindCertificateWithCareUnitFilter() {

        // two certificates with different care units IDs
        Certificate west = buildCertificate("1");
        west.setCareUnitId("west");
        entityManager.persist(west);

        Certificate east = buildCertificate("2");
        east.setCareUnitId("east");
        entityManager.persist(east);

        // no matching care unit ID, no certificates
        List<Certificate> certificate = certificateDao.findCertificate(CIVIC_REGISTRATION_NUMBER, null, null, null,
                Collections.singletonList("unknown"));
        assertEquals(0, certificate.size());

        // filter by 'west' -> only return 'west'-based intyg
        certificate = certificateDao.findCertificate(CIVIC_REGISTRATION_NUMBER, null, null, null, Collections.singletonList("west"));
        assertEquals(1, certificate.size());
        assertEquals(west, certificate.get(0));

        // filter by 'west' and 'east' -> both 'west'- and 'east'-based intyg
        certificate = certificateDao.findCertificate(CIVIC_REGISTRATION_NUMBER, null, null, null, Arrays.asList("west", "east"));
        assertEquals(2, certificate.size());
    }

    @Test
    public void testFindCertificateWithDates() throws Exception {
        int certificateId = Integer.parseInt(CERTIFICATE_ID);

        entityManager.persist(buildCertificate(String.valueOf(certificateId++), LocalDate.parse("2013-04-13").atStartOfDay()));
        entityManager.persist(buildCertificate(String.valueOf(certificateId++), LocalDate.parse("2013-05-13").atStartOfDay()));
        entityManager.persist(buildCertificate(String.valueOf(certificateId++), LocalDate.parse("2013-04-12").atStartOfDay()));

        List<Certificate> certificate = certificateDao.findCertificate(CIVIC_REGISTRATION_NUMBER,
                singletonList(FK7263), LocalDate.parse("2013-04-01"), LocalDate.parse("2013-04-15"), null);

        assertEquals(2, certificate.size());
    }

    @Test
    public void testFindCertificateWithDatesInclusive() throws Exception {
        int certificateId = Integer.parseInt(CERTIFICATE_ID);
        final String certificateId1 = String.valueOf(certificateId++);
        final String certificateId2 = String.valueOf(certificateId++);

        entityManager.persist(buildCertificate(String.valueOf(certificateId++), LocalDate.parse("2013-04-11").atStartOfDay()));
        entityManager.persist(buildCertificate(certificateId1, LocalDate.parse("2013-04-12").atStartOfDay()));
        entityManager.persist(buildCertificate(certificateId2, LocalDate.parse("2013-04-13").atStartOfDay()));
        entityManager.persist(buildCertificate(String.valueOf(certificateId++), LocalDate.parse("2013-04-14").atStartOfDay()));

        List<Certificate> certificate = certificateDao.findCertificate(CIVIC_REGISTRATION_NUMBER,
                singletonList(FK7263), LocalDate.parse("2013-04-12"), LocalDate.parse("2013-04-13"), null);

        assertEquals(2, certificate.size());
        assertEquals(certificateId1, certificate.get(0).getId()); // ordered by signedDate
        assertEquals(certificateId2, certificate.get(1).getId());
    }

    @Test
    public void testFindCertificateFromDateOnly() throws Exception {
        int certificateId = Integer.parseInt(CERTIFICATE_ID);
        final String certificateId1 = String.valueOf(certificateId++);
        final String certificateId2 = String.valueOf(certificateId++);
        final String certificateId3 = String.valueOf(certificateId++);
        final String certificateId4 = String.valueOf(certificateId++);

        entityManager.persist(buildCertificate(certificateId1, LocalDate.parse("2013-04-11").atStartOfDay()));
        entityManager.persist(buildCertificate(certificateId2, LocalDate.parse("2013-04-12").atStartOfDay()));
        entityManager.persist(buildCertificate(certificateId3, LocalDate.parse("2013-04-13").atStartOfDay()));
        entityManager.persist(buildCertificate(certificateId4, LocalDate.parse("2013-04-14").atStartOfDay()));

        List<Certificate> certificate = certificateDao.findCertificate(CIVIC_REGISTRATION_NUMBER,
                singletonList(FK7263), LocalDate.parse("2013-04-12"), null, null);

        assertEquals(3, certificate.size());
        assertEquals(certificateId2, certificate.get(0).getId()); // ordered by signedDate
        assertEquals(certificateId3, certificate.get(1).getId());
        assertEquals(certificateId4, certificate.get(2).getId());
    }

    @Test
    public void testFindCertificateToDateOnly() throws Exception {
        int certificateId = Integer.parseInt(CERTIFICATE_ID);
        final String certificateId1 = String.valueOf(certificateId++);
        final String certificateId2 = String.valueOf(certificateId++);
        final String certificateId3 = String.valueOf(certificateId++);
        final String certificateId4 = String.valueOf(certificateId++);

        entityManager.persist(buildCertificate(certificateId1, LocalDate.parse("2013-04-11").atStartOfDay()));
        entityManager.persist(buildCertificate(certificateId2, LocalDate.parse("2013-04-12").atStartOfDay()));
        entityManager.persist(buildCertificate(certificateId3, LocalDate.parse("2013-04-13").atStartOfDay()));
        entityManager.persist(buildCertificate(certificateId4, LocalDate.parse("2013-04-14").atStartOfDay()));

        List<Certificate> certificate = certificateDao.findCertificate(CIVIC_REGISTRATION_NUMBER,
                singletonList(FK7263), null, LocalDate.parse("2013-04-13"), null);

        assertEquals(3, certificate.size());
        assertEquals(certificateId1, certificate.get(0).getId()); // ordered by signedDate
        assertEquals(certificateId2, certificate.get(1).getId());
        assertEquals(certificateId3, certificate.get(2).getId());
    }

    @Test
    public void testStore() throws PersistenceException {

        assertNull(certificateDao.getCertificate(CIVIC_REGISTRATION_NUMBER, CERTIFICATE_ID));

        Certificate certificate = buildCertificate();
        certificateDao.store(certificate);

        assertNotNull(certificateDao.getCertificate(CIVIC_REGISTRATION_NUMBER, CERTIFICATE_ID));
    }

    @Test(expected = PersistenceException.class)
    public void testUpdateStatusForWrongCertificate() throws PersistenceException {
        certificateDao.updateStatus("<unknownCertId>", new Personnummer("<unknownPersonnummer>"), CertificateState.SENT, "fk",
                null);
    }

    @Test
    public void testGetCertificateForUnknownCertificate() throws PersistenceException {
        certificateDao.store(buildCertificate());
        assertNull(certificateDao.getCertificate(new Personnummer("<unknownPersonnummer>"), "<unknownCertId>"));
    }

    @Test
    public void testGetCertificateForPnrWithoutDash() throws PersistenceException {
        certificateDao.store(buildCertificate());
        assertNotNull(certificateDao.getCertificate(new Personnummer("190011223344"), "123456"));
    }

    @Test(expected = PersistenceException.class)
    public void testGetCertificateForWrongCivicRegistrationNumber() throws PersistenceException {
        certificateDao.store(buildCertificate());
        certificateDao.getCertificate(new Personnummer("<another civic registration number>"), CERTIFICATE_ID);
    }

    @Test
    public void testUpdateStatusForDifferentPatients() throws PersistenceException {

        // store a certificate for reference patient
        Certificate certificate = buildCertificate();
        entityManager.persist(certificate);

        assertEquals(0, certificate.getStates().size());

        try {
            certificateDao.updateStatus(CERTIFICATE_ID, new Personnummer("another patient"), RECEIVED, "fk", null);
            fail("Exception expected.");
        } catch (PersistenceException e) {
            // Empty
        }

        assertEquals(0, certificate.getStates().size());
        certificateDao.updateStatus(CERTIFICATE_ID, CIVIC_REGISTRATION_NUMBER, RECEIVED, "fk", null);
        assertEquals(1, certificate.getStates().size());
    }

    @Test
    public void testUpdateStatusStatus() throws PersistenceException {

        // store a certificate for patient 19101112-1314
        Certificate certificate = buildCertificate();
        entityManager.persist(certificate);

        assertEquals(0, certificate.getStates().size());

        certificateDao.updateStatus(CERTIFICATE_ID, CIVIC_REGISTRATION_NUMBER, DELETED, "fk", null);

        assertEquals(1, certificate.getStates().size());
        assertEquals(DELETED, certificate.getStates().get(0).getState());
        assertEquals("fk", certificate.getStates().get(0).getTarget());
        assertNotNull(certificate.getStates().get(0).getTimestamp());
    }

    @Test
    public void testUpdateStatusPnrWithoutDash() throws PersistenceException {

        // store a certificate for patient 19101112-1314
        Certificate certificate = buildCertificate();
        entityManager.persist(certificate);

        assertEquals(0, certificate.getStates().size());

        certificateDao.updateStatus(CERTIFICATE_ID, new Personnummer("190011223344"), DELETED, "fk", null);

        assertEquals(1, certificate.getStates().size());
        assertEquals(DELETED, certificate.getStates().get(0).getState());
        assertEquals("fk", certificate.getStates().get(0).getTarget());
        assertNotNull(certificate.getStates().get(0).getTimestamp());
    }

    @Test(expected = PersistenceException.class)
    public void testUpdateStatusNoPnrForWrongCertificate() throws PersistenceException {
        certificateDao.updateStatus("<unknownCertId>", CertificateState.SENT, "fk", null);
    }

    @Test
    public void testUpdateStatusNoPnr() throws PersistenceException {
        Certificate certificate = buildCertificate();
        entityManager.persist(certificate);

        assertEquals(0, certificate.getStates().size());

        certificateDao.updateStatus(CERTIFICATE_ID, DELETED, "fk", null);

        assertEquals(1, certificate.getStates().size());
        assertEquals(DELETED, certificate.getStates().get(0).getState());
        assertEquals("fk", certificate.getStates().get(0).getTarget());
        assertNotNull(certificate.getStates().get(0).getTimestamp());
    }

    @Test
    public void testStatusOrderForGetCertificate() throws PersistenceException {

        Certificate certificate = buildCertificate();
        entityManager.persist(certificate);

        LocalDateTime lastMonth = LocalDateTime.now().minusWeeks(4);
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        certificateDao.updateStatus(CERTIFICATE_ID, CIVIC_REGISTRATION_NUMBER, DELETED, "fk", lastWeek);
        certificateDao.updateStatus(CERTIFICATE_ID, CIVIC_REGISTRATION_NUMBER, SENT, "fk", lastMonth);
        certificateDao.updateStatus(CERTIFICATE_ID, CIVIC_REGISTRATION_NUMBER, RESTORED, "fk", yesterday);

        entityManager.flush();
        entityManager.clear();

        certificate = certificateDao.getCertificate(CIVIC_REGISTRATION_NUMBER, CERTIFICATE_ID);

        assertEquals(3, certificate.getStates().size());

        assertEquals(yesterday, certificate.getStates().get(0).getTimestamp());
        assertEquals(lastWeek, certificate.getStates().get(1).getTimestamp());
        assertEquals(lastMonth, certificate.getStates().get(2).getTimestamp());
    }

    @Test
    public void testStoreOriginalCertificate() {
        long originalCertId = certificateDao.storeOriginalCertificate(new OriginalCertificate(LocalDateTime.now(),
                "Some text", null));

        OriginalCertificate original = entityManager.find(OriginalCertificate.class, originalCertId);
        assertNotNull(original);
        assertEquals("Some text", original.getDocument());
    }

    @Test
    public void testStoreOriginalCertificateWithCertificate() {
        Certificate certificate = buildCertificate();
        certificateDao.store(certificate);
        long originalCertId = certificateDao.storeOriginalCertificate(new OriginalCertificate(LocalDateTime.now(),
                "Some text", certificate));

        OriginalCertificate original = entityManager.find(OriginalCertificate.class, originalCertId);
        assertNotNull(original);
        assertEquals("Some text", original.getDocument());
    }

}
