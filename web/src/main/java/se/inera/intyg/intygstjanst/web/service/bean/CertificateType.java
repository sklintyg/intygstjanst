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

package se.inera.intyg.intygstjanst.web.service.bean;

import static org.springframework.util.Assert.hasText;

public class CertificateType {

    private final String certificateTypeId;

    public CertificateType(String certificateTypeId) {
        hasText(certificateTypeId, "certificateTypeId must not be empty");
        this.certificateTypeId = certificateTypeId;
    }

    public String getCertificateTypeId() {
        return certificateTypeId;
    }

    @Override
    public String toString() {
        return certificateTypeId;
    }

    @Override
    public int hashCode() {
        return certificateTypeId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        CertificateType other = (CertificateType) obj;
        return certificateTypeId.equals(other.certificateTypeId);
    }
}
