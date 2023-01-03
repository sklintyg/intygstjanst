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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by eriklupander on 2016-02-02.
 */
@Entity
@Table(name = "SJUKFALL_CERT_WORK_CAPACITY")
public class SjukfallCertificateWorkCapacity {

    /**
     * Just needed for JPA compliance.
     */
    @Id
    @GeneratedValue
    @Column(name = "ID", nullable = false)
    private long id;

    @Column(name = "CAPACITY_PERCENTAGE", nullable = false)
    private Integer capacityPercentage;

    /**
     * Time from which this certificate is valid.
     */
    @Column(name = "FROM_DATE", nullable = false)
    private String fromDate;

    /**
     * Time to which this certificate is valid.
     */
    @Column(name = "TO_DATE", nullable = false)
    private String toDate;

    public long getId() {
        return id;
    }

    public Integer getCapacityPercentage() {
        return capacityPercentage;
    }

    public void setCapacityPercentage(Integer capacityPercentage) {
        this.capacityPercentage = capacityPercentage;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }
}
