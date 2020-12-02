/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

import java.io.Serializable;
import java.util.Objects;

public class PopulateProcessedId implements Serializable {

    private String populateId;
    private String jobName;

    public PopulateProcessedId() {
    }

    public PopulateProcessedId(String populateId, String jobName) {
        this.populateId = populateId;
        this.jobName = jobName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PopulateProcessedId that = (PopulateProcessedId) o;
        return populateId.equals(that.populateId) && jobName.equals(that.jobName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(populateId, jobName);
    }
}
