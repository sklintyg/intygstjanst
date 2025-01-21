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
 */
public class Recipient {

    public static final String SEPARATOR = ",";

    private String logicalAddress;
    private String name;
    private String id;
    private CertificateRecipientType recipientType;
    private List<String> certificateTypes;
    private boolean active;
    private boolean trusted;

    public Recipient() {
    }

    /**
     * Constructor for recipient object.
     *
     * @param logicalAddress a recipient's logical address
     * @param name a recipient's name
     * @param id a recipient's identifier
     * @param recipientType a recipient's type: HUVUDMOTTAGARE, DIREKTMOTTAGARE, MOTTAGARE
     * @param certificateTypes a comma-separated string of the type of certificates this recipient support
     * @param active if the recipient is active
     * @param trusted if the recipient can be trusted with information about sekretessmarkerade patients
     */
    public Recipient(String logicalAddress, String name, String id, String recipientType, String certificateTypes, boolean active,
        boolean trusted) {
        hasText(logicalAddress, "logicalAddress must not be empty");
        hasText(name, "name must not be empty");
        hasText(id, "id must not be empty");
        hasText(recipientType, "id must not be empty");
        hasText(certificateTypes, "certificateTypes must not be empty");

        this.logicalAddress = logicalAddress;
        this.name = name;
        this.id = id;
        this.recipientType = CertificateRecipientType.valueOf(recipientType);
        this.certificateTypes = Arrays.asList(certificateTypes.split(SEPARATOR));
        this.active = active;
        this.trusted = trusted;
    }

    /**
     * Constructor for recipient object.
     *
     * @param logicalAddress a recipient's logical address
     * @param name a recipient's name
     * @param id a recipient's identifier
     * @param recipientType a recipient's type: HUVUDMOTTAGARE, DIREKTMOTTAGARE, MOTTAGARE
     * @param certificateTypes a list of the type of certificates this recipient support
     * @param active if the recipient is active
     * @param trusted if the recipient can be trusted with information about sekretessmarkerade patients
     */
    public Recipient(String logicalAddress, String name, String id, String recipientType, List<String> certificateTypes, boolean active,
        boolean trusted) {
        hasText(logicalAddress, "logicalAddress must not be empty");
        hasText(name, "name must not be empty");
        hasText(id, "id must not be empty");
        hasText(id, "recipientType must not be empty");
        notEmpty(certificateTypes, "certificateTypes must have at least one certificate type");

        this.logicalAddress = logicalAddress;
        this.name = name;
        this.id = id;
        this.recipientType = CertificateRecipientType.valueOf(recipientType);
        this.certificateTypes = certificateTypes;
        this.active = active;
        this.trusted = trusted;

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

    public CertificateRecipientType getRecipientType() {
        return recipientType;
    }

    public List<String> getCertificateTypes() {
        return certificateTypes;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    @Override
    public String toString() {
        return "logicalAddress: " + logicalAddress
            + " name: " + name
            + " id: " + id
            + " recipientType: " + recipientType.name()
            + " certificateTypes: " + Joiner.on(SEPARATOR).join(certificateTypes)
            + " active: " + active
            + " trusted: " + trusted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Recipient)) {
            return false;
        }

        Recipient recipient = (Recipient) o;

        if (active != recipient.active) {
            return false;
        }
        if (trusted != recipient.trusted) {
            return false;
        }
        if (!logicalAddress.equals(recipient.logicalAddress)) {
            return false;
        }
        if (!name.equals(recipient.name)) {
            return false;
        }
        if (!id.equals(recipient.id)) {
            return false;
        }
        if (!recipientType.equals(recipient.recipientType)) {
            return false;
        }
        return certificateTypes.equals(recipient.certificateTypes);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = logicalAddress.hashCode();
        result = prime * result + name.hashCode();
        result = prime * result + id.hashCode();
        result = prime * result + recipientType.hashCode();
        result = prime * result + certificateTypes.hashCode();
        result = prime * result + (active ? 1 : 0);
        result = prime * result + (trusted ? 1 : 0);
        return result;
    }
}
