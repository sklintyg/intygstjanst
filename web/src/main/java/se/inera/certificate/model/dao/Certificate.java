/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Certificate (http://code.google.com/p/inera-certificate).
 *
 * Inera Certificate is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Certificate is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.certificate.model.dao;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static se.inera.certificate.model.util.Iterables.find;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.ModelException;
import se.inera.certificate.model.util.Predicate;

/**
 * This class represents the document part of a certificate. The document is stored as a binary large object in the
 * database. The encoding is UTF-8.
 * 
 * @author andreaskaltenbach
 */
@Entity
@Table(name = "CERTIFICATE")
@XmlRootElement
public class Certificate {

    /**
     * Id of the certificate.
     */
    @Id
    @Column(name = "ID")
    private String id;

    /**
     * Certificate document.
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "DOCUMENT")
    private byte[] document;

    /**
     * The transport model (XML) that was used to generate this entity.
     */

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "certificate")
    private OriginalCertificate originalCertificate;

    /**
     * Type of the certificate.
     */
    @Column(name = "CERTIFICATE_TYPE", nullable = false)
    private String type;

    /**
     * Name of the doctor that signed the certificate.
     */
    // TODO: naming? (PW)
    @Column(name = "SIGNING_DOCTOR_NAME", nullable = false)
    private String signingDoctorName;

    /**
     * Name of care unit.
     */
    @Column(name = "CARE_UNIT_ID", nullable = false)
    private String careUnitId;

    /**
     * Name of care unit.
     */
    @Column(name = "CARE_UNIT_NAME", nullable = false)
    private String careUnitName;

    /**
     * Civic registration number for patient.
     */
    @Column(name = "CIVIC_REGISTRATION_NUMBER", nullable = false)
    private String civicRegistrationNumber;

    /**
     * Time this certificate was signed.
     */
    @Column(name = "SIGNED_DATE", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime signedDate;

    /**
     * Time from which this certificate is valid.
     */
    @Column(name = "VALID_FROM_DATE", nullable = false)
    private String validFromDate;

    /**
     * Time to which this certificate is valid.
     */
    @Column(name = "VALID_TO_DATE", nullable = false)
    private String validToDate;

    /**
     * If this certificate is deleted or not.
     */
    @Column(name = "DELETED", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean deleted = Boolean.FALSE;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "CERTIFICATE_STATE", joinColumns = @JoinColumn(name = "CERTIFICATE_ID"))
    private Collection<CertificateStateHistoryEntry> states = new ArrayList<>();

    /**
     * Constructor that takes an id and a document.
     * 
     * @param id
     *            the id
     * @param document
     *            the document
     */
    public Certificate(String id, String document) {
        this.id = id;
        doSetDocument(document);
    }

    /**
     * Constructor for JPA.
     */
    public Certificate() {
        // Empty
    }

    private void doSetDocument(String document) {
        this.document = toBytes(document);
    }

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * @return document
     */
    public String getDocument() {
        return fromBytes(this.document);
    }

    /**
     * Sets the document data.
     * 
     * @param document
     */
    public void setDocument(String document) {
        doSetDocument(document);
    }

    public OriginalCertificate getOriginalCertificate() {
        return originalCertificate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSigningDoctorName() {
        return signingDoctorName;
    }

    public void setSigningDoctorName(String signingDoctorName) {
        this.signingDoctorName = signingDoctorName;
    }

    public String getCareUnitId() {
        return careUnitId;
    }

    public void setCareUnitId(String careUnitId) {
        this.careUnitId = careUnitId;
    }

    public String getCareUnitName() {
        return careUnitName;
    }

    public void setCareUnitName(String careUnitName) {
        this.careUnitName = careUnitName;
    }

    public String getCivicRegistrationNumber() {
        return civicRegistrationNumber;
    }

    public void setCivicRegistrationNumber(String civicRegistrationNumber) {
        this.civicRegistrationNumber = civicRegistrationNumber;
    }

    public LocalDateTime getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(LocalDateTime signedDate) {
        this.signedDate = signedDate;
    }

    public String getValidFromDate() {
        return validFromDate;
    }

    public void setValidFromDate(String validFromDate) {
        this.validFromDate = validFromDate;
    }

    public String getValidToDate() {
        return validToDate;
    }

    public void setValidToDate(String validToDate) {
        this.validToDate = validToDate;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public List<CertificateStateHistoryEntry> getStates() {
        return Collections.unmodifiableList(CertificateStateHistoryEntry.byTimestampDesc.sortedCopy(states));
    }

    public void setStates(List<CertificateStateHistoryEntry> states) {
        this.states = states;
    }

    public void addState(CertificateStateHistoryEntry state) {
        this.states.add(state);
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

    public boolean isRevoked() {
        return find(getStates(), new Predicate<CertificateStateHistoryEntry>() {
            @Override
            public boolean apply(CertificateStateHistoryEntry state) {
                return state.getState() == CertificateState.CANCELLED;
            }
        }, null) != null;
    }

    public boolean wasSentToTarget(final String target) {
        return find(getStates(), new Predicate<CertificateStateHistoryEntry>() {
            @Override
            public boolean apply(CertificateStateHistoryEntry state) {
                return state.getState() == CertificateState.SENT && state.getTarget().equals(target);
            }
        }, null) != null;
    }

    @Override
    public String toString() {
        return "Certificate{" + "id='" + id + '\'' + ", document=" + fromBytes(document) + ", type='" + type + '\''
                + ", signingDoctorName='" + signingDoctorName + '\'' + ", careUnitName='" + careUnitName + '\''
                + ", civicRegistrationNumber='" + civicRegistrationNumber + '\'' + ", signedDate=" + signedDate
                + ", validFromDate='" + validFromDate + '\'' + ", validToDate='" + validToDate + '\'' + ", deleted="
                + deleted + ", states=" + states + '}';
    }

}