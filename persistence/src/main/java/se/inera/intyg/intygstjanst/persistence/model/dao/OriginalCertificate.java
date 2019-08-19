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
package se.inera.intyg.intygstjanst.persistence.model.dao;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import se.inera.intyg.common.support.model.ModelException;

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

    @OneToOne(optional = true, fetch = FetchType.LAZY)
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

        try {
            return data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ModelException("Failed to convert String to bytes!", e);
        }
    }

    private String fromBytes(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ModelException("Failed to convert bytes to String!", e);
        }
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }
}
