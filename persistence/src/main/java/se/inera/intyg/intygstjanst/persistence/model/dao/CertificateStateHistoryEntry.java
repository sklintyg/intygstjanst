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

import com.google.common.collect.Ordering;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
import java.util.Objects;
import se.inera.intyg.common.support.model.CertificateState;

/**
 * @author andreaskaltenbach
 */
@Embeddable
public class CertificateStateHistoryEntry {

    @Column(name = "TARGET", nullable = false)
    private String target;

    @Column(name = "STATE")
    @Enumerated(EnumType.STRING)
    private CertificateState state;

    @Column(name = "TIMESTAMP")
    private LocalDateTime timestamp;

    private static final Ordering<LocalDateTime> ORDERING_DESC_TIME_NULL_LAST = Ordering.<LocalDateTime>natural().reverse().nullsFirst();

    static final Ordering<CertificateStateHistoryEntry> BY_TIMESTAMP_DESC = new Ordering<>() {
        @Override
        public int compare(@Nonnull CertificateStateHistoryEntry left, @Nonnull CertificateStateHistoryEntry right) {
            return ORDERING_DESC_TIME_NULL_LAST.compare(left.timestamp, right.timestamp);
        }
    };

    public CertificateStateHistoryEntry() {
        // default constructor for hibernate
    }

    public CertificateStateHistoryEntry(String target, CertificateState state, LocalDateTime timestamp) {
        this.target = target;
        this.state = state;
        this.timestamp = Objects.requireNonNullElseGet(timestamp, LocalDateTime::now);
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public CertificateState getState() {
        return state;
    }

    public void setState(CertificateState state) {
        this.state = state;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "CertificateStateHistoryEntry{"
            + "target='" + target + '\''
            + ", state=" + state + ", timestamp="
            + timestamp + '}';
    }
}
