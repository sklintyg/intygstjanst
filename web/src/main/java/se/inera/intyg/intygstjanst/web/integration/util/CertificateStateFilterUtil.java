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
package se.inera.intyg.intygstjanst.web.integration.util;

import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.api.CertificateStateHolder;

import java.util.Objects;

/**
 * Utility class containing business logic for what status removed for different consumers of the soap api.
 *
 * Created by carlf on 10/04/17.
 */
public final class CertificateStateFilterUtil {

    private CertificateStateFilterUtil() {
    }

    /**
     * A filters for status items, depending on consumer of the api (part) and the default recipient (huvudmottagare) of
     * the intyg type of the related intyg.
     *
     * @param status
     *            the status to be either kept or removed
     * @param part
     *            the consumer of the api
     * @return whether to keep or the given status item
     */
    public static boolean filter(CertificateStateHolder status, String part) {
        switch (part) {
        case "INVANA":
            // Invanaren: alla statusar (INTYG-3629).
            return true;
        case "HSVARD":
            // Varden: alla statusar förutom Arkiverat, Aterstallt
            if (status.getState() == CertificateState.DELETED || status.getState() == CertificateState.RESTORED) {
                return false;
            }
            return true;
        default:
            // FKASSA och ovriga parter
            if (status.getState() == CertificateState.DELETED || status.getState() == CertificateState.RESTORED) {
                return false;
            }
            if (status.getState() == CertificateState.SENT && !Objects.equals(part, status.getTarget())) {
                // Should not receive SENT-status items if consumer of api are not the recipient.
                return false;
            }
            return true;

        }
    }
}
