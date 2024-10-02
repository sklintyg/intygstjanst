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
package se.inera.intyg.intygstjanst.persistence.model.dao;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.hibernate.annotations.Type;

/**
 * @author johannesc
 */
@Entity
@Table(name = "ORIGINAL_CERTIFICATE")
public class OriginalCertificate {

    /**
     * Just needed for JPA compliance.
     */
    @Id
    @GeneratedValue
    private long id;

    /**
     * Time this certificate was received.
     */
    @Column(name = "received", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime received;

    /**
     * Certificate JAXB serialization.
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "DOCUMENT")
    private byte[] document;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CERTIFICATE_ID")
    private Certificate certificate;

    public OriginalCertificate() {
        // default constructor for hibernate
    }

    public OriginalCertificate(LocalDateTime received, String document, Certificate certificate) {
        this.received = received;
        this.document = toBytes(document);
        this.certificate = certificate;
    }

    public LocalDateTime getReceived() {
        return received;
    }

    public void setReceived(LocalDateTime received) {
        this.received = received;
    }

    public void setDocument(String document) {
        this.document = toBytes(document);
    }

    public String getDocument() {
        return fromBytes(this.document);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private byte[] toBytes(String data) {
        if (data == null) {
            return new byte[0];
        }

        return data.getBytes(StandardCharsets.UTF_8);
    }

    private String fromBytes(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }
}
