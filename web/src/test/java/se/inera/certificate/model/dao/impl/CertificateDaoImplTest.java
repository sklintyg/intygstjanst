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
package se.inera.certificate.model.dao.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static se.inera.certificate.model.CertificateState.DELETED;
import static se.inera.certificate.model.CertificateState.RECEIVED;
import static se.inera.certificate.model.CertificateState.RESTORED;
import static se.inera.certificate.model.CertificateState.SENT;
import static se.inera.certificate.support.CertificateFactory.CERTIFICATE_ID;
import static se.inera.certificate.support.CertificateFactory.CIVIC_REGISTRATION_NUMBER;
import static se.inera.certificate.support.CertificateFactory.FK7263;
import static se.inera.certificate.support.CertificateFactory.buildCertificate;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import se.inera.certificate.exception.InvalidCertificateIdentifierException;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.model.dao.CertificateDao;
import se.inera.certificate.model.dao.OriginalCertificate;
import se.inera.certificate.support.CertificateFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:persistence-config.xml" })
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
    public void testFindCertificateWithValidityFilter() throws Exception {
        int certificateId = Integer.parseInt(CERTIFICATE_ID);

        entityManager.persist(buildCertificate(String.valueOf(certificateId++), "2013-04-13", "2013-05-13"));
        entityManager.persist(buildCertificate(String.valueOf(certificateId++), "2013-03-13", "2013-04-12"));
        entityManager.persist(buildCertificate(String.valueOf(certificateId++), "2013-05-13", "2013-06-13"));

        List<Certificate> certificate = certificateDao.findCertificate(CIVIC_REGISTRATION_NUMBER,
                singletonList(FK7263), new LocalDate("2013-04-01"), new LocalDate("2013-04-15"), null);

        assertEquals(2, certificate.size());
    }

    @Test
    public void testGetDocument() {

        Certificate certificate = CertificateFactory.buildCertificate("1", "2013-04-25", "2013-05-25");
        certificate.setCivicRegistrationNumber(CIVIC_REGISTRATION_NUMBER);
        certificate.setSignedDate(new LocalDateTime("2013-04-24"));
        certificateDao.store(certificate);

        entityManager.flush();
        entityManager.clear();

        Certificate storedCertificate = certificateDao.getCertificate(CIVIC_REGISTRATION_NUMBER, "1");

        assertEquals(new LocalDateTime("2013-04-24"), storedCertificate.getSignedDate());
        assertEquals("2013-04-25", storedCertificate.getValidFromDate());
        assertEquals("2013-05-25", storedCertificate.getValidToDate());

        String document = storedCertificate.getDocument();
        assertEquals("{\"name\":\"Some JSON\"}", document);
    }

    @Test
    public void testStore() {

        assertNull(certificateDao.getCertificate(CIVIC_REGISTRATION_NUMBER, CERTIFICATE_ID));

        Certificate certificate = buildCertificate();
        certificateDao.store(certificate);

        assertNotNull(certificateDao.getCertificate(CIVIC_REGISTRATION_NUMBER, CERTIFICATE_ID));
    }

    @Test(expected = InvalidCertificateIdentifierException.class)
    public void testUpdateStatusForWrongCertificate() {
        certificateDao.updateStatus("<unknownCertId>", "<unknownPersonnummer>", CertificateState.IN_PROGRESS, "fk",
                null);
    }

    @Test
    public void testGetCertificateForUnknownCertificate() {
        certificateDao.store(buildCertificate());
        assertNull(certificateDao.getCertificate("<unknownCertId>", "<unknownPersonnummer>"));
    }

    @Test(expected = InvalidCertificateIdentifierException.class)
    public void testGetCertificateForWrongCivicRegistrationNumber() {
        certificateDao.store(buildCertificate());
        certificateDao.getCertificate("<another civic registration number>", CERTIFICATE_ID);
    }

    @Test
    public void testUpdateStatusForDifferentPatients() {

        // store a certificate for reference patient
        Certificate certificate = buildCertificate();
        entityManager.persist(certificate);

        assertEquals(0, certificate.getStates().size());

        try {
            certificateDao.updateStatus(CERTIFICATE_ID, "another patient", RECEIVED, "fk", null);
            fail("Exception expected.");
        } catch (InvalidCertificateIdentifierException e) {
            // Empty
        }

        assertEquals(0, certificate.getStates().size());
        certificateDao.updateStatus(CERTIFICATE_ID, CIVIC_REGISTRATION_NUMBER, RECEIVED, "fk", null);
        assertEquals(1, certificate.getStates().size());
    }

    @Test
    public void testUpdateStatusStatus() {

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
    public void testStatusOrderForGetCertificate() {

        Certificate certificate = buildCertificate();
        entityManager.persist(certificate);

        LocalDateTime lastMonth = new LocalDateTime().minusWeeks(4);
        LocalDateTime lastWeek = new LocalDateTime().minusWeeks(1);
        LocalDateTime yesterday = new LocalDateTime().minusDays(1);

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
        assertEquals(CertificateFactory.CERTIFICATE_DOCUMENT, original.getCertificate().getDocument());
    }
}