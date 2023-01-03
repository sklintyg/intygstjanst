/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.service.builder;

import com.google.common.base.Joiner;
import java.util.List;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateRecipientType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

/**
 * Recipient object.
 *
 * @author erik
 */
public class RecipientBuilder {

    private String logicalAddress;
    private String name;
    private String id;
    private String certificateTypes;
    private boolean active;
    private boolean trusted;

    public Recipient build() {
        return new Recipient(logicalAddress, name, id, CertificateRecipientType.HUVUDMOTTAGARE.name(), certificateTypes, active, trusted);
    }

    public String getLogicalAddress() {
        return logicalAddress;
    }

    public RecipientBuilder setLogicalAddress(String logicalAddress) {
        this.logicalAddress = logicalAddress;
        return this;
    }

    public String getName() {
        return name;
    }

    public RecipientBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public String getId() {
        return id;
    }

    public RecipientBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public String getCertificateTypes() {
        return certificateTypes;
    }

    public RecipientBuilder setCertificateTypes(String certificateTypes) {
        this.certificateTypes = certificateTypes;
        return this;
    }

    public RecipientBuilder setCertificateTypes(List<String> certificateTypes) {
        this.certificateTypes = Joiner.on(Recipient.SEPARATOR).join(certificateTypes);
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public RecipientBuilder setActive(boolean active) {
        this.active = active;
        return this;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public RecipientBuilder setTrusted(boolean trusted) {
        this.trusted = trusted;
        return this;
    }
}
