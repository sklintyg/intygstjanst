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
