/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

public class RecipientCertificateType extends CertificateType {

    private final String recipientId;

    public RecipientCertificateType(String recipientId, String certificateTypeId) {
        super(certificateTypeId);
        hasText(recipientId, "recipientId must not be empty");
        this.recipientId = recipientId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getCertificateTypeId().hashCode();
        result = prime * result + getRecipientId().hashCode();
        return result;
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

        RecipientCertificateType other = (RecipientCertificateType) obj;
        if (!getCertificateTypeId().equals(other.getCertificateTypeId())) {
            return false;
        } else {
            return getRecipientId().equals(other.getRecipientId());
        }
    }
}
