/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.persistence.model.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Test;

import se.inera.intyg.common.support.model.CertificateState;

public class CertificateTest {

    @Test
    public void isDeletedTest() {
        Certificate certificate = new Certificate();
        certificate.addState(new CertificateStateHistoryEntry("INVANA", CertificateState.DELETED, LocalDateTime.now()));
        assertTrue(certificate.isDeleted());
    }

    @Test
    public void isDeletedThenRestoredTest() {
        Certificate certificate = new Certificate();
        certificate.addState(new CertificateStateHistoryEntry("INVANA", CertificateState.DELETED, LocalDateTime.now().minusSeconds(10)));
        certificate.addState(new CertificateStateHistoryEntry("INVANA", CertificateState.RESTORED, LocalDateTime.now()));
        assertFalse(certificate.isDeleted());
    }

    @Test
    public void isDeletedThenRestoredAndArchivedAgainTest() {
        Certificate certificate = new Certificate();
        certificate.addState(new CertificateStateHistoryEntry("INVANA", CertificateState.DELETED, LocalDateTime.now().minusSeconds(10)));
        certificate.addState(new CertificateStateHistoryEntry("INVANA", CertificateState.DELETED, LocalDateTime.now()));
        certificate.addState(new CertificateStateHistoryEntry("INVANA", CertificateState.RESTORED, LocalDateTime.now().minusSeconds(5)));
        assertTrue(certificate.isDeleted());
    }

    @Test
    public void isDeletedMultipleStatusesTest() {
        Certificate certificate = new Certificate();
        certificate.addState(new CertificateStateHistoryEntry("FKASSA", CertificateState.SENT, LocalDateTime.now().minusSeconds(10)));
        certificate.addState(new CertificateStateHistoryEntry("INVANA", CertificateState.DELETED, LocalDateTime.now().minusSeconds(5)));
        certificate.addState(new CertificateStateHistoryEntry("FKASSA", CertificateState.RECEIVED, LocalDateTime.now()));
        assertTrue(certificate.isDeleted());
    }

    @Test
    public void isDeletedNoArchivedStatusesTest() {
        Certificate certificate = new Certificate();
        certificate.addState(new CertificateStateHistoryEntry("FKASSA", CertificateState.SENT, LocalDateTime.now()));
        certificate.addState(new CertificateStateHistoryEntry("FKASSA", CertificateState.RECEIVED, LocalDateTime.now()));
        certificate.addState(new CertificateStateHistoryEntry("HSVARD", CertificateState.CANCELLED, LocalDateTime.now()));
        assertFalse(certificate.isDeleted());
    }

    @Test
    public void isDeletedNoStatusesTest() {
        assertFalse(new Certificate().isDeleted());
    }

    @Test
    public void isRevokedTest() {
        Certificate certificate = new Certificate();
        certificate.addState(new CertificateStateHistoryEntry("HSVARD", CertificateState.RECEIVED, LocalDateTime.now()));
        certificate.addState(new CertificateStateHistoryEntry("HSVARD", CertificateState.CANCELLED, LocalDateTime.now()));
        certificate.addState(new CertificateStateHistoryEntry("FKASSA", CertificateState.SENT, LocalDateTime.now()));
        assertTrue(certificate.isRevoked());
    }

    @Test
    public void isRevokedFalseTest() {
        Certificate certificate = new Certificate();
        certificate.addState(new CertificateStateHistoryEntry("HSVARD", CertificateState.RECEIVED, LocalDateTime.now()));
        certificate.addState(new CertificateStateHistoryEntry("FKASSA", CertificateState.SENT, LocalDateTime.now()));
        assertFalse(certificate.isRevoked());
    }

    @Test
    public void isRevokedNoStatusesTest() {
        assertFalse(new Certificate().isRevoked());
    }

    @Test
    public void isAlreadySentTest() {
        Certificate certificate = new Certificate();
        certificate.addState(new CertificateStateHistoryEntry("HSVARD", CertificateState.RECEIVED, LocalDateTime.now()));
        certificate.addState(new CertificateStateHistoryEntry("HSVARD", CertificateState.CANCELLED, LocalDateTime.now()));
        certificate.addState(new CertificateStateHistoryEntry("FKASSA", CertificateState.SENT, LocalDateTime.now()));
        assertTrue(certificate.isAlreadySent("FKASSA"));
        assertFalse(certificate.isAlreadySent("TRANSP"));
    }

    @Test
    public void isAlreadySentFalseTest() {
        Certificate certificate = new Certificate();
        certificate.addState(new CertificateStateHistoryEntry("HSVARD", CertificateState.RECEIVED, LocalDateTime.now()));
        assertFalse(certificate.isAlreadySent("FKASSA"));
        assertFalse(certificate.isAlreadySent("TRANSP"));
    }

    @Test
    public void isAlreadySentNoStatusesTest() {
        assertFalse(new Certificate().isAlreadySent("FKASSA"));
    }
}
