/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.ifv.insuranceprocess.certificate.v1.CertificateStatusType;
import se.inera.ifv.insuranceprocess.certificate.v1.StatusType;

/**
 * @author andreaskaltenbach
 */
public final class CertificateStateHistoryEntryConverter {

    private CertificateStateHistoryEntryConverter() {
    }

    public static List<CertificateStatusType> toCertificateStatusType(List<CertificateStateHistoryEntry> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }

        List<CertificateStatusType> states = new ArrayList<>();
        for (CertificateStateHistoryEntry state : source) {
            states.add(toCertificateStatusType(state));
        }
        return states;
    }

    private static CertificateStatusType toCertificateStatusType(CertificateStateHistoryEntry source) {
        CertificateStatusType status = new CertificateStatusType();
        status.setTarget(source.getTarget());
        status.setTimestamp(source.getTimestamp());
        status.setType(StatusType.valueOf(source.getState().name()));
        return status;
    }
}
