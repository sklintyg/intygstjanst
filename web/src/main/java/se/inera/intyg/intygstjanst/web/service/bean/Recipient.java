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
import static org.springframework.util.Assert.notEmpty;

import com.google.common.base.Joiner;

import java.util.Arrays;
import java.util.List;

/**
 * Recipient object.
 *
 * @author erik
 *
 */
public class Recipient {

    public static final String SEPARATOR = ",";

    private final String logicalAddress;
    private final String name;
    private final String id;
    private final List<String> certificateTypes;

    /**
     * Constructor for recipient object.
     * @param logicalAddress a recipient's logical address
     * @param name a recipient's name
     * @param id a recipient's identifier
     * @param certificateTypes a comma-separated string of the type of certificates this recipient support
     */
    public Recipient(String logicalAddress, String name, String id, String certificateTypes) {
        hasText(logicalAddress, "logicalAddress must not be empty");
        hasText(name, "name must not be empty");
        hasText(id, "id must not be empty");
        hasText(certificateTypes, "certificateTypes must not be empty");

        this.logicalAddress = logicalAddress;
        this.name = name;
        this.id = id;
        this.certificateTypes = Arrays.asList(certificateTypes.split(SEPARATOR));
    }

    /**
     * Constructor for recipient object.
     * @param logicalAddress a recipient's logical address
     * @param name a recipient's name
     * @param id a recipient's identifier
     * @param certificateTypes a list of the type of certificates this recipient support
     */
    public Recipient(String logicalAddress, String name, String id, List<String> certificateTypes) {
        hasText(logicalAddress, "logicalAddress must not be empty");
        hasText(name, "name must not be empty");
        hasText(id, "id must not be empty");
        notEmpty(certificateTypes, "certificateTypes must have at least one certificate type");

        this.logicalAddress = logicalAddress;
        this.name = name;
        this.id = id;
        this.certificateTypes = certificateTypes;
    }

    public String getLogicalAddress() {
        return logicalAddress;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<String> getCertificateTypes() {
        return certificateTypes;
    }

    @Override
    public String toString() {
        return "logicalAddress: " + logicalAddress
                + " name: " + name
                + " id: " + id
                + " certificateTypes: " + Joiner.on(SEPARATOR).join(certificateTypes);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        result = prime * result + logicalAddress.hashCode();
        result = prime * result + name.hashCode();
        result = prime * result + Joiner.on(SEPARATOR).join(certificateTypes).hashCode();

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

        Recipient other = (Recipient) obj;

        if (!id.equals(other.id)) {
            return false;
        } else if (!logicalAddress.equals(other.logicalAddress)) {
            return false;
        } else if (!name.equals(other.name)) {
            return false;
        } else {
            String one = Joiner.on(SEPARATOR).join(certificateTypes);
            String two = Joiner.on(SEPARATOR).join(other.certificateTypes);
            return one.equals(two);
        }
    }
}
