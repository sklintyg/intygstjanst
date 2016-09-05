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
package se.inera.intyg.intygstjanst.persistence.model.dao;

import java.time.LocalDateTime;
import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;

import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.common.support.peristence.dao.util.DaoUtil;

/**
 * This class represents the document part of a certificate. The document is stored as a binary large object in the
 * database. The encoding is UTF-8.
 *
 * * @author andreaskaltenbach
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
     * The transport model (XML) that was used to generate this entity.
     */
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "certificate", cascade = CascadeType.REMOVE)
    private OriginalCertificate originalCertificate;

    /**
     * Type of the certificate.
     */
    @Column(name = "CERTIFICATE_TYPE", nullable = false)
    private String type;

    /**
     * Name of the doctor that signed the certificate.
     */
    @Column(name = "SIGNING_DOCTOR_NAME", nullable = false)
    private String signingDoctorName;

    /**
     * Id of care unit.
     */
    @Column(name = "CARE_UNIT_ID", nullable = false)
    private String careUnitId;

    /**
     * Name of care unit.
     */
    @Column(name = "CARE_UNIT_NAME", nullable = false)
    private String careUnitName;

    /**
     * Id of care giver.
     */
    @Column(name = "CARE_GIVER_ID", nullable = false)
    private String careGiverId;

    /**
     * Civic registration number for patient.
     */
    @Column(name = "CIVIC_REGISTRATION_NUMBER", nullable = false)
    private String civicRegistrationNumber;

    /**
     * Time this certificate was signed.
     */
    @Column(name = "SIGNED_DATE", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime signedDate;

    /**
     * Time from which this certificate is valid.
     */
    @Column(name = "VALID_FROM_DATE", nullable = true)
    private String validFromDate;

    /**
     * Time to which this certificate is valid.
     */
    @Column(name = "VALID_TO_DATE", nullable = true)
    private String validToDate;

    /**
     * Additional information.
     */
    @Column(name = "ADDITIONAL_INFO", nullable = true)
    private String additionalInfo;

    /**
     * If this certificate is no longer used by the care giver.
     * <p>
     * This can be due to that the care giver has stopped using WebCert and have their certificates persisted elsewhere.
     * The certificate can be deleted from the database as soon as the citizen no longer has access to the certificate
     * (by revoking its consent or stops being a citizen).
     */
    @Column(name = "DELETED_BY_CARE_GIVER", nullable = false, columnDefinition = "TINYINT(1")
    private boolean deletedByCareGiver = false;

    /**
     * If this certificate was wireTapped.
     */
    @Column(name = "WIRETAPPED", nullable = false, columnDefinition = "TINYINT(1")
    private boolean wireTapped = false;

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
    public Certificate(String id) {
        this.id = id;
    }

    /**
     * Constructor for JPA.
     */
    public Certificate() {
        // Empty
    }

    /**
     * @return id
     */
    public String getId() {
        return id;
    }

    public OriginalCertificate getOriginalCertificate() {
        return originalCertificate;
    }

    public void setOriginalCertificate(OriginalCertificate originalCertificate) {
        this.originalCertificate = originalCertificate;
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

    public String getCareGiverId() {
        return careGiverId;
    }

    public void setCareGiverId(String careGiverId) {
        this.careGiverId = careGiverId;
    }

    public Personnummer getCivicRegistrationNumber() {
        return new Personnummer(civicRegistrationNumber);
    }

    public void setCivicRegistrationNumber(Personnummer civicRegistrationNumber) {
        this.civicRegistrationNumber = civicRegistrationNumber != null ? DaoUtil.formatPnrForPersistence(civicRegistrationNumber) : null;
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

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public boolean isDeletedByCareGiver() {
        return deletedByCareGiver;
    }

    public void setDeletedByCareGiver(boolean deletedByCareGiver) {
        this.deletedByCareGiver = deletedByCareGiver;
    }

    public boolean isWireTapped() {
        return wireTapped;
    }

    public void setWireTapped(boolean wireTapped) {
        this.wireTapped = wireTapped;
    }

    public List<CertificateStateHistoryEntry> getStates() {
        return Collections.unmodifiableList(CertificateStateHistoryEntry.BY_TIMESTAMP_DESC.sortedCopy(states));
    }

    public void setStates(List<CertificateStateHistoryEntry> states) {
        this.states = states;
    }

    public void addState(CertificateStateHistoryEntry state) {
        this.states.add(state);
    }

    public boolean isRevoked() {
        return getStates().stream().anyMatch(state -> state.getState() == CertificateState.CANCELLED);
    }

    /**
     * Check if this certificate is currently deleted ("arkiverad") by the citizen.
     *
     * @return <code>true</code> if the latest {@link CertificateState} of either type <code>DELETED</code> or <code>RESTORED</code> is <code>DELETED</code>, otherwise return <code>false</code>.
     */
    public boolean isDeleted() {
        for (CertificateStateHistoryEntry state : getStates()) {
            if (state.getState() == CertificateState.DELETED) {
                return true;
            } else if (state.getState() == CertificateState.RESTORED) {
                return false;
            }
        }
        return false;
    }

    public boolean isAlreadySent(final String recipientId) {
        return getStates().stream().anyMatch(state -> state.getState() == CertificateState.SENT && state.getTarget().equals(recipientId));
    }

    @Override
    public String toString() {
        return "Certificate{" + "id='" + id + '\'' + ", type='" + type + '\''
                + ", signingDoctorName='" + signingDoctorName + '\'' + ", careUnitName='" + careUnitName + '\''
                + ", civicRegistrationNumber='" + civicRegistrationNumber + '\'' + ", signedDate=" + signedDate
                + ", validFromDate='" + validFromDate + '\'' + ", validToDate='" + validToDate + '\'' + ", states=" + states + '}';
    }

}
