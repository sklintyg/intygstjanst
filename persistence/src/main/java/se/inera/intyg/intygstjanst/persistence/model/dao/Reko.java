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

package se.inera.intyg.intygstjanst.persistence.model.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Data
@Table(name = "REKO")
public class Reko {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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
    private LocalDateTime sickLeaveTimestamp;

    @Column(name = "REGISTRATION_TIMESTAMP", nullable = false)
    private LocalDateTime registrationTimestamp;
}
