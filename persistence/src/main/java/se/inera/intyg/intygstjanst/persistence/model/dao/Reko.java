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

package se.inera.intyg.intygstjanst.persistence.model.dao;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "REKO")
public class Reko {

    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "STATUS", nullable = false)
    private String status;
    @Column(name = "PATIENT_ID", nullable = false)
    private String patientId;
    @Column(name = "STAFF_ID", nullable = false)
    private String staffId;
    @Column(name = "STAFF_NAME", nullable = false)
    private String staffName;
    @Column(name = "CARE_PROVIDER_ID", nullable = false)
    private String careProviderId;
    @Column(name = "CARE_UNIT_ID", nullable = false)
    private String careUnitId;
    @Column(name = "UNIT_ID")
    private String unitId;
    @Column(name = "SICK_LEAVE_TIMESTAMP", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime sickLeaveTimestamp;

    @Column(name = "REGISTRATION_TIMESTAMP", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime registrationTimestamp;

    public String getId() {
        return id;
    }

    public String getCareUnitId() {
        return careUnitId;
    }

    public String getStatus() {
        return status;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getStaffId() {
        return staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public String getCareProviderId() {
        return careProviderId;
    }

    public String getUnitId() {
        return unitId;
    }

    public LocalDateTime getSickLeaveTimestamp() {
        return sickLeaveTimestamp;
    }

    public LocalDateTime getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCareUnitId(String careUnitId) {
        this.careUnitId = careUnitId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public void setCareProviderId(String careProviderId) {
        this.careProviderId = careProviderId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public void setSickLeaveTimestamp(LocalDateTime sickLeaveTimestamp) {
        this.sickLeaveTimestamp = sickLeaveTimestamp;
    }

    public void setRegistrationTimestamp(LocalDateTime registrationTimestamp) {
        this.registrationTimestamp = registrationTimestamp;
    }
}
