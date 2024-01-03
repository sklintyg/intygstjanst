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
package se.inera.intyg.intygstjanst.persistence.model.dao.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static se.inera.intyg.common.support.model.CertificateState.DELETED;
import static se.inera.intyg.common.support.model.CertificateState.RECEIVED;
import static se.inera.intyg.common.support.model.CertificateState.RESTORED;
import static se.inera.intyg.common.support.model.CertificateState.SENT;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.persistence.exception.PersistenceException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.schemas.contract.Personnummer;

public class CertificateDaoImplTest extends TestSupport {

    private final String CERTIFICATE_ID_UNKNOWN = "unknownId";
    private final String CIVIC_REGISTRATION_NUMBER_UNKNOWN = "19440223-1641";

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
            Collections.<String>emptyList(), null, null, null);
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
    public void testFindCertificateWithCareUnitAsBase() {

        // two certificates with different care units IDs
        Certificate west = buildCertificate("1");
        west.setCareUnitId("west");
        entityManager.persist(west);

        Certificate east = buildCertificate("2");
        east.setCareUnitId("east");
        entityManager.persist(east);

        Certificate north = buildCertificate("3");
        north.setCareUnitId("north");
        entityManager.persist(north);

        Certificate east2 = buildCertificate("4");
        east2.setCareUnitId("east");
        entityManager.persist(east2);

        // no matching care unit ID, no certificates
        List<Certificate> certificate = certificateDao
            .findCertificate(Collections.singletonList("center"), null, null, null);
        assertEquals(0, certificate.size());

        // filter by 'west' -> only return 'west'-based intyg
        certificate = certificateDao.findCertificate(Collections.singletonList("west"), null, null, null);
        assertEquals(1, certificate.size());
        assertEquals(west, certificate.get(0));

        // filter by 'west' and 'east' -> both 'west'- and 'east'-based intyg
        certificate = certificateDao.findCertificate(Arrays.asList("west", "east"), null, null, null);
        assertEquals(3, certificate.size());
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
        certificateDao.updateStatus(CERTIFICATE_ID_UNKNOWN, createPnr(CIVIC_REGISTRATION_NUMBER_UNKNOWN), CertificateState.SENT, "FKASSA",
            null);
    }

    @Test
    public void testGetCertificateForUnknownCertificate() throws PersistenceException {
        certificateDao.store(buildCertificate());
        assertNull(certificateDao.getCertificate(createPnr(CIVIC_REGISTRATION_NUMBER_UNKNOWN), CERTIFICATE_ID_UNKNOWN));
    }

    @Test
    public void testGetCertificateForPnrWithoutDash() throws PersistenceException {
        certificateDao.store(buildCertificate());
        assertNotNull(certificateDao.getCertificate(CIVIC_REGISTRATION_NUMBER_NO_DASH, "123456"));
    }

    @Test(expected = PersistenceException.class)
    public void testGetCertificateForWrongCivicRegistrationNumber() throws PersistenceException {
        certificateDao.store(buildCertificate());
        certificateDao.getCertificate(createPnr(CIVIC_REGISTRATION_NUMBER_UNKNOWN), CERTIFICATE_ID);
    }

    @Test
    public void testUpdateStatusForDifferentPatients() throws PersistenceException {

        // store a certificate for reference patient
        Certificate certificate = buildCertificate();
        entityManager.persist(certificate);

        assertEquals(0, certificate.getStates().size());

        try {
            certificateDao.updateStatus(CERTIFICATE_ID, createPnr(CIVIC_REGISTRATION_NUMBER_UNKNOWN), RECEIVED, "FKASSA", null);
            fail("Exception expected.");
        } catch (PersistenceException e) {
            // Empty
        }

        assertEquals(0, certificate.getStates().size());
        certificateDao.updateStatus(CERTIFICATE_ID, CIVIC_REGISTRATION_NUMBER, RECEIVED, "FKASSA", null);
        assertEquals(1, certificate.getStates().size());
    }

    @Test
    public void testUpdateStatusStatus() throws PersistenceException {

        // store a certificate for patient 19101112-1314
        Certificate certificate = buildCertificate();
        entityManager.persist(certificate);

        assertEquals(0, certificate.getStates().size());

        certificateDao.updateStatus(CERTIFICATE_ID, CIVIC_REGISTRATION_NUMBER, DELETED, "FKASSA", null);

        assertEquals(1, certificate.getStates().size());
        assertEquals(DELETED, certificate.getStates().get(0).getState());
        assertEquals("FKASSA", certificate.getStates().get(0).getTarget());
        assertNotNull(certificate.getStates().get(0).getTimestamp());
    }

    @Test
    public void testUpdateStatusPnrWithoutDash() throws PersistenceException {

        // store a certificate for patient 19101112-1314
        Certificate certificate = buildCertificate();
        entityManager.persist(certificate);

        assertEquals(0, certificate.getStates().size());

        certificateDao.updateStatus(CERTIFICATE_ID, CIVIC_REGISTRATION_NUMBER_NO_DASH, DELETED, "FKASSA", null);

        assertEquals(1, certificate.getStates().size());
        assertEquals(DELETED, certificate.getStates().get(0).getState());
        assertEquals("FKASSA", certificate.getStates().get(0).getTarget());
        assertNotNull(certificate.getStates().get(0).getTimestamp());
    }

    @Test(expected = PersistenceException.class)
    public void testUpdateStatusNoPnrForWrongCertificate() throws PersistenceException {
        certificateDao.updateStatus(CERTIFICATE_ID_UNKNOWN, CertificateState.SENT, "FKASSA", null);
    }

    @Test
    public void testUpdateStatusNoPnr() throws PersistenceException {
        Certificate certificate = buildCertificate();
        entityManager.persist(certificate);

        assertEquals(0, certificate.getStates().size());

        certificateDao.updateStatus(CERTIFICATE_ID, DELETED, "FKASSA", null);

        assertEquals(1, certificate.getStates().size());
        assertEquals(DELETED, certificate.getStates().get(0).getState());
        assertEquals("FKASSA", certificate.getStates().get(0).getTarget());
        assertNotNull(certificate.getStates().get(0).getTimestamp());
    }

    @Test
    public void testStatusOrderForGetCertificate() throws PersistenceException {

        Certificate certificate = buildCertificate();
        entityManager.persist(certificate);

        LocalDateTime lastMonth = LocalDateTime.now().minusWeeks(4).truncatedTo(ChronoUnit.MICROS);
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1).truncatedTo(ChronoUnit.MICROS);
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MICROS);

        certificateDao.updateStatus(CERTIFICATE_ID, CIVIC_REGISTRATION_NUMBER, DELETED, "FKASSA", lastWeek);
        certificateDao.updateStatus(CERTIFICATE_ID, CIVIC_REGISTRATION_NUMBER, SENT, "FKASSA", lastMonth);
        certificateDao.updateStatus(CERTIFICATE_ID, CIVIC_REGISTRATION_NUMBER, RESTORED, "FKASSA", yesterday);

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

    @Test
    public void shouldEraseCertificates() {
        final var certificate1 = buildCertificate("certificate1");
        final var certificate2 = buildCertificate("certificate2");
        certificateDao.store(certificate1);
        certificateDao.store(certificate2);

        final var erasedCount = certificateDao.eraseCertificates(List.of(certificate1.getId(), certificate2.getId()), "5678");

        assertEquals(2, erasedCount);
        assertNull(entityManager.find(Certificate.class, certificate1.getId()));
        assertNull(entityManager.find(Certificate.class, certificate2.getId()));
    }

    @Test
    public void shouldReturnZeroErasedIfCertificateNotFound() {
        final var erasedCount = certificateDao.eraseCertificates(List.of("non-existant"), "5678");
        assertEquals(0, erasedCount);
    }

    private Personnummer createPnr(String pnr) {
        return Personnummer.createPersonnummer(pnr).get();
    }

}
